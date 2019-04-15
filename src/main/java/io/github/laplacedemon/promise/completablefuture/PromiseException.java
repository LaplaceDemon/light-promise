package io.github.laplacedemon.promise.completablefuture;

public class PromiseException extends RuntimeException {
	private static final long serialVersionUID = -9126949050871104675L;
	
	private Object reason;
	
	public PromiseException(Object reason) {
		this.reason = reason;
	}

	public Object getReason() {
		return reason;
	}
	
}
