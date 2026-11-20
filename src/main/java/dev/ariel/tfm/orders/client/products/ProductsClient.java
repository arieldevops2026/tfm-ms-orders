package dev.ariel.tfm.orders.client.products;

import dev.ariel.tfm.orders.client.products.dto.ProductDto;

import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;

/**
 * Cliente REST de ms-products. No define reintentos: un reintento ocultaria fallos y distorsionaria las metricas del experimento.
 */
@ApplicationScoped
@Path("/products")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RegisterRestClient(configKey = "ms-products")
@ClientHeaderParam(name = "uuid", value = "{dev.ariel.tfm.orders.util.utils.MDCHelper.getUuid}")
@ClientHeaderParam(name = "ip", value = "{dev.ariel.tfm.orders.util.utils.MDCHelper.getIp}")
@ClientHeaderParam(name = "channel", value = "{dev.ariel.tfm.orders.util.utils.MDCHelper.getChannel}")
@ClientHeaderParam(name = "user", value = "{dev.ariel.tfm.orders.util.utils.MDCHelper.getUser}")
public interface ProductsClient {

    @GET
    @Path("/get-by-id/{id}")
    ProductDto getById(@PathParam("id") String id);

}
