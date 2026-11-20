package dev.ariel.tfm.orders.api.resource;

import dev.ariel.tfm.orders.api.dto.CreateOrderRequest;
import dev.ariel.tfm.orders.api.dto.GeneralRequest;
import dev.ariel.tfm.orders.api.dto.Order;
import dev.ariel.tfm.orders.api.dto.OrderDetailRequest;
import dev.ariel.tfm.orders.api.dto.OrderResponse;
import dev.ariel.tfm.orders.api.dto.OrderResponse.OrderFrame;
import dev.ariel.tfm.orders.api.service.OrderService;
import dev.ariel.tfm.orders.api.service.RequestService;
import dev.ariel.tfm.orders.util.constants.GeneralCodResponse;

import io.smallrye.common.annotation.RunOnVirtualThread;
import io.vertx.core.http.HttpServerRequest;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.Valid;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;

/**
 * Resource para operaciones de ordenes
 */
@Slf4j
@Path("/orders")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class OrderResource {

    private final OrderService service;
    private final RequestService requestService;

    /**
     * Endpoint para crear una orden
     */
    @POST
    @Path("/create")
    @RunOnVirtualThread
    public OrderResponse create(@Context HttpServerRequest context, @NotNull @Valid GeneralRequest<CreateOrderRequest> request) {
        requestService.onInitialProcess(context, request);

        Order order = service.createOrder(request.getData());

        OrderResponse response = buildResponse(order);

        requestService.onFinalProcess(response);
        return response;
    }

    /**
     * Endpoint para obtener el detalle de una orden
     */
    @POST
    @Path("/get-detail")
    @RunOnVirtualThread
    public OrderResponse getDetail(@Context HttpServerRequest context, @NotNull @Valid GeneralRequest<OrderDetailRequest> request) {
        requestService.onInitialProcess(context, request);

        Order order = service.getOrderById(request.getData());

        OrderResponse response = buildResponse(order);

        requestService.onFinalProcess(response);
        return response;
    }

    private OrderResponse buildResponse(Order order) {
        return OrderResponse.builder()
            .code(GeneralCodResponse.SUCCESS)
            .frame(OrderFrame.builder()
                .order(order)
                .build())
            .build();
    }

}
