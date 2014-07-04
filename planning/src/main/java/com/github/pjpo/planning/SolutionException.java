package com.github.pjpo.planning;

public class SolutionException extends Exception {

	/** Serial id */
	private static final long serialVersionUID = 6071645435437368821L;

	public SolutionException() { }

	public SolutionException(String message) {
		super(message);
	}

	public SolutionException(Throwable cause) {
		super(cause);
	}

	public SolutionException(String message, Throwable cause) {
		super(message, cause);
	}

	public SolutionException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
