# Taller sobre Operadores de Programación Reactiva

## Objetivo

El objetivo es presentar la solución de unos puntos del taller.

## Paso 1: Configuración del Proyecto

### Configuración del `pom.xml` para RxJava

Asegúrate de tener las siguientes dependencias en tu archivo `pom.xml`:

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.example</groupId>
    <artifactId>reactive-operators</artifactId>
    <version>1.0-SNAPSHOT</version>

    <dependencies>
        <!-- RxJava -->
        <dependency>
            <groupId>io.reactivex.rxjava2</groupId>
            <artifactId>rxjava</artifactId>
            <version>2.2.21</version>
        </dependency>

        <!-- Reactor Core -->
        <dependency>
            <groupId>io.projectreactor</groupId>
            <artifactId>reactor-core</artifactId>
            <version>3.4.11</version>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.8.1</version>
                <configuration>
                    <source>1.8</source>
                    <target>1.8</target>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

## Ejemplos de Operadores en RxJava

### 1. 

```java
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
```

## Ejemplos de Operadores en Reactor
### 2. 

```java

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


```


##  Ejemplos de MultiThreading

### 1. 

```java

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


```

### 2. 

```java
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


```


### Conclusión

Se realizan ejemplos comprendiendo la programación reactiva.
