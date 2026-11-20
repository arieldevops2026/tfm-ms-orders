package dev.ariel.tfm.orders.client.payments.service;

import dev.ariel.tfm.orders.client.payments.PaymentsClient;
import dev.ariel.tfm.orders.client.payments.dto.AuthorizePaymentRequestDto;
import dev.ariel.tfm.orders.client.payments.dto.PaymentDto;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.WebApplicationException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
public class PaymentsService {

    private final PaymentsClient paymentsClient;

    @Inject
    public PaymentsService(@RestClient PaymentsClient paymentsClient) {
        this.paymentsClient = paymentsClient;
    }

    /**
     * Solicita la autorizacion de un pago a ms-payments. Cualquier fallo de la dependencia se propaga como 500.
     */
    public PaymentDto authorize(AuthorizePaymentRequestDto request) {
        try {
            return paymentsClient.authorize(request);
        } catch (WebApplicationException e) {
            log.error("ms-payments respondio con error, orderId: {}, estado: {}",
                request.getOrderId(), e.getResponse().getStatus(), e);
            throw new InternalServerErrorException("Error consultando ms-payments", e);
        } catch (ProcessingException e) {
            log.error("Error de comunicacion con ms-payments, orderId: {}", request.getOrderId(), e);
            throw new InternalServerErrorException("Error de comunicacion con ms-payments", e);
        }
    }

}
