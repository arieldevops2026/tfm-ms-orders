package dev.ariel.tfm.orders.client.payments;

import dev.ariel.tfm.orders.client.payments.dto.AuthorizePaymentRequestDto;
import dev.ariel.tfm.orders.client.payments.dto.PaymentDto;

import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

/**
 * Cliente REST de ms-payments. No define reintentos: un reintento ocultaria fallos y distorsionaria las metricas del experimento.
 */
@ApplicationScoped
@Path("/payments")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RegisterRestClient(configKey = "ms-payments")
@ClientHeaderParam(name = "uuid", value = "{dev.ariel.tfm.orders.util.utils.MDCHelper.getUuid}")
@ClientHeaderParam(name = "ip", value = "{dev.ariel.tfm.orders.util.utils.MDCHelper.getIp}")
@ClientHeaderParam(name = "channel", value = "{dev.ariel.tfm.orders.util.utils.MDCHelper.getChannel}")
@ClientHeaderParam(name = "user", value = "{dev.ariel.tfm.orders.util.utils.MDCHelper.getUser}")
public interface PaymentsClient {

    @POST
    @Path("/authorize")
    PaymentDto authorize(AuthorizePaymentRequestDto request);

}
