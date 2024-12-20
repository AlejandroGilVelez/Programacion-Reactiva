package com.example;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class RxJavaNewExample {
    public static void main(String[] args) {
        // Simulamos una lista de usuarios
        List<String> users = Arrays.asList("Alice", "Bob", "Charlie", "Diana");

        // Observable 1: Emite nombres de usuarios
        Observable<String> userObservable = Observable.fromIterable(users);

        // Observable 2: Simula tiempos de respuesta de una API
        Observable<Long> responseTimes = Observable.interval(200, TimeUnit.MILLISECONDS)
                .take(users.size());

        // Operaciones reactivas
        Observable<String> result = userObservable
            .filter(name -> name.startsWith("A") || name.startsWith("B")) // Filtrar usuarios que comienzan con A o B
            .map(name -> name.toUpperCase()) // Convertir los nombres a mayúsculas
            .flatMap(name -> Observable.just("Welcome, " + name + "!")
                    .subscribeOn(Schedulers.io())) // Generar mensajes de bienvenida en otro hilo
            .mergeWith(Observable.just("System: Maintenance scheduled")) // Añadir un mensaje adicional al flujo
            .zipWith(responseTimes, (message, time) -> message + " (Response in " + (time + 1) * 200 + " ms)") // Combinar con tiempos de respuesta
            .doOnNext(message -> System.out.println("Log: " + message)) // Registrar el procesamiento
            .observeOn(Schedulers.computation()) // Procesar en el hilo de cómputo
            .onErrorReturn(throwable -> "Error occurred: " + throwable.getMessage()); // Manejar errores

        // Suscribirse al flujo
        result.subscribe(
            message -> System.out.println("Received: " + message),
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
