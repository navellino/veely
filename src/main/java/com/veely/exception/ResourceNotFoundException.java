package com.veely.exception;

public class ResourceNotFoundException extends RuntimeException {
    /**
	 * 
	 */
	private static final long serialVersionUID = -877933646869176203L;

	public ResourceNotFoundException(String msg) { super(msg); }
}
