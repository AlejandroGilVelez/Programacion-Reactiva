package com.example;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class MultiThreading {

    // Tarea que realiza una operación costosa: calcular el cuadrado de un número
    static class SquareCalculator implements Callable<Integer> {
        private final int number;

        public SquareCalculator(int number) {
            this.number = number;
        }

        @Override
        public Integer call() throws Exception {
            Thread.sleep(500); // Simula una operación costosa (500ms)
            return number * number;
        }
    }

    public static void main(String[] args) {
        // Crear una lista de números a procesar
        List<Integer> numbers = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            numbers.add(i);
        }

        // Medir tiempo de ejecución secuencial
        long startSequential = System.nanoTime();
        processSequential(numbers);
        long endSequential = System.nanoTime();
        System.out.printf("Tiempo de ejecución secuencial: %.2f segundos%n",
                (endSequential - startSequential) / 1_000_000_000.0);

        // Medir tiempo de ejecución con multi-threading
        long startParallel = System.nanoTime();
        processParallel(numbers);
        long endParallel = System.nanoTime();
        System.out.printf("Tiempo de ejecución paralelo (multi-threading): %.2f segundos%n",
                (endParallel - startParallel) / 1_000_000_000.0);
    }

    // Procesamiento secuencial
    private static void processSequential(List<Integer> numbers) {
        System.out.println("Procesamiento secuencial:");
        for (int number : numbers) {
            try {
                Thread.sleep(500); // Simula la misma operación costosa
                int result = number * number;
                System.out.printf("El cuadrado de %d es %d%n", number, result);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    // Procesamiento paralelo usando ExecutorService
    private static void processParallel(List<Integer> numbers) {
        System.out.println("Procesamiento paralelo:");
        ExecutorService executor = Executors.newFixedThreadPool(4); // Pool de 4 hilos
        List<Future<Integer>> futures = new ArrayList<>();

        // Enviar tareas al pool
        for (int number : numbers) {
            futures.add(executor.submit(new SquareCalculator(number)));
        }

        // Recoger resultados
        for (int i = 0; i < futures.size(); i++) {
            try {
                int result = futures.get(i).get();
                System.out.printf("El cuadrado de %d es %d%n", numbers.get(i), result);
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        executor.shutdown(); // Apagar el ExecutorService
    }
}
