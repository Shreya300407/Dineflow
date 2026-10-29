package com.dineflow.repository;

import com.dineflow.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * Find order by its unique public alphanumeric order number.
     */
    Optional<Order> findByOrderNumber(String orderNumber);

    /**
     * List all orders in descending order of placement time.
     */
    List<Order> findAllByOrderByCreatedAtDesc();
}
