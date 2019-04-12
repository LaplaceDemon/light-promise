package io.github.laplacedemon.promise.completablefuture;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import io.github.laplacedemon.State;
import io.github.laplacedemon.promise.base.PromiseBase;

public class Promise {
    private CompletableFuture<Void> completableFuture;
    private Consumer<Object> resolve;
    private Consumer<Object> reject;
    
    public Promise(BiConsumer<Consumer<Object>, Consumer<Object>> executor) {
        this.resolve = (Object value) -> {
            this.completableFuture.completedFuture(value);
        };

        this.reject = (Object reason) -> {
            this.completableFuture.completedFuture(reason);
        };
        
        CompletableFuture.runAsync(()->{
            executor.accept(this.resolve, this.reject);
        });
    }
    
    public PromiseBase then(final Function<Object, Object> onResolved, final Function<Object, Object> onRejected) {
        this.completableFuture.thenAccept(action);
    }
}
