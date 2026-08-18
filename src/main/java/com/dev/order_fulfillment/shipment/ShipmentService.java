package com.dev.order_fulfillment.shipment;

import com.dev.order_fulfillment.config.RabbitMQConfig;
import com.dev.order_fulfillment.event.PaymentCompletedEvent;
import com.dev.order_fulfillment.exceptionHandling.NotFound;
import com.dev.order_fulfillment.order.Order;
import com.dev.order_fulfillment.order.OrderRepository;
import com.dev.order_fulfillment.order.OrderStatus;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class ShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final OrderRepository orderRepository;

    public ShipmentService(ShipmentRepository shipmentRepository, OrderRepository orderRepository) {
        this.shipmentRepository = shipmentRepository;
        this.orderRepository = orderRepository;
    }

    @RabbitListener(queues = RabbitMQConfig.PAYMENT_COMPLETED_QUEUE)
    public void handlePaymentCompleted(PaymentCompletedEvent event){

        Order order = orderRepository.findById(event.orderId()).orElseThrow(() -> new NotFound("Order not found"));
        Shipment shipment = Shipment.builder().order(order).build();

        shipmentRepository.save(shipment);
        order.setOrderStatus(OrderStatus.SHIPPED);
        orderRepository.save(order);
    }
}
