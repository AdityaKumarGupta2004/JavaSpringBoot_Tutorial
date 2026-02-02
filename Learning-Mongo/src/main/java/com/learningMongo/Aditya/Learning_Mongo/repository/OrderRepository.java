package com.learningMongo.Aditya.Learning_Mongo.repository;

import com.learningMongo.Aditya.Learning_Mongo.entity.Order;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface OrderRepository extends MongoRepository<Order,String> {

    List<Order> findOrderByStatusAndQuantityGreaterThan(String status, Integer quantityIsGreaterThan);
    List<Order> findOrderByStatusAndQuantityGreaterThanOrderByCreatedAtDesc(String status, Integer quantityIsGreaterThan);

    @Query("{ 'status': ?0, 'totalPrice': { $gte: ?1 } }")
    List<Order> findOrdersByStatusAndPrice(String status, double minPrice);


    List<Order> findByAddressCity(String  city);

    @Query(value = "{ 'address.city' : ?0 }", fields = "{ '_id': 1, 'quantity': 1 }")
    List<Order> findByCity(String city);
}
