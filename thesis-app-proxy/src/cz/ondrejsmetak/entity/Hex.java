package cz.ondrejsmetak.entity;

import cz.ondrejsmetak.tool.Helper;
import java.util.Objects;

/**
 * Hexadecial value
 *
 * @author Ondřej Směták <posta@ondrejsmetak.cz>
 */
public class Hex {

	private String data;
	private int paddingToLength = 0; //kolik "cifer" bude mit sestnactkova reprezentace

	/**
	 * Creation from value of (one) byte
	 *
	 * @param oneByte byte value represented as integer
	 */
	public Hex(int oneByte) {
		this(Helper.decToHex(oneByte), 1);
	}

	public Hex(byte... bytes) {
		this(Helper.toHexString(bytes));
	}

	public Hex(String data) {
		String input = normalizeInput(data);
		checkLength(input);

		this.paddingToLength = input.length();
		if (Helper.isEven(input.length())) {
			this.paddingToLength++; //odd count will be padded on even count
		}
		this.data = createToString(input, paddingToLength);

	}

	/**
	 * Creation from direct hexadeciamal value written asi string
	 *
	 * @param data hexadecimal value, n digits allowed
	 * @param paddingToBytesLength padding to how many digits
	 */
	public Hex(String data, int paddingToBytesLength) {
		String input = normalizeInput(data);
		checkLength(input);

		this.paddingToLength = paddingToBytesLength * 2;
		this.data = createToString(input, paddingToLength);
	}

	private void checkLength(String data) {
		if (data.isEmpty()) {
			throw new IllegalArgumentException("Length of input hex representation must be non-zero.");
		}
	}

	/**
	 * How many bytes is needed for storing this hexadecimal value
	 *
	 * @return quantity of bytes needed for storing
	 */
	public int getLengthInDec() {
		return this.data.length() / 2;
	}

	/**
	 * How many bytes (in hexadecimal value) is needed for storing this
	 * hexadecimal value
	 *
	 * @return hexadecimal quantity of bytes needed for storing
	 */
	public String getLengthInHex() {
		return Helper.decToHex(getLengthInDec());
	}

	/**
	 * How many digits has this hexadecimal value
	 * @return 
	 */
	public int getDigits(){
		return this.data.length();
	}
	
	private String createToString(String data, int paddingToLength) {
		return String.format("%1$" + paddingToLength + "s", data).replace(' ', '0');
	}

	private String normalizeInput(String input) {
		input = input.replaceAll(",", "");
		input = input.replaceAll("0x", "");
		input = input.replaceAll(" ", "");
		return input;
	}

	@Override
	public String toString() {
		return createToString(data, paddingToLength);
	}

	/**
	 * Human readable representation
	 *
	 * @return
	 */
	public String toStringHuman() {
		return "0x" + toString();
	}

	/**
	 * Join this hexadecimal value with another hexadecimal value
	 *
	 * @param hex
	 * @return
	 */
	public Hex join(Hex hex) {
		this.data = this.data + hex.data;
		this.paddingToLength = this.paddingToLength + hex.paddingToLength;
		return this;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 97 * hash + Objects.hashCode(this.data);
		hash = 97 * hash + this.paddingToLength;
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
		final Hex other = (Hex) obj;
		if (!Objects.equals(this.data, other.data)) {
			return false;
		}
		return true;
	}
}
