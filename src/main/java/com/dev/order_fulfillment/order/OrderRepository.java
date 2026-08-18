package com.dev.order_fulfillment.order;

import com.dev.order_fulfillment.customer.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order>findByCustomer(Customer customer);
}
