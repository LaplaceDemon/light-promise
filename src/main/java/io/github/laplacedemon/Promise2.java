package io.github.laplacedemon;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public class Promise2 {
    private AtomicReference<State> status;
    private Object value;
    private Object reason;
    private List<Function<Object, Object>> resolvedCallbackList;
    private List<Function<Object, Object>> rejectCallbackList;

    private Consumer<Object> resolve;
    private Consumer<Object> reject;
    
    private final ExecutorService es = Executors.newFixedThreadPool(10);
    
    private Consumer<Runnable> submitMethod = (Runnable r)-> {
        es.submit(r);
    };
    
    private void initExecutor(BiConsumer<Consumer<Object>, Consumer<Object>> executor) {
        executor.accept(this.resolve, this.reject);
    }

    private Promise2() {
        this.status.set(State.Pending);
        this.resolvedCallbackList = new LinkedList<>();
        this.rejectCallbackList = new LinkedList<>();
        
        this.resolve = (Object value) -> {
            if (this.status.compareAndSet(State.Pending, State.Fulfilled)) {
                this.value = value;
                for (Function<Object, Object> fn : this.resolvedCallbackList) {
                    fn.apply(value);
                }
            }
        };

        this.reject = (Object reason) -> {
            if (this.status.compareAndSet(State.Pending, State.Rejected)) {
                this.reason = reason;
                for (Function<Object, Object> fn : this.rejectCallbackList) {
                    fn.apply(reason);
                }
            }
        };
    }

    public Promise2(BiConsumer<Consumer<Object>, Consumer<Object>> executor) {
        this();
        initExecutor(executor);
    }

    public Promise2 then(final Function<Object, Object> onResolved, final Function<Object, Object> onRejected) {
    	Promise2 self = this;
        final Promise2 newPromise = new Promise2();
        
        State currentState = self.status.get();
        
        if (currentState.equals(State.Fulfilled)) {
            submitMethod.accept(() -> {
                // 当前以及完成, 成功
                newPromise.initExecutor((resolve, reject) -> {
                    try {
                        Object result = onResolved.apply(self.value);
                        resolvePromise(newPromise, result, resolve, reject);
                    } catch (Exception reason) {
                        self.reject.accept(reason);
                    }
                });
            });
            
        } else if (currentState.equals(State.Rejected)) {
            submitMethod.accept(() -> {
                // 当前以及完成, 失败
                newPromise.initExecutor((resolve, reject) -> {
                    try {
                        Object result = onResolved.apply(self.reason);
                        resolvePromise(newPromise, result, resolve, reject);
                    } catch (Exception reason) {
                        self.reject.accept(reason);
                    }
                });
            });
        } else if (currentState.equals(State.Pending)) {
            // 当前尚未运行
            newPromise.initExecutor((resolve, reject) -> {
                self.resolvedCallbackList.add(
                        
                    (Object v) -> {
                    try {
                        Object result = onResolved.apply(v);
                        resolvePromise(newPromise, result, resolve, reject);
                    } catch (Exception reason) {
                        self.reject.accept(reason);
                    }
                    
                    return null;
                });

                self.rejectCallbackList.add((Object r) -> {
                    try {
                        Object result = onResolved.apply(r);
                        resolvePromise(newPromise, result, resolve, reject);
                    } catch (Exception reason) {
                        self.reject.accept(reason);
                    }
                    
                    return null;
                });
            });
        }

        return newPromise;
    }

    private final static void resolvePromise(Promise2 promise, Object result, final Consumer<Object> resolve, final Consumer<Object> reject) {
        if (promise == result) {
            reject.accept(new IllegalArgumentException("loop ref"));
            return ;
        }

        if (result instanceof Promise2) {
        	Promise2 thenPromise = (Promise2) result;
            try {
                thenPromise.then((y) -> {
                    resolvePromise(promise, y, resolve, reject);
                    return null;
                }, (error) -> {
                    reject.accept(error);
                    return null;
                });
            } catch (Exception error) {
                reject.accept(error);
            }
        } else {
            resolve.accept(result);
        }
    }
}
