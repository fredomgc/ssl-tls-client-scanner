package cz.ondrejsmetak;

import cz.ondrejsmetak.entity.CipherSuite;
import cz.ondrejsmetak.entity.Hex;
import cz.ondrejsmetak.entity.Mode;
import java.util.HashMap;

/**
 *
 * @author Ondřej Směták <posta@ondrejsmetak.cz>
 */
public class CipherSuiteRegister {

	private static CipherSuiteRegister instance = null;
	private final HashMap<Hex, CipherSuite> register = new HashMap<>();

	protected CipherSuiteRegister() {
		//no direct instantiation.
	}

	public static CipherSuiteRegister getInstance() {
		if (instance == null) {
			instance = new CipherSuiteRegister();
		}
		return instance;
	}

	public void addCipherSuite(CipherSuite cipherSuite) {
		register.put(cipherSuite.getHex(), cipherSuite);
	}

	public boolean containsCipherSuite(CipherSuite cipherSuite) {
		return register.keySet().contains(cipherSuite.getHex());
	}

	public int getSize() {
		return register.size();
	}

	public CipherSuite getByHexOrCreateDefault(Hex hex) {
		if (hex.getDigits() != 4) {
			throw new IllegalArgumentException("Hexadecimal key must have exactly 4 digits!");
		}

		CipherSuite found = register.get(hex);

		if (found instanceof CipherSuite) {
			return found; //cipher suite found
		}
		
		//no such cipher suite found, so create it
		CipherSuite unknown = new CipherSuite(hex, "Unknown", new Mode(Mode.Type.MUST_NOT_BE));
		addCipherSuite(unknown);
		return unknown;
	}
}
