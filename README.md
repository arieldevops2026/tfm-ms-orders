# tfm-ms-orders

Microservicio de **órdenes** del TFM *"Diseño y evaluación de una plataforma GitOps con entrega progresiva gobernada por SLO y telemetría OpenTelemetry para microservicios en Kubernetes"* (Máster en DevOps, UNIR).

Es el **punto de entrada** de la aplicación experimental de tres servicios sobre la que se evalúa la estrategia de despliegue canary gobernada por objetivos de nivel de servicio (SLO). El generador de carga de los experimentos envía sus peticiones a este servicio.

## Papel en el laboratorio

```text
cliente ──► ms-orders ──► ms-products   (valora cada item)
                     └──► ms-payments   (autoriza el pago del total)
```

Para crear una orden, el servicio:

1. Consulta cada producto en `ms-products` y calcula subtotales y total.
2. Solicita la autorización del total a `ms-payments`.
3. Devuelve la orden con estado `CONFIRMED` si el pago se aprueba, o `PAYMENT_REJECTED` si se rechaza.

Como depende de los otros dos servicios, una versión defectuosa de cualquiera de ellos se refleja también en sus métricas. Por eso el análisis del canary consulta la versión candidata del servicio que se despliega, y no el promedio global.

## Tecnología

- Quarkus 3.40, Java 25, hilos virtuales (`@RunOnVirtualThread`).
- Clientes REST de MicroProfile hacia `ms-products` y `ms-payments`.
- Compilación nativa con GraalVM/Mandrel para la imagen de contenedor.
- Lombok, Hibernate Validator, SmallRye OpenAPI y Health.
- Micrometer (métricas Prometheus) y OpenTelemetry (trazas y logs).

## Llamadas a dependencias

- **Sin reintentos.** Un reintento ocultaría fallos y deformaría la latencia que se mide en los experimentos.
- **Timeouts acotados:** 1 s de conexión y 3 s de lectura.
- Un producto inexistente devuelve `404`. Cualquier otro fallo de una dependencia devuelve `500`.
- Las cabeceras `uuid`, `ip`, `channel` y `user` se propagan a las dependencias, y el contexto de traza viaja en `traceparent`. Así, una orden produce una sola traza que atraviesa los tres servicios.

## Datos

Las órdenes se guardan **en memoria**, por réplica, y se conservan solo las últimas 10 000. Se pierden al reiniciar. No hay base de datos, por lo que revertir una versión defectuosa no deja datos persistentes que corregir.

## API

Base: `http://localhost:18000/api/v1`. Contrato completo en `src/main/resources/META-INF/openapi.yaml` y en Swagger UI (`/swagger-ui`). Las peticiones van dentro de `data` y las respuestas siguen el formato `code` + `frame`.

| Método | Ruta | Descripción |
|---|---|---|
| `POST` | `/orders/create` | Crea una orden (flujo usado en los experimentos) |
| `POST` | `/orders/get-detail` | Consulta una orden. `404` si no existe en esta réplica |

```bash
curl -X POST localhost:18000/api/v1/orders/create \
  -H 'Content-Type: application/json' -H 'session-id: demo-001' \
  -d '{"data":{"customerId":"CUS-001","paymentMethod":"CARD","items":[{"productId":"PRD-001","quantity":1},{"productId":"PRD-003","quantity":2}]}}'
```

Cabeceras opcionales de trazabilidad: `session-id`, `username`, `client-ip`, `channel`. El `session-id` se registra como `uuid` en los logs de los tres servicios.

## Configuración

Toda la configuración se sobrescribe con variables de entorno, sin cambiar el código. Así, una misma imagen sirve como versión estable o como versión candidata defectuosa.

| Variable | Por defecto | Uso |
|---|---|---|
| `MS_PRODUCTS_URL` | `http://localhost:18001/api/v1/` | URL base de `ms-products` |
| `MS_PAYMENTS_URL` | `http://localhost:18002/api/v1/` | URL base de `ms-payments` |
| `APP_VERSION` | `1.0.0` | Versión que aparece en métricas, trazas y logs (`service_version`) |
| `APP_ENVIRONMENT` | `local` | Entorno (`deployment_environment`) |
| `QUARKUS_OTEL_EXPORTER_OTLP_ENDPOINT` | `http://localhost:4317` | OpenTelemetry Collector |
| `QUARKUS_OTEL_SDK_DISABLED` | `false` | `true` para no enviar telemetría |
| `FAULT_MODE` | `NONE` | Fallo a inyectar (ver abajo) |
| `FAULT_LATENCY_MS` | `0` | Retardo del modo `LATENCY` |
| `FAULT_TIMEOUT_MS` | `10000` | Espera del modo `TIMEOUT` |
| `FAULT_PROBABILITY` | `1.0` | Proporción de peticiones afectadas (0.0 a 1.0) |

