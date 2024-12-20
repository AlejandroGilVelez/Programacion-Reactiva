package com.example;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class MultiThreadingComparativo {

    // Tarea que realiza una operación costosa: calcular el cuadrado de un número
    static class SquareCalculator implements Callable<Integer> {
        private final int number;

        public SquareCalculator(int number) {
            this.number = number;
        }

        @Override
        public Integer call() throws Exception {
            // Simulamos una operación costosa
            Thread.sleep(500); // Pausa de 500ms
            return number * number;
        }
    }

    public static void main(String[] args) {
        // 1. Crear una lista de números que queremos procesar
        List<Integer> numbers = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            numbers.add(i);
        }

        // 2. Crear un ExecutorService con un pool de 4 hilos
        ExecutorService executor = Executors.newFixedThreadPool(4);

        // 3. Crear una lista de tareas (Callables)
        List<Future<Integer>> futures = new ArrayList<>();
        for (int number : numbers) {
            SquareCalculator task = new SquareCalculator(number);
            Future<Integer> future = executor.submit(task);
            futures.add(future);
        }

        // 4. Recoger y mostrar los resultados
        System.out.println("Resultados del procesamiento paralelo:");
        for (int i = 0; i < futures.size(); i++) {
            try {
                Integer result = futures.get(i).get(); // Obtener el resultado del Future
                System.out.printf("El cuadrado de %d es %d%n", numbers.get(i), result);
            } catch (InterruptedException | ExecutionException e) {
                System.err.println("Error al procesar la tarea: " + e.getMessage());
            }
        }

        // 5. Apagar el ExecutorService
        executor.shutdown();
        System.out.println("Procesamiento completado.");
    }
}
