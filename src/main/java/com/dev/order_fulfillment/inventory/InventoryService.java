package com.dev.order_fulfillment.inventory;

import com.dev.order_fulfillment.config.RabbitMQConfig;
import com.dev.order_fulfillment.event.InventoryOutOfStockEvent;
import com.dev.order_fulfillment.event.InventoryReservedEvent;
import com.dev.order_fulfillment.event.OrderPlacedEvent;
import com.dev.order_fulfillment.event.PaymentFailedEvent;
import com.dev.order_fulfillment.exceptionHandling.NotFound;
import com.dev.order_fulfillment.order.Order;
import com.dev.order_fulfillment.order.OrderRepository;
import com.dev.order_fulfillment.order.OrderStatus;
import com.dev.order_fulfillment.product.Product;
import com.dev.order_fulfillment.product.ProductRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InventoryService {

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final RabbitTemplate rabbitTemplate;

    public InventoryService(ProductRepository productRepository, OrderRepository orderRepository, RabbitTemplate rabbitTemplate) {
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Transactional
    @RabbitListener(queues = RabbitMQConfig.ORDER_PLACED_QUEUE)
    public void handleOrderPlaced(OrderPlacedEvent event){

        boolean allInStock = event.items().stream()
                .allMatch(item -> {
                    Product product = productRepository.findById(item.productId()).orElseThrow(() -> new NotFound("Product not found"));
                    return product.getStockQuantity() >= item.quantity();
                });

        Order order = orderRepository.findById(event.orderId()).orElseThrow(() -> new NotFound("Order not found"));


        if (allInStock) {
            event.items()
                    .forEach(item -> {
                        Product product = productRepository.findById(item.productId()).orElseThrow(() -> new NotFound("Product not found"));
                        product.setStockQuantity(product.getStockQuantity() - item.quantity());
                        productRepository.save(product);
                    });

            order.setOrderStatus(OrderStatus.RESERVED);
            orderRepository.save(order);

            InventoryReservedEvent inventoryReservedEvent =
                    new InventoryReservedEvent(order.getId(), order.getCustomer().getId(), event.totalAmount(), event.items());

            rabbitTemplate.convertAndSend(RabbitMQConfig.ORDER_EXCHANGE, RabbitMQConfig.INVENTORY_RESERVED_ROUTING_KEY, inventoryReservedEvent);


        } else {

            order.setOrderStatus(OrderStatus.OUT_OF_STOCK);
            orderRepository.save(order);

            InventoryOutOfStockEvent inventoryOutOfStockEvent =
                    new InventoryOutOfStockEvent(order.getId(), order.getCustomer().getId());

            rabbitTemplate.convertAndSend(RabbitMQConfig.ORDER_EXCHANGE, RabbitMQConfig.INVENTORY_OUT_OF_STOCK_ROUTING_KEY, inventoryOutOfStockEvent);
        }
    }

    @Transactional
    @RabbitListener(queues = RabbitMQConfig.PAYMENT_FAILED_QUEUE)
    public void handleOrderFailed(PaymentFailedEvent event) {

        Order order = orderRepository.findById(event.orderId()).orElseThrow(() -> new NotFound("Order not found"));

        event.items().forEach(item -> {
            Product product = productRepository.findById(item.productId()).orElseThrow(() -> new NotFound("Product not found"));
            product.setStockQuantity(product.getStockQuantity() + item.quantity());
            productRepository.save(product);
        });

        order.setOrderStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
    }
}