## Inyección de fallos

Permite construir las versiones defectuosas de los experimentos:

| `FAULT_MODE` | Efecto |
|---|---|
| `NONE` | Sin fallo (versión saludable) |
| `LATENCY` | Añade `FAULT_LATENCY_MS` a la respuesta |
| `ERROR` | Responde HTTP 500 |
| `TIMEOUT` | Espera `FAULT_TIMEOUT_MS` antes de responder |

```bash
# Versión candidata 1.1.0 con 700 ms de latencia en el 30 % de las peticiones
APP_VERSION=1.1.0 FAULT_MODE=LATENCY FAULT_LATENCY_MS=700 FAULT_PROBABILITY=0.3 \
  java -jar target/quarkus-app/quarkus-run.jar
```

## Observabilidad

| Señal | Dónde | Uso en el TFM |
|---|---|---|
| Métricas | `/q/metrics` (Prometheus las consulta) | **Decisión** del canary: SLI de latencia y errores |
| Trazas | OTLP al Collector, luego Tempo | Diagnóstico: una orden muestra sus llamadas a products y payments |
| Logs | OTLP al Collector, luego Loki | Diagnóstico, enlazados a la traza por `trace_id` |
| Salud | `/q/health`, `/q/health/ready`, `/q/health/live` | Sondas de Kubernetes |

Las métricas HTTP de servidor y de cliente llevan las etiquetas `service_name`, `service_version` y `deployment_environment`, y el histograma de latencia incluye un bucket exacto en **500 ms**, el umbral del SLO. Ejemplo del SLI de latencia:

```promql
histogram_quantile(0.95, sum by (le, service_version) (
  rate(http_server_requests_seconds_bucket{service_name="ms-orders", uri="/orders/create"}[1m])))
```

## Ejecutar

Necesita que `ms-products` (18001) y `ms-payments` (18002) estén levantados. Sin ellos, `create` responde `500`.

```bash
./mvnw quarkus:dev                                    # desarrollo, con recarga en caliente
./mvnw package && java -jar target/quarkus-app/quarkus-run.jar   # JVM
```

En modo dev la telemetría se controla con `%dev.quarkus.otel.sdk.disabled` en `application.properties`. Para ver trazas y logs en local, levanta antes el stack de observabilidad del laboratorio (`local-stack`).

## Imagen nativa

```bash
./mvnw package -Pnative -DskipTests -Dquarkus.native.container-build=true   # ~3-5 min, no requiere GraalVM local
docker build -f src/main/docker/Dockerfile.native-micro \
  --build-arg GIT_SHA=$(git rev-parse HEAD) -t tfm-ms-orders:local .
```

Para ejecutarla junto a las otras dos, los contenedores deben compartir una red y localizarse por nombre:

```bash
docker network create tfm
docker run -d --rm --name ms-products --network tfm -e QUARKUS_OTEL_SDK_DISABLED=true tfm-ms-products:local
docker run -d --rm --name ms-payments --network tfm -e QUARKUS_OTEL_SDK_DISABLED=true tfm-ms-payments:local
docker run -d --rm --name ms-orders --network tfm -p 18000:18000 -e QUARKUS_OTEL_SDK_DISABLED=true \
  -e MS_PRODUCTS_URL=http://ms-products:18001/api/v1/ \
  -e MS_PAYMENTS_URL=http://ms-payments:18002/api/v1/ \
  tfm-ms-orders:local
```

La imagen parte de una base mínima fijada por digest, corre con un usuario sin privilegios (`1001`) e incluye en la etiqueta `org.opencontainers.image.revision` el commit del que se construyó.

## Limitaciones

- Datos en memoria: no hay persistencia.
- `POST /orders/get-detail` solo encuentra órdenes de la réplica que las creó. Con varias réplicas, como durante un canary, puede responder `404`. El flujo de los experimentos solo usa `create`.
- Cuando una dependencia rechaza la conexión, la traza no incluye el span de esa llamada. El error queda registrado en los logs.
- Sin autenticación: está pensado para un laboratorio aislado.
