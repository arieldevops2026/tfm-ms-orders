package dev.ariel.tfm.orders.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * DTO para solicitud de creacion de una orden
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@RegisterForReflection
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateOrderRequest {

    @NotBlank(message = "customerId is required")
    @JsonProperty("customerId")
    private String customerId;

    @NotBlank(message = "paymentMethod is required")
    @JsonProperty("paymentMethod")
    private String paymentMethod;

    @NotEmpty(message = "items is required")
    @Size(max = 20, message = "items must not exceed 20")
    @Valid
    @JsonProperty("items")
    private List<OrderItemRequest> items;
}
