package io.github.laplacedemon;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import io.github.laplacedemon.promise.completablefuture.Promise;

public class PromiseBaseOnCompletableFuture {
    private static ScheduledExecutorService se;
    
    static {
        se = Executors.newScheduledThreadPool(1);
    }
    
//    @BeforeClass
//    public void init() {
//        
//    }
    
    @After
    public void after() throws InterruptedException {
        se.awaitTermination(1, TimeUnit.DAYS);
    }

    @Test
    public void testFlunt() {
        System.out.println("start task.");
         
        Promise promise = 
                
        // 当前线程
        new Promise((resolve, reject) -> {
            // 当前线程
            se.schedule(() -> {
                resolve.accept(123);
            }, 1, TimeUnit.SECONDS);
        })
        
        // 当前线程
        .then((Object value) -> {
            // 异步线程
            System.out.println("value1 " + value);
            return null;
        })
        
        // 当前线程
        .then((Object value) -> {
         // 异步线程
            return new Promise((resolve, reject) -> {
//                resolve.accept(456);
            	 // 当前线程
                se.schedule(() -> {
                	System.out.println("value2 " + value);
                    resolve.accept(456);
                }, 10, TimeUnit.SECONDS);
            });
        })
        
        .then((Object value) -> {
            // 异步线程
            return new Promise((resolve, reject) -> {
                resolve.accept(789);
            });
        })
        
        // 当前线程
        .then((Object value) -> {
            // 异步线程
            System.out.println("value3 " + value);
            return null;
        });
        
    }
    
    
    @Test
    public void testPromise() {
        Function<Object, Object> multiply = (Object value) -> {
            Integer input = (Integer)value;
            return new Promise((resolve, reject) -> {
                log("calculating " + input + " x " + input + "...");
                setTimeout(resolve, 500, input * input);
            });
        };
        
        Function<Object, Object> add = (Object value) -> {
            Integer input = (Integer)value;
            return new Promise((resolve, reject) -> {
                log("calculating " + input + " x " + input + "...");
                setTimeout(resolve, 500, input + input);
            });
        };
        
        Promise p = new Promise((resolve, reject) -> {
            System.out.println("start new Promise...");
            resolve.accept(123);            
        });
        
        p.then(multiply).then(add).then(multiply).then(add).then((Object result)->{
            log("Got value: " + result);
            return null; 
        });
        
    }
    
    public static void log(String str) {
        System.out.println(str);
    }
    
    public static void setTimeout(final Consumer<Object> funConsumer, long timeout, final Object param) {
        se.schedule(() -> {
            funConsumer.accept(param);
        }, timeout, TimeUnit.MILLISECONDS);
    }
    
}
