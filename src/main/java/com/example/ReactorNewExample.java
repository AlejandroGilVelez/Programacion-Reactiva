package com.example;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

public class ReactorNewExample {
    public static void main(String[] args) {
        // Lista de productos
        List<String> products = Arrays.asList("Laptop", "Phone", "Tablet", "Monitor");

        // Flux 1: Emitir productos
        Flux<String> productFlux = Flux.fromIterable(products);

        // Flux 2: Emitir precios correspondientes
        Flux<Double> priceFlux = Flux.just(1000.0, 500.0, 300.0, 200.0)
                .delayElements(Duration.ofMillis(300)); // Simular retraso en la obtención de precios

        // Operaciones reactivas
        Flux<String> result = productFlux
            .filter(product -> product.length() > 5) // Filtrar productos con nombre largo
            .map(product -> product.toLowerCase()) // Convertir nombres a minúsculas
            .flatMap(product -> Mono.just("Processed: " + product)
                    .subscribeOn(Schedulers.boundedElastic())) // Procesar en otro hilo
            .zipWith(priceFlux, (product, price) -> product + " costs $" + price) // Combinar productos con precios
            .mergeWith(Flux.just("Note: Special discounts available!")) // Agregar una nota adicional
            .doOnNext(item -> System.out.println("Processing: " + item)) // Registrar el procesamiento
            .onErrorResume(error -> Flux.just("Error: Could not process data")) // Manejar errores
            .subscribeOn(Schedulers.parallel()); // Ejecutar en paralelo

        // Suscribirse al flujo
        result.subscribe(
            item -> System.out.println("Received: " + item),
            error -> System.out.println("Error: " + error.getMessage()),
            () -> System.out.println("Processing completed")
        );

        // Mantener el hilo principal vivo para procesar eventos asíncronos
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
