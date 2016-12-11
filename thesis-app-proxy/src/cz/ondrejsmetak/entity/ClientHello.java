/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.ondrejsmetak.entity;

import cz.ondrejsmetak.tool.Helper;
import java.util.Arrays;

/**
 *
 * @author Ondřej Směták <posta@ondrejsmetak.cz>
 */
public class ClientHello extends BaseEntity {

	private static Hex CONTENT_TYPE = new Hex("16");

	private static final Hex VERSION_SSL_V_2 = new Hex("0200");
	private static final Hex VERSION_SSL_V_3 = new Hex("0300");
	private static final Hex VERSION_TLS_V_1_0 = new Hex("0301");
	private static final Hex VERSION_TLS_V_1_1 = new Hex("0302");
	private static final Hex VERSION_TLS_V_1_2 = new Hex("0303");
	private static final Hex VERSION_TLS_V_1_3 = new Hex("0304");

	public ClientHello(byte[] bytes) {
		isClientHello(bytes);
	}

	private void isClientHello(byte[] bytes) {
		int i = 0;

		/*SSL/TLS layer*/
		Hex contentType = new Hex(bytes[i++]);
		Hex versionLayer = new Hex(bytes[i++], bytes[i++]);
		Hex lengthLayer = new Hex(bytes[i++], bytes[i++]);

		/*Handshake protocol*/
		Hex handshakeType = new Hex(bytes[i++]);
		Hex lengthHandshake = new Hex(bytes[i++], bytes[i++], bytes[i++]);
		Hex versionHandshake = new Hex(bytes[i++], bytes[i++]);
		Hex random = new Hex(Arrays.copyOfRange(bytes, i, i + 32)); //32
		i += 32;
		
		Hex sessionIdLength = new Hex(bytes[i++]); //TODO, kde je to v datech
		//TODO, parsovani sessionid
		
		Hex cipherSuitesLength = new Hex(bytes[i++], bytes[i++]);
		int cipherSuitesLengthDec = Helper.hexToDec(cipherSuitesLength);
		Hex cipherSuites = new Hex(Arrays.copyOfRange(bytes, i, i + cipherSuitesLengthDec)); //TODO, parse
		i += cipherSuitesLengthDec;
		
		
		
	}

	private void parseCipherSuites(byte[] bytes){
		//2 bajty pro každou šifru
	}
	
}
