package dev.ariel.tfm.orders.client.products.service;

import dev.ariel.tfm.orders.client.products.ProductsClient;
import dev.ariel.tfm.orders.client.products.dto.ProductDto;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
public class ProductsService {

    private final ProductsClient productsClient;

    @Inject
    public ProductsService(@RestClient ProductsClient productsClient) {
        this.productsClient = productsClient;
    }

    /**
     * Consulta un producto en ms-products. Un 404 se propaga como 404 y cualquier otro fallo como 500.
     */
    public ProductDto getById(String productId) {
        try {
            return productsClient.getById(productId);
        } catch (WebApplicationException e) {
            int status = e.getResponse().getStatus();
            if (status == Response.Status.NOT_FOUND.getStatusCode()) {
                log.warn("Producto no encontrado en ms-products, id: {}", productId);
                throw new NotFoundException("Producto no encontrado: " + productId);
            }
            log.error("ms-products respondio con error, id: {}, estado: {}", productId, status, e);
            throw new InternalServerErrorException("Error consultando ms-products", e);
        } catch (ProcessingException e) {
            log.error("Error de comunicacion con ms-products, id: {}", productId, e);
            throw new InternalServerErrorException("Error de comunicacion con ms-products", e);
        }
    }

}
