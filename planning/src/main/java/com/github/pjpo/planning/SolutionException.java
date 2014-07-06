package com.github.pjpo.planning;

public class SolutionException extends Exception {

	/** Serial id */
	private static final long serialVersionUID = 6071645435437368821L;

	public SolutionException() { }

	public SolutionException(final String message) {
		super(message);
	}

	public SolutionException(final Throwable cause) {
		super(cause);
	}

	public SolutionException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public SolutionException(final String message, final Throwable cause,
			final boolean enableSuppression, final boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
