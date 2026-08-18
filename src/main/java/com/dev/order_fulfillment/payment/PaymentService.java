package com.dev.order_fulfillment.payment;

import com.dev.order_fulfillment.config.RabbitMQConfig;
import com.dev.order_fulfillment.event.InventoryReservedEvent;
import com.dev.order_fulfillment.event.PaymentCompletedEvent;
import com.dev.order_fulfillment.event.PaymentFailedEvent;
import com.dev.order_fulfillment.exceptionHandling.NotFound;
import com.dev.order_fulfillment.order.Order;
import com.dev.order_fulfillment.order.OrderRepository;
import com.dev.order_fulfillment.order.OrderStatus;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class PaymentService {

    private final OrderRepository orderRepository;
    private final RabbitTemplate rabbitTemplate;
    private final Random random = new Random();

    public PaymentService(OrderRepository orderRepository, RabbitTemplate rabbitTemplate) {
        this.orderRepository = orderRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    @RabbitListener(queues = RabbitMQConfig.INVENTORY_RESERVED_QUEUE)
    public void handleInventoryReserved(InventoryReservedEvent event) {

        boolean paymentSucceeded = random.nextInt(100) < 80;
        Order order = orderRepository.findById(event.orderId()).orElseThrow(() -> new NotFound("Order not found"));

        if (paymentSucceeded) {
            order.setOrderStatus(OrderStatus.PAYMENT_COMPLETED);
            orderRepository.save(order);

            PaymentCompletedEvent paymentCompletedEvent =
                    new PaymentCompletedEvent(order.getId(), order.getCustomer().getId(), event.items());
            rabbitTemplate.convertAndSend(RabbitMQConfig.ORDER_EXCHANGE, RabbitMQConfig.PAYMENT_COMPLETED_ROUTING_KEY, paymentCompletedEvent);

        } else {
            order.setOrderStatus(OrderStatus.PAYMENT_FAILED);
            orderRepository.save(order);

            PaymentFailedEvent paymentFailedEvent =
                    new PaymentFailedEvent(order.getId(), order.getCustomer().getId(), event.items());
            rabbitTemplate.convertAndSend(RabbitMQConfig.ORDER_EXCHANGE, RabbitMQConfig.PAYMENT_FAILED_ROUTING_KEY, paymentFailedEvent);
        }
    }

}
