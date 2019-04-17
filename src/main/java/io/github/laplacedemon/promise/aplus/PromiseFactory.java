package io.github.laplacedemon.promise.aplus;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class PromiseFactory {
    
    private Consumer<Runnable> consumer = (Runnable r)->{
        r.run();
    };
    
    public void submitMethod(Consumer<Runnable> consumer) {
        this.consumer = consumer;
    }
    
    public PromiseAPlus create(BiConsumer<Consumer<Object>, Consumer<Object>> executor) {
        return new PromiseAPlus(executor);
    }
}
