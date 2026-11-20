package dev.ariel.tfm.orders.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

/**
 * DTO de una orden
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@RegisterForReflection
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Order {

    @JsonProperty("orderId")
    private String orderId;

    @JsonProperty("customerId")
    private String customerId;

    @JsonProperty("status")
    private String status;

    @JsonProperty("items")
    private List<OrderItem> items;

    @JsonProperty("total")
    private AmountObj total;

    @JsonProperty("paymentId")
    private String paymentId;

    @JsonProperty("paymentStatus")
    private String paymentStatus;

    @JsonProperty("createdAt")
    private Instant createdAt;
}
