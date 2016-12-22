/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.ondrejsmetak.tool;

import cz.ondrejsmetak.entity.Hex;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import javax.xml.bind.DatatypeConverter;
import org.apache.commons.lang3.SystemUtils;

/**
 *
 * @author Ondřej Směták <posta@ondrejsmetak.cz>
 */
public class Helper {
	
	private static final Pattern isHex = Pattern.compile("^[0-9A-Fa-f]+$");

	
	/**
	 * Is input boolean value in its text form?
	 *
	 * @param input
	 * @return
	 */
	public static boolean isBooleanStr(String input) {
		return input.equalsIgnoreCase("true") || input.equalsIgnoreCase("false");
	}

	public static boolean parseBooleanStr(String input) {
		return input.equalsIgnoreCase("true");
	}
	
	
	/**
	 * Checks, if string contains properly formatted integer value 
	 * @param s
	 * @param radix
	 * @return 
	 */
	private static boolean isInteger(String s, int radix) {
		Scanner sc = new Scanner(s.trim());
		if (!sc.hasNextInt(radix)) {
			return false;
		}
		sc.nextInt(radix);
		return !sc.hasNext();
	}
	
	public static boolean isInteger(String input) {
		return isInteger(input, 10);
	}
	
	public static String toHexString(byte[] array) {
		return DatatypeConverter.printHexBinary(array);
	}

	public static byte[] toByteArray(String s) {
		return DatatypeConverter.parseHexBinary(s);
	}

	public static byte[] byteToByte(Byte[] array) {
		byte[] data = new byte[array.length];

		for (int i = 0; i < array.length; i++) {
			data[i] = array[i];
		}

		return data;
	}

	public static Byte[] byteToByte(byte[] array) {
		Byte[] data = new Byte[array.length];

		for (int i = 0; i < array.length; i++) {
			data[i] = array[i];
		}

		return data;
	}

	public static String decToHex(int decValue) {
		return Integer.toHexString(decValue);
	}

	public static String decToBin(int decValue) {
		return Integer.toBinaryString(decValue);
	}

	public static Integer hexToDec(String hexValue) {
		return Integer.parseInt(hexValue, 16);
	}

	public static Integer hexToDec(Hex hexValue) {
		return hexToDec(hexValue.toString());
	}

	public static String hexToBin(String hexValue) {
		return decToBin(hexToDec(hexValue));
	}

	public static boolean isWindows() {
		return SystemUtils.IS_OS_WINDOWS;
	}

	public static boolean isLinux() {
		return SystemUtils.IS_OS_LINUX;
	}

	public static boolean isMacOsx() {
		return SystemUtils.IS_OS_MAC_OSX;
	}
	
	public static boolean isHex(String value){
		return isHex.matcher(value).matches();
	}
	
	/**
	 * Je sudé?
	 *
	 * @param number
	 * @return
	 */
	public static boolean isOdd(int number) {
		return number % 2 == 0;
	}

