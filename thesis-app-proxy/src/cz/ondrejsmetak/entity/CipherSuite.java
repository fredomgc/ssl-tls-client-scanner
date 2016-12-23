package cz.ondrejsmetak.entity;

import java.util.Objects;

/**
 * CipherSuite used in SSL/TLS communication
 *
 * @author Ondřej Směták <posta@ondrejsmetak.cz>
 */
public class CipherSuite {

	/**
	 * Hexadecimal code (by RFC standard)
	 */
	private Hex hexCode;

	private String name;

	private Mode mode;

	public CipherSuite(Hex hexCode, String name, Mode mode) {
		this.hexCode = hexCode;
		this.name = name;
		this.mode = mode;
	}

	public CipherSuite(String hexCode, String name, Mode mode) {
		this.hexCode = new Hex(hexCode);
		this.name = name;
		this.mode = mode;
	}

	public Hex getHex() {
		return hexCode;
	}

	public String getName() {
		return name;
	}

	public Mode getMode() {
		return mode;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(name);
		sb.append(" ").append("(").append(hexCode.toStringHuman()).append(")");
		return sb.toString();
	}

	@Override
	public int hashCode() {
		int hash = 5;
		hash = 29 * hash + Objects.hashCode(this.hexCode);
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
		final CipherSuite other = (CipherSuite) obj;
		if (!Objects.equals(this.hexCode, other.hexCode)) {
			return false;
		}
		return true;
	}
	
	
}
