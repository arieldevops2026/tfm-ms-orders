package dev.ariel.tfm.orders.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO de respuesta para operaciones de ordenes
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@RegisterForReflection
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderResponse {

    @JsonProperty("code")
    private String code;

    @JsonProperty("frame")
    private OrderFrame frame;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @RegisterForReflection
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class OrderFrame {

        @JsonProperty("order")
        private Order order;
    }

}
