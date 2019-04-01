package io.github.laplacedemon.promise.base;

import java.util.LinkedList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import io.github.laplacedemon.State;

public class PromiseBase {
    private State status;
    private Object value;
    private Object reason;
    private List<Function<Object, Object>> resolvedCallbackList;
    private List<Function<Object, Object>> rejectCallbackList;

    private Consumer<Object> resolve;
    private Consumer<Object> reject;
    
    private void initExecutor(BiConsumer<Consumer<Object>, Consumer<Object>> executor) {
        executor.accept(this.resolve, this.reject);
    }

    private PromiseBase() {
        this.status = State.Pending;
        this.resolvedCallbackList = new LinkedList<>();
        this.rejectCallbackList = new LinkedList<>();
        
        this.resolve = (Object value) -> {
            if (this.status.equals(State.Pending)) {
                this.value = value;
                this.status = State.Fulfilled;
                for (Function<Object, Object> fn : this.resolvedCallbackList) {
                    fn.apply(value);
                }
            }
        };

        this.reject = (Object reason) -> {
            if (this.status.equals(State.Pending)) {
                this.reason = reason;
                this.status = State.Rejected;
                for (Function<Object, Object> fn : this.rejectCallbackList) {
                    fn.apply(reason);
                }
            }
        };
    }

    public PromiseBase(BiConsumer<Consumer<Object>, Consumer<Object>> executor) {
        this();
        initExecutor(executor);
    }

    public PromiseBase then(final Function<Object, Object> onResolved, final Function<Object, Object> onRejected) {
        PromiseBase self = this;
        final PromiseBase newPromise = new PromiseBase();

        if (self.status.equals(State.Fulfilled)) {
            // 当前以及完成, 成功
            newPromise.initExecutor((resolve, reject) -> {
                try {
                    Object x = onResolved.apply(self.value);
                    resolvePromise(newPromise, x, resolve, reject);
                } catch (Exception reason) {
                    self.reject.accept(reason);
                }
            });
        } else if (self.status.equals(State.Rejected)) {
            // 当前以及完成, 失败
            newPromise.initExecutor((resolve, reject) -> {
                try {
                    Object x = onResolved.apply(self.reason);
                    resolvePromise(newPromise, x, resolve, reject);
                } catch (Exception reason) {
                    self.reject.accept(reason);
                }
            });

        } else if (self.status.equals(State.Pending)) {
            // 当前尚未运行
            newPromise.initExecutor((resolve, reject) -> {
                self.resolvedCallbackList.add((Object v) -> {
                    try {
                        Object x = onResolved.apply(v);
                        resolvePromise(newPromise, x, resolve, reject);
                    } catch (Exception reason) {
                        self.reject.accept(reason);
                    }
                    
                    return null;
                });

                self.rejectCallbackList.add((Object r) -> {
                    try {
                        Object x = onResolved.apply(r);
                        resolvePromise(newPromise, x, resolve, reject);
                    } catch (Exception reason) {
                        self.reject.accept(reason);
                    }
                    
                    return null;
                });
            });
        }

        return newPromise;
    }

    private final static void resolvePromise(PromiseBase promise, Object result, final Consumer<Object> resolve, final Consumer<Object> reject) {
        if (promise == result) {
            reject.accept(new IllegalArgumentException("loop ref"));
            return ;
        }

        if (result instanceof PromiseBase) {
            PromiseBase thenPromise = (PromiseBase) result;
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
