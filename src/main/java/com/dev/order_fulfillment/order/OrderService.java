package com.dev.order_fulfillment.order;

import com.dev.order_fulfillment.config.RabbitMQConfig;
import com.dev.order_fulfillment.customer.Customer;
import com.dev.order_fulfillment.event.OrderPlacedEvent;
import com.dev.order_fulfillment.event.OrderPlacedItem;
import com.dev.order_fulfillment.exceptionHandling.NotFound;
import com.dev.order_fulfillment.exceptionHandling.UnauthorizedException;
import com.dev.order_fulfillment.product.Product;
import com.dev.order_fulfillment.product.ProductRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final RabbitTemplate rabbitTemplate;

    public OrderService(OrderRepository orderRepository, ProductRepository productRepository, RabbitTemplate rabbitTemplate) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    public Order createOrder(Customer customer, CreateOrderRequest request) {

        if (customer == null) {
            throw new UnauthorizedException("Authenticated customer required to place an order");
        }

        List<OrderItem> orderItems = new ArrayList<>();

        request.orderItemRequests().forEach(orderItemRequest -> {

            Product product = productRepository.findById(orderItemRequest.productId()).orElseThrow(() -> new NotFound("Product not found"));

            OrderItem orderItem = OrderItem.builder()
                    .product(product)
                    .quantity(orderItemRequest.quantity())
                    .priceAtPurchase(product.getPrice())
                    .build();

            orderItems.add(orderItem);
        });

        Order order = Order.builder()
                .orderItems(orderItems)
                .customer(customer)
                .orderStatus(OrderStatus.ORDER_PLACED)
                .totalAmount(orderItems.stream()
                        .map(orderItem ->
                                orderItem.getPriceAtPurchase()
                                        .multiply(BigDecimal.valueOf(orderItem.getQuantity())))
                        .reduce(BigDecimal.ZERO, BigDecimal::add))
                .build();

        orderItems.forEach(orderItem -> orderItem.setOrder(order));

        Order savedOrder = orderRepository.save(order);

        List<OrderPlacedItem> orderPlacedItems = new ArrayList<>();

        orderItems.forEach(orderItem -> {
            OrderPlacedItem orderPlacedItem = new OrderPlacedItem(orderItem.getProduct().getId(), orderItem.getQuantity());
            orderPlacedItems.add(orderPlacedItem);
        });

        OrderPlacedEvent orderPlacedEvent = new OrderPlacedEvent(savedOrder.getId(), orderPlacedItems, savedOrder.getTotalAmount());

        rabbitTemplate.convertAndSend(RabbitMQConfig.ORDER_EXCHANGE, RabbitMQConfig.ORDER_PLACED_ROUTING_KEY, orderPlacedEvent);

        return savedOrder;
    }

    public Order getOrderForCustomer(Customer customer, Long orderId){

        Order order = orderRepository.findById(orderId).orElseThrow(() -> new NotFound("Order not found"));

        if (!order.getCustomer().getId().equals(customer.getId())){
            throw new NotFound("Order not found");
        }

        return order;
    }

    public List<Order> getOrders(Customer customer){

        if (customer==null){
            throw new UnauthorizedException("Authenticated customer required to view orders");
        }
        return orderRepository.findByCustomer(customer);
    }
}
