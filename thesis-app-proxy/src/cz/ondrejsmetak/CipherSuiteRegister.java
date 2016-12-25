package cz.ondrejsmetak;

import cz.ondrejsmetak.entity.CipherSuite;
import cz.ondrejsmetak.entity.Hex;
import cz.ondrejsmetak.entity.Mode;
import java.util.Collection;
import java.util.HashMap;

/**
 * Holds all cipher suites used in application
 *
 * @author Ondřej Směták <posta@ondrejsmetak.cz>
 */
public class CipherSuiteRegister {

	/**
	 * Instance of this class
	 */
	private static CipherSuiteRegister instance = null;

	/**
	 * Register of all cipher suites
	 */
	private final HashMap<Hex, CipherSuite> register = new HashMap<>();

	/**
	 * Hexadecimal value of cipher suite TLS_FALLBACK_SCSV
	 */
	public static final Hex TLS_FALLBACK_SCSV_HEX = new Hex("0x5600");

	/**
	 * Name of cipher suite TLS_FALLBACK_SCSV
	 */
	public static final String TLS_FALLBACK_SCSV_NAME = "TLS_FALLBACK_SCSV";

	protected CipherSuiteRegister() {
		//no direct instantiation.
	}

	/**
	 * Returns a instance of this class
	 *
	 * @return instance of this class
	 */
	public static CipherSuiteRegister getInstance() {
		if (instance == null) {
			instance = new CipherSuiteRegister();
		}
		return instance;
	}

	/**
	 * Adds the given cipher suite to this register
	 *
	 * @param cipherSuite cipher suite, that will be added
	 */
	public void addCipherSuite(CipherSuite cipherSuite) {
		register.put(cipherSuite.getHex(), cipherSuite);
	}

	/**
	 * Checks, if this register contains given cipher suite
	 *
	 * @param cipherSuite cipher suite, that will be tested
	 * @return true, if register contains given cipher suite, false otherwise
	 */
	public boolean containsCipherSuite(CipherSuite cipherSuite) {
		return register.keySet().contains(cipherSuite.getHex());
	}

	/**
	 * Returns size (count of records) of this register
	 *
	 * @return size of this register
	 */
	public int getSize() {
		return register.size();
	}

	/**
	 * Returns all cipher suites in this register
	 *
	 * @return collection of the cipher suites
	 */
	public Collection<CipherSuite> getCipherSuites() {
		return register.values();
	}

	/**
	 * Returns cipher suite with the given hexadecimal value contained in this
	 * register. If such cipher suite is not found, that cipher suite is created
	 * (with default "unknown" name).
	 *
	 * @param hex hexadecimal value of the target cipher suite
	 * @return found cipher suite or a newly created cipher suite
	 */
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
