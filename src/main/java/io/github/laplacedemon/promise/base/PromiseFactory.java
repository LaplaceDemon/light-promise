package io.github.laplacedemon.promise.base;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import io.github.laplacedemon.Promise;

public class PromiseFactory {
    
    private Consumer<Runnable> consumer = (Runnable r)->{
        r.run();
    };
    
    public void submitMethod(Consumer<Runnable> consumer) {
        this.consumer = consumer;
    }
    
    public Promise create(BiConsumer<Consumer<Object>, Consumer<Object>> executor) {
        return new Promise(executor);
    }
}
