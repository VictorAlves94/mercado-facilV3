package com.mercadofacil;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * MercadoFácil — Sistema de Gestão para Pequenos Mercados
 * Versão 1.0.0
 */
@SpringBootApplication
@EnableScheduling
public class MercadoFacilApplication {
    public static void main(String[] args) {
        SpringApplication.run(MercadoFacilApplication.class, args);
    }
}
