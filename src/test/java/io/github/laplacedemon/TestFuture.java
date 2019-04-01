package io.github.laplacedemon;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.junit.Test;

public class TestFuture {

    @Test
    public void testCompletableFuture() throws InterruptedException, ExecutionException {
        CompletableFuture<String> future = CompletableFuture.supplyAsync(()->{
            System.out.println("带有返回值的异步任务");
            return "a future value";
        });
        
        System.out.println(future.get());
    }
}
