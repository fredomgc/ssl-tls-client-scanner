/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.ondrejsmetak.entity;

import cz.ondrejsmetak.CipherSuiteRegister;
import cz.ondrejsmetak.tool.Helper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

/**
 *
 * @author Ondřej Směták <posta@ondrejsmetak.cz>
 */
public class ClientHello extends BaseEntity {

	private static final Hex CONTENT_TYPE = new Hex("16");

	private static final Hex VERSION_SSL_V_2 = new Hex("0200");
	private static final Hex VERSION_SSL_V_3 = new Hex("0300");
	private static final Hex VERSION_TLS_V_1_0 = new Hex("0301");
	private static final Hex VERSION_TLS_V_1_1 = new Hex("0302");
	private static final Hex VERSION_TLS_V_1_2 = new Hex("0303");
	private static final Hex VERSION_TLS_V_1_3 = new Hex("0304");

	private static final Hex HANDSHAKE_TYPE_CLIENT_HELLO = new Hex("01");
	
	/**
	 * 
	 * @param bytes 
	 */
	private Hex versionHandshake;
	
	public ClientHello(byte[] bytes) {
		parse(bytes);
	}

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
		Hex sessionIdLength = new Hex(bytes[i++]); //TODO, kde je to v datech
		int sessionIdLengthDec = Helper.hexToDec(sessionIdLength);
		Hex sessionId = sessionIdLengthDec > 0 ? new Hex(Arrays.copyOfRange(bytes, i, i + sessionIdLengthDec)) : new Hex("00");
		i += sessionIdLengthDec;
		
		/*cipherSuites*/
		Hex cipherSuitesLength = new Hex(bytes[i++], bytes[i++]);
		int cipherSuitesLengthDec = Helper.hexToDec(cipherSuitesLength);
		parseCipherSuites(Arrays.copyOfRange(bytes, i, i + cipherSuitesLengthDec));
		i += cipherSuitesLengthDec;
		
		/*we could continue in parsing, that we dont't acutally need following values*/
	}

	private static List<Hex> getAllVersions() {
		Hex[] data = new Hex[]{
			VERSION_SSL_V_2,
			VERSION_SSL_V_3,
			VERSION_TLS_V_1_0,
			VERSION_TLS_V_1_1,
			VERSION_TLS_V_1_2,
			VERSION_TLS_V_1_3
		};

		return Arrays.asList(data);
	}

	public static boolean isClientHello(byte[] bytes) {
		//we need at least 1 bytes to determine
		if (bytes.length < 1) {
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

	private List<CipherSuite> parseCipherSuites(byte[] bytes) {
		if (Helper.isEven(bytes.length)) {
			throw new IllegalArgumentException("Byte array of cipher suites must have odd length!");
		}

		List<CipherSuite> cipherSuites = new ArrayList<>();

		for (int i = 0; i < bytes.length; i += 2) {
			Hex hex = new Hex(bytes[i], bytes[i + 1]);
			cipherSuites.add(CipherSuiteRegister.getInstance().getByHexOrCreateDefault(hex));
		}

		return cipherSuites;
	}

	
	private List<Hex> getSupportedVersionsHandshake() {
		List<Hex> supported = new ArrayList<>();
		
		for(Hex version : getAllVersions()){
			if(Integer.parseInt(version.toString()) <= Integer.parseInt(versionHandshake.toString())){
				supported.add(version);
			}
		}
		
		return supported;
	}
	
	public List<Protocol> getSupportedProtocolsDuringHandshake() {
		List<Protocol> supported = new ArrayList<>();
		for (Hex protocol : getSupportedVersionsHandshake()) {
			supported.add(new Protocol(protocol.toString()));
		}
		
		return supported;
	}
	
}
