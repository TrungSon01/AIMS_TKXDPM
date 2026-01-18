package com.example.AIMS_TKXDPM.repository;

import com.example.AIMS_TKXDPM.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {
}
