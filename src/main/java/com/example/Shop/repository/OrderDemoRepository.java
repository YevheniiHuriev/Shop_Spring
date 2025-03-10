package com.example.Shop.repository;

import com.example.Shop.model.OrderDemo;
import com.example.Shop.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderDemoRepository extends JpaRepository<OrderDemo, Long> {
    List<OrderDemo> findByUser(User user);
}
