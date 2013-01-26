package controllers;

import play.Logger;

final public class AppLogger {

	final static private String LOGGER_PREFIX = "[App] ";

	static public void info(final String msg, final Object... args) {
		Logger.info(makeMsg(msg, args));
	}

	static public void warn(final String msg, final Object... args) {
		Logger.warn(makeMsg(msg, args));
	}

	static public void error(final String msg, final Object... args) {
		Logger.error(makeMsg(msg, args));
	}

	static public void error(final Throwable t, final String msg, final Object... args) {
		Logger.error(makeMsg(msg, args), t);
	}

	static public void debug(final String msg, final Object... args) {
		Logger.debug(makeMsg(msg, args));
	}

	static public void debug(final Throwable t, final String msg, final Object... args) {
		Logger.debug(makeMsg(msg, args), t);
	}

	// //

	private AppLogger() {
	}

	static private String makeMsg(final String msg, final Object... args) {
		return LOGGER_PREFIX + String.format(msg, args);
	}
}
