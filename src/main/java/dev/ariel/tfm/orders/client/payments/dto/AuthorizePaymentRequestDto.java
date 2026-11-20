package dev.ariel.tfm.orders.client.payments.dto;

import dev.ariel.tfm.orders.api.dto.AmountObj;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO de solicitud de autorizacion enviado a ms-payments
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@RegisterForReflection
public class AuthorizePaymentRequestDto {

    @JsonProperty("orderId")
    private String orderId;

    @JsonProperty("customerId")
    private String customerId;

    @JsonProperty("amount")
    private AmountObj amount;

    @JsonProperty("paymentMethod")
    private String paymentMethod;
}
