package cz.ondrejsmetak.tool;

import cz.ondrejsmetak.ConfigurationRegister;

/**
 * Abstraction for printing messages to console
 *
 *
 * @author Ondřej Směták <posta@ondrejsmetak.cz>
 */
public class Log {

	/**
	 * Prints given info message
	 *
	 * @param message message, that will be printeds
	 */
	public static void info(Object message) {
		System.out.print(message);
	}

	/**
	 * Prints given info message and symbol of new line
	 *
	 * @param message message, that will be printeds
	 */
	public static void infoln(Object message) {
		System.out.println(message);
	}

	public static void infoln(String format, Object... args) {
		infoln(String.format(format, args));
	}

	/**
	 * Prints given error message
	 *
	 * @param message message, that will be printeds
	 */
	public static void error(Object message) {
		System.out.print("! ERROR: " + message);
	}

	/**
	 * Prints given error message and symbol of new line
	 *
	 * @param message message, that will be printeds
	 */
	public static void errorln(Object message) {
		System.out.println("! ERROR: " + message);
	}
	
	public static void errorln(String format, Object... args) {
		errorln(String.format(format, args));
	}

	/**
	 * Prints given warning message
	 *
	 * @param message message, that will be printeds
	 */
	public static void warning(Object message) {
		System.out.print("WARNING: " + message);
	}

	/**
	 * Prints given warning message and symbol of new line
	 *
	 * @param message message, that will be printeds
	 */
	public static void warningln(Object message) {
		System.out.println("WARNING: " + message);
	}

	public static void warningln(String format, Object... args) {
		warningln(String.format(format, args));
	}
	
	
	/**
	 * Print exception for debug purpose
	 *
	 * @param exception exception, that was thrown
	 */
	public static void debugException(Throwable exception) {
		if (ConfigurationRegister.getInstance().isDebug()) {
			System.out.println("DEBUG: " + exception.toString());
			exception.printStackTrace();
		}
	}
}
