package com.dev.order_fulfillment.order;

import com.dev.order_fulfillment.customer.Customer;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/order")
public class OrderController {

    private final OrderService orderService;
    private final OrderMapper orderMapper;


    public OrderController(OrderService orderService, OrderMapper orderMapper) {
        this.orderService = orderService;
        this.orderMapper = orderMapper;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder (@AuthenticationPrincipal Customer customer,
                                              @RequestBody @Valid CreateOrderRequest createOrderRequest) {

        Order order = orderService.createOrder(customer, createOrderRequest);

        return ResponseEntity.status(HttpStatus.CREATED).body(orderMapper.toResponse(order));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrder (@AuthenticationPrincipal Customer customer, @PathVariable Long orderId){

        Order order = orderService.getOrderForCustomer(customer, orderId);

       return ResponseEntity.ok(orderMapper.toResponse(order));
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> getOrders (@AuthenticationPrincipal Customer customer){

       List<OrderResponse> list = orderService.getOrders(customer).stream()
                .map(orderMapper::toResponse)
                .toList();

       return ResponseEntity.ok(list);
    }
}
