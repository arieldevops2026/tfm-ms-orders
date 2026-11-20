package dev.ariel.tfm.orders.client.payments.dto;

import dev.ariel.tfm.orders.api.dto.AmountObj;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * DTO de un pago devuelto por ms-payments
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@RegisterForReflection
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentDto {

    @JsonProperty("paymentId")
    private String paymentId;

    @JsonProperty("orderId")
    private String orderId;

    @JsonProperty("customerId")
    private String customerId;

    @JsonProperty("amount")
    private AmountObj amount;

    @JsonProperty("paymentMethod")
    private String paymentMethod;

    @JsonProperty("status")
    private String status;

    @JsonProperty("reason")
    private String reason;

    @JsonProperty("processedAt")
    private Instant processedAt;
}
