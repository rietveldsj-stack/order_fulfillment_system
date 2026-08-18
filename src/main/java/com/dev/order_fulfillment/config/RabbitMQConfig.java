package com.dev.order_fulfillment.config;


import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitMQConfig {

    // Routing key + queue name constants
    public static final String ORDER_EXCHANGE = "order.exchange";

    public static final String ORDER_PLACED_QUEUE = "order.placed.queue";
    public static final String ORDER_PLACED_ROUTING_KEY = "order.placed";

    public static final String INVENTORY_RESERVED_QUEUE = "inventory.reserved.queue";
    public static final String INVENTORY_RESERVED_ROUTING_KEY = "inventory.reserved";

    public static final String INVENTORY_OUT_OF_STOCK_QUEUE = "inventory.out_of_stock.queue";
    public static final String INVENTORY_OUT_OF_STOCK_ROUTING_KEY = "inventory.out_of_stock";

    public static final String PAYMENT_COMPLETED_QUEUE = "payment.completed.queue";
    public static final String PAYMENT_COMPLETED_ROUTING_KEY = "payment.completed";

    public static final String PAYMENT_FAILED_QUEUE = "payment.failed.queue";
    public static final String PAYMENT_FAILED_ROUTING_KEY = "payment.failed";

    public static final String DEAD_LETTER_EXCHANGE = "dead.letter.exchange";
    public static final String DEAD_LETTER_QUEUE = "dead.letter.queue";
    public static final String DEAD_LETTER_ROUTING_KEY = "dead.letter";

    // The exchange itself.
    @Bean
    public DirectExchange orderExchange() {
        return new DirectExchange(ORDER_EXCHANGE);
    }

    // --- order.placed ---
    @Bean
    public Queue orderPlacedQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", DEAD_LETTER_EXCHANGE);
        args.put("x-dead-letter-routing-key", DEAD_LETTER_ROUTING_KEY);
        return new Queue(ORDER_PLACED_QUEUE, true, false, false, args);
    }

    @Bean
    public Binding orderPlacedBinding(Queue orderPlacedQueue, DirectExchange orderExchange) {
        return BindingBuilder
                .bind(orderPlacedQueue)
                .to(orderExchange)
                .with(ORDER_PLACED_ROUTING_KEY);
    }

    // --- inventory.reserved ---
    @Bean
    public Queue inventoryReservedQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", DEAD_LETTER_EXCHANGE);
        args.put("x-dead-letter-routing-key", DEAD_LETTER_ROUTING_KEY);
        return new Queue(INVENTORY_RESERVED_QUEUE, true, false, false, args);
    }

    @Bean
    public Binding inventoryReservedBinding(Queue inventoryReservedQueue, DirectExchange orderExchange) {
        return BindingBuilder
                .bind(inventoryReservedQueue)
                .to(orderExchange)
                .with(INVENTORY_RESERVED_ROUTING_KEY);
    }

    // --- inventory.out_of_stock ---
    @Bean
    public Queue inventoryOutOfStockQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", DEAD_LETTER_EXCHANGE);
        args.put("x-dead-letter-routing-key", DEAD_LETTER_ROUTING_KEY);
        return new Queue(INVENTORY_OUT_OF_STOCK_QUEUE, true, false, false, args);
    }

    @Bean
    public Binding inventoryOutOfStockBinding(Queue inventoryOutOfStockQueue, DirectExchange orderExchange) {
        return BindingBuilder
                .bind(inventoryOutOfStockQueue)
                .to(orderExchange)
                .with(INVENTORY_OUT_OF_STOCK_ROUTING_KEY);
    }

    // --- payment.completed ---
    @Bean
    public Queue paymentCompletedQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", DEAD_LETTER_EXCHANGE);
        args.put("x-dead-letter-routing-key", DEAD_LETTER_ROUTING_KEY);
        return new Queue(PAYMENT_COMPLETED_QUEUE, true, false, false, args);
    }

    @Bean
    public Binding paymentCompletedBinding(Queue paymentCompletedQueue, DirectExchange orderExchange) {
        return BindingBuilder
                .bind(paymentCompletedQueue)
                .to(orderExchange)
                .with(PAYMENT_COMPLETED_ROUTING_KEY);
    }

    // --- payment.failed ---
    @Bean
    public Queue paymentFailedQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", DEAD_LETTER_EXCHANGE);
        args.put("x-dead-letter-routing-key", DEAD_LETTER_ROUTING_KEY);
        return new Queue(PAYMENT_FAILED_QUEUE, true, false, false, args);
    }

    @Bean
    public Binding paymentFailedBinding(Queue paymentFailedQueue, DirectExchange orderExchange) {
        return BindingBuilder
                .bind(paymentFailedQueue)
                .to(orderExchange)
                .with(PAYMENT_FAILED_ROUTING_KEY);
    }

    // -- dead.letter ---
    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(DEAD_LETTER_EXCHANGE);
    }

    @Bean
    public Queue deadLetterQueue() {
        return new Queue(DEAD_LETTER_QUEUE, true);
    }

    @Bean
    public Binding deadLetterBinding(Queue deadLetterQueue, DirectExchange deadLetterExchange) {
        return BindingBuilder.bind(deadLetterQueue).to(deadLetterExchange).with(DEAD_LETTER_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter jsonMessageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter);
        return template;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory, MessageConverter jsonMessageConverter) {

        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter);
        factory.setAdviceChain(
                RetryInterceptorBuilder.stateless()
                        .maxRetries(3)
                        .recoverer(new RejectAndDontRequeueRecoverer())
                        .build()
        );
        return factory;
    }
}