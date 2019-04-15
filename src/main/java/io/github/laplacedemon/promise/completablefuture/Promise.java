package io.github.laplacedemon.promise.completablefuture;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;


public class Promise {
	private CompletableFuture<Object> future;
	private Consumer<Object> resolve;
	private Consumer<Object> reject;

	public Promise(BiConsumer<Consumer<Object>, Consumer<Object>> executor) {

		this.future = new CompletableFuture<>();

		this.resolve = (Object value) -> {
			this.future.complete(value);
		};

		this.reject = (Object reason) -> {
			PromiseException promiseException = new PromiseException(reason);
			this.future.completeExceptionally(promiseException);
		};

		CompletableFuture.runAsync(() -> {
			executor.accept(this.resolve, this.reject);
		});
	}

	private Promise() {
	}

	public Promise then(final Function<Object, Object> onResolved) {
		// this.completableFuture.thenAccept(action);
		CompletableFuture<Object> newFuture = this.future.thenApplyAsync((Object value)->{
			if(value instanceof Promise) {
				Promise promiseValue = (Promise)value;
				Promise thenPromise = promiseValue.then(onResolved);
//				CompletableFuture<Object> cf = promiseValue.future.thenApplyAsync(onResolved);
				return thenPromise;
			} else {
				return onResolved.apply(value);
			}
		});
		
		Promise promise = new Promise();
		promise.future = newFuture;
		return promise;
	}

	public Promise then(final Function<Object, Object> onResolved, final Function<Object, Object> onRejected) {
		CompletableFuture<Object> newFuture = this.future.thenApplyAsync((Object value)->{
			if(value instanceof Promise) {
				Promise promiseValue = (Promise)value;
				Promise thenPromise = promiseValue.then(onResolved);
				return thenPromise;
			} else {
				return onResolved.apply(value);
			}
		});
		
		newFuture.exceptionally((Throwable t)->{
			return onRejected.apply(t);
		});
		
		Promise promise = new Promise();
		promise.future = newFuture;
		return promise;
	}
}
