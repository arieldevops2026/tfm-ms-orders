package dev.ariel.tfm.orders.util.enums;

/**
 * Tipos de fallo que se pueden inyectar para los experimentos de entrega progresiva
 */
public enum FaultMode {

    NONE,
    LATENCY,
    ERROR,
    TIMEOUT;
}
