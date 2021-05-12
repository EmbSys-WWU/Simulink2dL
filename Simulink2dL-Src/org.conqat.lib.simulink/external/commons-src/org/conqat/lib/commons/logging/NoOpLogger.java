/*-------------------------------------------------------------------------+
|                                                                          |
| Copyright (c) 2009-2017 CQSE GmbH                                        |
|                                                                          |
+-------------------------------------------------------------------------*/
package org.conqat.lib.commons.logging;

/**
 * Logger implementation that does not log anything.
 */
public class NoOpLogger implements ILogger {

	/** {@inheritDoc} */
	@Override
	public void debug(Object message) {
		// do nothing
	}

	/** {@inheritDoc} */
	@Override
	public void debug(Object message, Throwable throwable) {
		// do nothing
	}

	/** {@inheritDoc} */
	@Override
	public void info(Object message) {
		// do nothing
	}

	/** {@inheritDoc} */
	@Override
	public void info(Object message, Throwable throwable) {
		// do nothing
	}

	/** {@inheritDoc} */
	@Override
	public void warn(Object message) {
		// do nothing
	}

	/** {@inheritDoc} */
	@Override
	public void warn(Object message, Throwable throwable) {
		// do nothing
	}

	/** {@inheritDoc} */
	@Override
	public void error(Object message) {
		// do nothing
	}

	/** {@inheritDoc} */
	@Override
	public void error(Object message, Throwable throwable) {
		// do nothing
	}

}
