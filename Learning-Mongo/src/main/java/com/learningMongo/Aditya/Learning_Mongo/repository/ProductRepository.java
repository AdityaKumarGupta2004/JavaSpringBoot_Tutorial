package com.learningMongo.Aditya.Learning_Mongo.repository;

import com.learningMongo.Aditya.Learning_Mongo.entity.Product;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ProductRepository extends MongoRepository<Product,String> {
}
