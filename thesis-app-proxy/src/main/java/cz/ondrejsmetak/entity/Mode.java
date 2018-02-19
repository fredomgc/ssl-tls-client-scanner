package cz.ondrejsmetak.entity;

import java.util.Objects;

/**
 * Represents mode Mode alters behaviour of application's various features
 *
 * @author Ondřej Směták <posta@ondrejsmetak.cz>
 */
public class Mode {

	/**
	 * Type of mode
	 */
	public enum Type {
		MUST_BE, MUST_NOT_BE, CAN_BE
	}

	/**
	 * Type of this mode
	 */
	private Type type = null;

	/**
	 * Text representation of all types of mode
	 */
	private static final String MUST_BE_STR = "mustBe";
	private static final String MUST_NOT_BE_STR = "mustNotBe";
	private static final String CAN_BE_STR = "canBe";

	/**
	 * Creates new mode with given type
	 *
	 * @param type type of mode
	 */
	public Mode(Type type) {
		this.type = type;
	}

	/**
	 * Creates new mode with given type, that is passed in it's textual form
	 *
	 * @param type textual representation of type
	 */
	public Mode(String type) {
		if (type.equals(MUST_BE_STR)) {
			this.type = Type.MUST_BE;
		}

		if (type.equals(MUST_NOT_BE_STR)) {
			this.type = Type.MUST_NOT_BE;
		}

		if (type.equals(CAN_BE_STR)) {
			this.type = Type.CAN_BE;
		}

		if (this.type == null) {
			throw new IllegalArgumentException(String.format("Unknown type of mode [%s]!", type));
		}
	}

	@Override
	public String toString() {
		if(type.equals(Type.MUST_BE)){
			return "MUST BE";
		}
		
		if(type.equals(Type.MUST_NOT_BE)){
			return "MUST NOT BE";
		}
		
		if(type.equals(Type.CAN_BE)){
			return "CAN BE";
		}
		
		return "UNKNOWN";
	}

	public Mode.Type getType() {
		return type;
	}

	/**
	 * Equals type of this mode to "mustBe" or "mustNotBe"? Usefull shortcut.
	 *
	 * @return true, if type equals to "mustBe" or "mustNotBe", false otherwise
	 */
	public boolean isMustBeOrMustNotBe() {
		return type == Type.MUST_BE || type == Type.MUST_NOT_BE;
	}

	/**
	 * Equals type of this mode to "mustBe"? Usefull shortcut.
	 *
	 * @return true, if type equals to "mustBe", false otherwise
	 */
	public boolean isMustBe() {
		return type == Type.MUST_BE;
	}

	/**
	 * Equals type of this mode to "mustNotBe"? Usefull shortcut.
	 *
	 * @return true, if type equals to "mustNotBe", false otherwise
	 */
	public boolean isMustNotBe() {
		return type == Type.MUST_NOT_BE;
	}

	/**
	 * Equals type of this mode to "canBe"? Usefull shortcut.
	 *
	 * @return true, if type equals to "canBe", false otherwise
	 */
	public boolean isCanBe() {
		return type == Type.CAN_BE;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 89 * hash + Objects.hashCode(this.type);
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Mode other = (Mode) obj;
		if (this.type != other.type) {
			return false;
		}
		return true;
	}

}