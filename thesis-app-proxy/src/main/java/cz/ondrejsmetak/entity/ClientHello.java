package cz.ondrejsmetak.entity;

import cz.ondrejsmetak.CipherSuiteRegister;
import cz.ondrejsmetak.tool.Helper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Client Hello. Message sent by client during SSL/TLS handshake
 *
 * @author Ondřej Směták <posta@ondrejsmetak.cz>
 */
public class ClientHello extends BasePayloadEntity {
	
	/**
	 * Version in hexadecimal format used in handshake
	 */
	private Hex versionHandshake;

	/**
	 * Collection of cipher suites offered by client
	 */
	private List<CipherSuite> cipherSuites;

	/**
	 * Creates a new Client Hello from array of bytes
	 *
	 * @param bytes array of bytes
	 */
	public ClientHello(byte[] bytes) {
		parse(bytes);
	}

	/**
	 * Parses array of bytes and fills attributes
	 *
	 * @param bytes array of bytes
	 */
	private void parse(byte[] bytes) {
		int i = 0;

		/*SSL/TLS layer*/
		Hex contentType = new Hex(bytes[i++]);
		Hex versionLayer = new Hex(bytes[i++], bytes[i++]);
		Hex lengthLayer = new Hex(bytes[i++], bytes[i++]);

		/*Handshake protocol*/
		Hex handshakeType = new Hex(bytes[i++]);
		Hex lengthHandshake = new Hex(bytes[i++], bytes[i++], bytes[i++]);
		versionHandshake = new Hex(bytes[i++], bytes[i++]);
		Hex random = new Hex(Arrays.copyOfRange(bytes, i, i + 32)); //32 bytes long
		i += 32;

		/*sessionId*/
		Hex sessionIdLength = new Hex(bytes[i++]);
		int sessionIdLengthDec = Helper.hexToDec(sessionIdLength);
		Hex sessionId = sessionIdLengthDec > 0 ? new Hex(Arrays.copyOfRange(bytes, i, i + sessionIdLengthDec)) : new Hex("00");
		i += sessionIdLengthDec;

		/*cipherSuites*/
		Hex cipherSuitesLength = new Hex(bytes[i++], bytes[i++]);
		int cipherSuitesLengthDec = Helper.hexToDec(cipherSuitesLength);
		cipherSuites = parseCipherSuites(Arrays.copyOfRange(bytes, i, i + cipherSuitesLengthDec));
		i += cipherSuitesLengthDec;

		/*we could continue in parsing, that we dont't acutally care about following values*/
	}



	/**
	 * Determines, if array of bytes if Client Hello
	 *
	 * @param bytes array of bytes
	 * @return true, if array of bytes represents Client Hello, false otherwise
	 */
	public static boolean isClientHello(byte[] bytes) {
		//we need at least 6 bytes to determine
		if (bytes.length < 6) {
			return false;
		}

		Hex contentType = new Hex(bytes[0]);
		Hex versionLayer = new Hex(bytes[1], bytes[2]);
		//skip two bytes with length
		Hex handshakeType = new Hex(bytes[5]);

		return contentType.equals(CONTENT_TYPE)
				&& getAllVersions().contains(versionLayer)
				&& handshakeType.equals(HANDSHAKE_TYPE_CLIENT_HELLO);

	}

	/**
	 * Parses part of Client Hello, that consists of offered cipher suites
	 *
	 * @param bytes array of bytes
	 * @return collection of offered cipher suites
	 */
	private List<CipherSuite> parseCipherSuites(byte[] bytes) {
		if (Helper.isEven(bytes.length)) {
			throw new IllegalArgumentException("Byte array of cipher suites must have odd length!");
		}

		List<CipherSuite> done = new ArrayList<>();

		for (int i = 0; i < bytes.length; i += 2) {
			Hex hex = new Hex(bytes[i], bytes[i + 1]);
			done.add(CipherSuiteRegister.getInstance().getByHexOrCreateDefault(hex));
		}

		return done;
	}

	/**
	 * Returns collection of hexadecimal values, where each hexadecimal value
	 * represents version (of protocol) supported by client.
	 *
	 * @return collection of supported protocol versions
	 */
	private List<Hex> getSupportedProtocolsHexDuringHandshake() {
		List<Hex> supported = new ArrayList<>();

		for (Hex version : getAllVersions()) {
			if (Integer.parseInt(version.toString()) <= Integer.parseInt(versionHandshake.toString())) {
				supported.add(version);
			}
		}

		return supported;
	}

	/**
	 * Returns collection of protocols supported by client
	 *
	 * @return collection of supported protocols
	 */
	public List<Protocol> getSupportedProtocolsDuringHandshake() {
		List<Protocol> supported = new ArrayList<>();
		for (Hex protocol : getSupportedProtocolsHexDuringHandshake()) {
			supported.add(new Protocol(protocol.toString()));
		}

		return supported;
	}

	/**
	 * Returns collection of offered cipher suites
	 *
	 * @return collection of offered cipher suites
	 */
	public List<CipherSuite> getCipherSuites() {
		return Collections.unmodifiableList(cipherSuites);
	}

	/**
	 * Checks, if collection of offered cipher suites contains "virtual" cipher
	 * suite TLS_FALLBACK_SCSV
	 *
	 * @return true, if TLS_FALLBACK_SCSV is supported, false otherwise
	 */
	public boolean isTlsFallbackScsv() {
		for (CipherSuite cipherSuite : cipherSuites) {
			if (cipherSuite.getHex().equals(CipherSuiteRegister.TLS_FALLBACK_SCSV_HEX)) {
				return true;
			}
		}
		return false;
	}
}
