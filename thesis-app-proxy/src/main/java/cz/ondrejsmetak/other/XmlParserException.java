package cz.ondrejsmetak.other;

/**
 * Exception thrown when error occured during parsing XML file
 *
 * @author Ondřej Směták <posta@ondrejsmetak.cz>
 */
public class XmlParserException extends Exception {

	/**
	 * Create a new exception with given message
	 *
	 * @param message message of exception
	 */
	public XmlParserException(String message) {
		super(message);
	}

	/**
	 * Create a new exception with given message. Message is passed just like as
	 * <code>java.lang.String.format</code> method
	 *
	 * @param format format of message
	 * @param args arguments referenced by the format specifiers in the format
	 */
	public XmlParserException(String format, Object... args) {
		super(String.format(format, args));
	}

	/**
	 * Create a new exception with given message and cause
	 *
	 * @param message message of exception
	 * @param cause cause of exception
	 */
	public XmlParserException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Create a new exception with given cause
	 *
	 * @param cause cause of exception
	 */
	public XmlParserException(Throwable cause) {
		super(cause.getMessage(), cause);
	}

}
