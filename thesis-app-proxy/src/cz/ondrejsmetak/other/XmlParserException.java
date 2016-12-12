package cz.ondrejsmetak.other;

/**
 *
 * @author Ondřej Směták <posta@ondrejsmetak.cz>
 */
public class XmlParserException extends Exception {

	public XmlParserException(String message) {
		super(message);
	}

	public XmlParserException(String format, Object... args) {
		super(String.format(format, args));
	}
	
	public XmlParserException(String message, Throwable cause) {
		super(message, cause);
	}

	public XmlParserException(Throwable cause) {
		super(cause.getMessage(), cause);
	}

}
