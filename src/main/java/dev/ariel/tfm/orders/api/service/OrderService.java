package dev.ariel.tfm.orders.api.service;

import dev.ariel.tfm.orders.api.dto.AmountObj;
import dev.ariel.tfm.orders.api.dto.CreateOrderRequest;
import dev.ariel.tfm.orders.api.dto.Order;
import dev.ariel.tfm.orders.api.dto.OrderDetailRequest;
import dev.ariel.tfm.orders.api.dto.OrderItem;
import dev.ariel.tfm.orders.api.dto.OrderItemRequest;
import dev.ariel.tfm.orders.client.payments.dto.AuthorizePaymentRequestDto;
import dev.ariel.tfm.orders.client.payments.dto.PaymentDto;
import dev.ariel.tfm.orders.client.payments.service.PaymentsService;
import dev.ariel.tfm.orders.client.products.dto.ProductDto;
import dev.ariel.tfm.orders.client.products.service.ProductsService;
import dev.ariel.tfm.orders.repository.OrderRepository;
import dev.ariel.tfm.orders.util.constants.Currency;
import dev.ariel.tfm.orders.util.enums.OrderStatus;
import dev.ariel.tfm.orders.util.fault.FaultInjector;
import dev.ariel.tfm.orders.util.utils.GeneralUtils;
import dev.ariel.tfm.orders.util.utils.MDCHelper;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class OrderService {

    private static final String PAYMENT_APPROVED = "APPROVED";
    private static final int MONEY_SCALE = 2;

    private final ProductsService productsService;
    private final PaymentsService paymentsService;
    private final OrderRepository repository;
    private final FaultInjector faultInjector;

    /**
     * Crea una orden: valora los items con ms-products y solicita el pago a ms-payments.
     */
    public Order createOrder(CreateOrderRequest request) {
        faultInjector.apply();
        MDCHelper.setAccount(GeneralUtils.coalesce(request.getCustomerId()));
        log.info("Creando orden para el cliente: {}, Total items: {}", request.getCustomerId(), request.getItems().size());

        List<OrderItem> items = request.getItems().stream()
            .map(this::buildItem)
            .toList();

        AmountObj total = AmountObj.builder()
            .currency(Currency.USD)
            .amount(items.stream()
                .map(OrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add))
            .build();

        String orderId = UUID.randomUUID().toString();

        PaymentDto payment = paymentsService.authorize(AuthorizePaymentRequestDto.builder()
            .orderId(orderId)
            .customerId(request.getCustomerId())
            .amount(total)
            .paymentMethod(request.getPaymentMethod())
            .build());

        Order order = repository.save(Order.builder()
            .orderId(orderId)
            .customerId(request.getCustomerId())
            .status(resolveStatus(payment).name())
            .items(items)
            .total(total)
            .paymentId(payment.getPaymentId())
            .paymentStatus(payment.getStatus())
            .createdAt(Instant.now())
            .build());

        log.info("Orden creada, orderId: {}, estado: {}, total: {}", order.getOrderId(), order.getStatus(), total.getAmount());
        return order;
    }

    public Order getOrderById(OrderDetailRequest request) {
        faultInjector.apply();
        log.info("Obteniendo orden con id: {}", request.getOrderId());

        return repository.findById(request.getOrderId())
            .orElseThrow(() -> {
                log.warn("Orden no encontrada, id: {}", request.getOrderId());
                return new NotFoundException("Orden no encontrada: " + request.getOrderId());
            });
    }

    private OrderItem buildItem(OrderItemRequest itemRequest) {
        ProductDto product = productsService.getById(itemRequest.getProductId());
        BigDecimal unitPrice = product.getPrice().getAmount();

        return OrderItem.builder()
            .productId(product.getProductId())
            .name(product.getName())
            .quantity(itemRequest.getQuantity())
            .unitPrice(unitPrice)
            .subtotal(unitPrice.multiply(BigDecimal.valueOf(itemRequest.getQuantity())).setScale(MONEY_SCALE, RoundingMode.HALF_UP))
            .build();
    }

    private OrderStatus resolveStatus(PaymentDto payment) {
        return PAYMENT_APPROVED.equalsIgnoreCase(payment.getStatus())
            ? OrderStatus.CONFIRMED
            : OrderStatus.PAYMENT_REJECTED;
    }

}
