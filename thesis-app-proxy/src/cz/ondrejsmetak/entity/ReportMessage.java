package cz.ondrejsmetak.entity;

/**
 * Represents one report message
 *
 * @author Ondřej Směták <posta@ondrejsmetak.cz>
 */
public class ReportMessage {

	/**
	 * Category of message
	 */
	public enum Category {
		PROTOCOL, VULNERABILITY, CERTIFICATE, CIPHER
	}

	/**
	 * Type of message
	 */
	public enum Type {
		ERROR, SUCCESS
	}

	/**
	 * Body of message
	 */
	private String message;
	/**
	 * Category of this message
	 */
	private Category category;
	/**
	 * Type of this message
	 */
	private Type type;
	/**
	 * Mode, that was used to determine this message
	 */
	private Mode requiredMode;

	/**
	 * Creates new success message with given attributes
	 * 
	 * @param message body of message
	 * @param category category of message
	 * @param requiredMode mode used to determine this message
	 */
	public ReportMessage(String message, Category category, Mode requiredMode) {
		this.message = message;
		this.category = category;
		this.requiredMode = requiredMode;
		this.type = Type.ERROR;
	}

	/**
	 * Creates new message with given attributes
	 * 
	 * @param message body of message
	 * @param category category of message
	 * @param requiredMode mode used to determine this message
	 * @param type type of message
	 */
	public ReportMessage(String message, Category category, Mode requiredMode, Type type) {
		this.message = message;
		this.category = category;
		this.requiredMode = requiredMode;
		this.type = type;

	}

	public String getMessage() {
		return message;
	}

	public Category getCategory() {
		return category;
	}

	public Type getType() {
		return type;
	}

	public Mode getRequiredMode() {
		return requiredMode;
	}

	/**
	 * Returs used mode in human readable form
	 * @return mode in human readable form
	 */
	public String getRequiredModeHuman() {
		if (requiredMode.isCanBe()) {
			return "can be";
		}

		if (requiredMode.isMustBe()) {
			return "must be";
		}

		if (requiredMode.isMustNotBe()) {
			return "must not be";
		}

		return "-";
	}
}
