package io.github.laplacedemon;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestPromise {
    private ScheduledExecutorService se;

    @Before
    public void begin() throws InterruptedException {
        se = Executors.newScheduledThreadPool(1);
    }

    @After
    public void after() throws InterruptedException {
        se.awaitTermination(1, TimeUnit.DAYS);
    }

    @Test
    public void testFlunt() {
        System.out.println("start task.");
         
        Promise promise = new Promise((resolve, reject) -> {
            se.schedule(() -> {
                resolve.accept(123);
            }, 1, TimeUnit.SECONDS);
        })
        .then((Object value) -> {
            System.out.println("value1 " + value);
            return null;
        }, (Object res) -> {
            return null;
        })
        .then((Object value) -> {
            return new Promise((resolve, reject) -> {
                resolve.accept(456);
            });
        }, null)
        .then((Object value) -> {
            return new Promise((resolve, reject) -> {
                resolve.accept(789);
            });
        }, null)
        .then((Object value) -> {
            System.out.println("value2 " + value);
            return null;
        }, null);
    }
}
