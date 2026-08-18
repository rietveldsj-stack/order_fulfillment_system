package com.dev.order_fulfillment.shipment;

import com.dev.order_fulfillment.order.Order;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "shipments")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class Shipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @OneToOne
    @JoinColumn(name = "order_id", unique = true)
    private Order order;

    @Column
    @CreationTimestamp
    private LocalDateTime shippedDate;
}
