package com.criffacademy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;

@SpringBootApplication
public class Application {
    
    @Autowired
    private Server server;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @PostConstruct
    public void init() {
        server.startServer();
    }
}