	/**
	 * Je liché?
	 *
	 * @param number
	 * @return
	 */
	public static boolean isEven(int number) {
		return !isOdd(number);
	}

	
	
	
		//Default enabled Cipher Suites
//	public static final String[] CIPHERS_A = new String[]{
//		"TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA384",
//		"TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384",
//		"TLS_RSA_WITH_AES_256_CBC_SHA256",
//		"TLS_ECDH_ECDSA_WITH_AES_256_CBC_SHA384",
//		"TLS_ECDH_RSA_WITH_AES_256_CBC_SHA384",
//		"TLS_DHE_RSA_WITH_AES_256_CBC_SHA256",
//		"TLS_DHE_DSS_WITH_AES_256_CBC_SHA256",
//		"TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA",
//		"TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA",
//		"TLS_RSA_WITH_AES_256_CBC_SHA",
//		"TLS_ECDH_ECDSA_WITH_AES_256_CBC_SHA",
//		"TLS_ECDH_RSA_WITH_AES_256_CBC_SHA",
//		"TLS_DHE_RSA_WITH_AES_256_CBC_SHA",
//		"TLS_DHE_DSS_WITH_AES_256_CBC_SHA",
//		"TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA256",
//		"TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256",
//		"TLS_RSA_WITH_AES_128_CBC_SHA256",
//		"TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA256",
//		"TLS_ECDH_RSA_WITH_AES_128_CBC_SHA256",
//		"TLS_DHE_RSA_WITH_AES_128_CBC_SHA256",
//		"TLS_DHE_DSS_WITH_AES_128_CBC_SHA256",
//		"TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA",
//		"TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA",
//		"TLS_RSA_WITH_AES_128_CBC_SHA",
//		"TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA",
//		"TLS_ECDH_RSA_WITH_AES_128_CBC_SHA",
//		"TLS_DHE_RSA_WITH_AES_128_CBC_SHA",
//		"TLS_DHE_DSS_WITH_AES_128_CBC_SHA",
//		"TLS_ECDHE_ECDSA_WITH_RC4_128_SHA",
//		"TLS_ECDHE_RSA_WITH_RC4_128_SHA",
//		"SSL_RSA_WITH_RC4_128_SHA",
//		"TLS_ECDH_ECDSA_WITH_RC4_128_SHA",
//		"TLS_ECDH_RSA_WITH_RC4_128_SHA",
//		"TLS_ECDHE_ECDSA_WITH_3DES_EDE_CBC_SHA",
//		"TLS_ECDHE_RSA_WITH_3DES_EDE_CBC_SHA",
//		"SSL_RSA_WITH_3DES_EDE_CBC_SHA",
//		"TLS_ECDH_ECDSA_WITH_3DES_EDE_CBC_SHA",
//		"TLS_ECDH_RSA_WITH_3DES_EDE_CBC_SHA",
//		"SSL_DHE_RSA_WITH_3DES_EDE_CBC_SHA",
//		"SSL_DHE_DSS_WITH_3DES_EDE_CBC_SHA",
//		"SSL_RSA_WITH_RC4_128_MD5", //"TLS_EMPTY_RENEGOTIATION_INFO_SCSV2",
//	};
//
//	//Default Disabled Cipher Suites
//	public static final String[] CIPHERS_B = new String[]{"TLS_DH_anon_WITH_AES_256_CBC_SHA256",
//		"TLS_ECDH_anon_WITH_AES_256_CBC_SHA",
//		"TLS_DH_anon_WITH_AES_256_CBC_SHA",
//		"TLS_DH_anon_WITH_AES_128_CBC_SHA256",
//		"TLS_ECDH_anon_WITH_AES_128_CBC_SHA",
//		"TLS_DH_anon_WITH_AES_128_CBC_SHA",
//		"TLS_ECDH_anon_WITH_RC4_128_SHA",
//		"SSL_DH_anon_WITH_RC4_128_MD5",
//		"TLS_ECDH_anon_WITH_3DES_EDE_CBC_SHA",
//		"SSL_DH_anon_WITH_3DES_EDE_CBC_SHA",
//		"TLS_RSA_WITH_NULL_SHA256",
//		"TLS_ECDHE_ECDSA_WITH_NULL_SHA",
//		"TLS_ECDHE_RSA_WITH_NULL_SHA",
//		"SSL_RSA_WITH_NULL_SHA",
//		"TLS_ECDH_ECDSA_WITH_NULL_SHA",
//		"TLS_ECDH_RSA_WITH_NULL_SHA",
//		"TLS_ECDH_anon_WITH_NULL_SHA",
//		"SSL_RSA_WITH_NULL_MD5",
//		"SSL_RSA_WITH_DES_CBC_SHA",
//		"SSL_DHE_RSA_WITH_DES_CBC_SHA",
//		"SSL_DHE_DSS_WITH_DES_CBC_SHA",
//		"SSL_DH_anon_WITH_DES_CBC_SHA",
//		"SSL_RSA_EXPORT_WITH_RC4_40_MD5",
//		"SSL_DH_anon_EXPORT_WITH_RC4_40_MD5",
//		"SSL_RSA_EXPORT_WITH_DES40_CBC_SHA",
//		"SSL_DHE_RSA_EXPORT_WITH_DES40_CBC_SHA",
//		"SSL_DHE_DSS_EXPORT_WITH_DES40_CBC_SHA",
//		"SSL_DH_anon_EXPORT_WITH_DES40_CBC_SHA",
//		"TLS_KRB5_WITH_RC4_128_SHA",
//		"TLS_KRB5_WITH_RC4_128_MD5",
//		"TLS_KRB5_WITH_3DES_EDE_CBC_SHA",
//		"TLS_KRB5_WITH_3DES_EDE_CBC_MD5",
//		"TLS_KRB5_WITH_DES_CBC_SHA",
//		"TLS_KRB5_WITH_DES_CBC_MD5",
//		"TLS_KRB5_EXPORT_WITH_RC4_40_SHA",
//		"TLS_KRB5_EXPORT_WITH_RC4_40_MD5",
//		"TLS_KRB5_EXPORT_WITH_DES_CBC_40_SHA",
//		"TLS_KRB5_EXPORT_WITH_DES_CBC_40_MD5",};
//
//	public static String[] getAllCiphers() {
//		return Stream.concat(Arrays.stream(Helper.CIPHERS_A), Arrays.stream(Helper.CIPHERS_B)).toArray(String[]::new);
//	}
}
