package com.learningMongo.Aditya.Learning_Mongo;

import com.learningMongo.Aditya.Learning_Mongo.entity.Order;
import com.learningMongo.Aditya.Learning_Mongo.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;

@SpringBootTest
public class SimpleMongoTests {

    @Autowired
    private OrderRepository orderRepository;

    @Test
    public void testCreateOrder() {
        for(int i =1; i<10; i++) {
            Order order = Order.builder()
                    .status("READY")
                    .quantity(2*i)
                    .totalPrice(100.0*i)
                    .build();

            order = orderRepository.insert(order);
        }
    }

    @Test
    public void testGetOrder() {
//        List<Order> orderList = orderRepository.findByStatusAndQuantityGreaterThanOrderByCreatedAtDesc("READY", 2);
//        List<Order> orderList = orderRepository.findOrdersByStatusAndPrice("READY", 10);

        Pageable pageRequest = PageRequest.of(1, 5, Sort.by(Sort.Direction.DESC, "totalPrice"));
        List<Order> orderList = orderRepository.findAll(pageRequest).toList();

        orderList.forEach(System.out::println);
    }

    @Test
    public void deleteOrder() {
//        List<Order> orderList = orderRepository.findOrdersByStatusAndPrice("READY", 10);
//        orderList.forEach(System.out::println);
//        orderRepository.deleteAll(orderList);
        List<Order>  orderList = orderRepository.findOrdersByStatusAndPrice("READY", 10);
        orderList.forEach(System.out::println); // should be empty
    }
}