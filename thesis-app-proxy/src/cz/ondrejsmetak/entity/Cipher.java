package cz.ondrejsmetak.entity;

/**
 * Cipher used in SSL/TLS communication
 *
 * @author Ondřej Směták <posta@ondrejsmetak.cz>
 */
public class Cipher {

	/**
	 * Hexadicaml code by RFC standard
	 */
	private Hex hexCode;

	/**
	 * Name by RFC standard
	 */
	private String name;

	public Cipher(Hex hexCode, String name) {
		this.hexCode = hexCode;
		this.name = name;
	}

	public Cipher(String hexCode, String name) {
		this.hexCode = new Hex(hexCode);
		this.name = name;
	}

	public Hex toHex() {
		return hexCode;
	}

	@Override
	public String toString() {
		return name;
	}
}
