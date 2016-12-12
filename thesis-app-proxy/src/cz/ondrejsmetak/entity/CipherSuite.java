package cz.ondrejsmetak.entity;

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

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(name);
		sb.append(" ").append("(").append(hexCode.toStringHuman()).append(")");
		return sb.toString();
	}
}
