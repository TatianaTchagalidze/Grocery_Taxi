package com.example.grocery_taxi.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

@RestController
public class HealthController {

    @PersistenceContext
    private EntityManager entityManager;

    @GetMapping("/health")
    public String checkHealth() {
        String databaseStatus = checkDatabaseConnection();
        if (databaseStatus.equals("connected")) {
            return "Application is healthy and database is connected";
        } else {
            return "Application is healthy, but unable to connect to the database";
        }
    }

    private String checkDatabaseConnection() {
        try {
            Query query = entityManager.createNativeQuery("SELECT 1");
            query.getSingleResult();
            return "connected";
        } catch (Exception e) {
            return "error: " + e.getMessage();
        }
    }
}
