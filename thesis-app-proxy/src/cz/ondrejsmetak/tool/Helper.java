package cz.ondrejsmetak.tool;

import cz.ondrejsmetak.entity.Hex;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;
import java.util.regex.Pattern;
import javax.xml.bind.DatatypeConverter;
import org.apache.commons.lang3.SystemUtils;

/**
 *
 * @author Ondřej Směták <posta@ondrejsmetak.cz>
 */
public class Helper {

	/**
	 * Regex pattern, that is being used during testing a correct format of
	 * hexadecimal value
	 */
	private static final Pattern isHex = Pattern.compile("^[0-9A-Fa-f]+$");

	/**
	 * Checks, if given port is available for (local) binding
	 *
	 * @param port given port, that will be tested
	 * @return true, if given port is available, false otherwise
	 */
	public static boolean isLocalPortAvailable(int port) {
		try {
			// Try to open a LOCAL port with ServerSocket
			new ServerSocket(port).close();
			// Local port can be opened, so it's available
			return true;
		} catch (IOException e) {
			// Local port cannot be opened, it's in use
			return false;
		}
	}

	/**
	 * Returns path to current working directory
	 *
	 * @return path to current working directory
	 */
	public static String getWorkingDirectory() {
		return System.getProperty("user.dir");
	}

	/**
	 * Returns date formatted to human readable format
	 *
	 * @param date date, that will be formatted
	 * @param dash use dash during formatting date or not
	 * @return formatted date
	 */
	public static String getFormattedDateTime(Date date, boolean dash) {
		DateFormat dateFormat = new SimpleDateFormat(dash ? "yyyy-MM-dd-HH-mm-ss" : "yyyy-MM-dd HH:mm:ss");
		return dateFormat.format(date);
	}

	/**
	 * Returns whole content of the given file
	 *
	 * @param file file, that will be read
	 * @return content of file
	 * @throws FileNotFoundException if case of error
	 */
	public static String getContentOfFile(File file) throws FileNotFoundException {
		//http://stackoverflow.com/a/3403112
		return new Scanner(file).useDelimiter("\\Z").next();
	}

	/**
	 * Returns whole content of the given file input stream
	 *
	 * @param inputStream file input stream, that will be read
	 * @return content of file
	 */
	public static String getContentOfFile(InputStream inputStream) {
		StringBuilder sb = new StringBuilder();
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
			String line;
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}
		} catch (IOException ex) {
			Log.debugException(ex);
			return "";
		}

		return sb.toString();
	}

	/**
	 * Is input boolean value in its text form?
	 *
	 * @param input
	 * @return
	 */
	public static boolean isBooleanStr(String input) {
		return input.equalsIgnoreCase("true") || input.equalsIgnoreCase("false");
	}

	/**
	 * Parses boolean value in text form to <code>code.lang.Boolean</code>
	 * object
	 *
	 * @param input
	 * @return
	 */
	public static boolean parseBooleanStr(String input) {
		return input.equalsIgnoreCase("true");
	}

	/**
	 * Checks, if string contains properly formatted integer value
	 *
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

	/**
	 * Checks if the given string contains exactly valid integer value
	 *
	 * @param input text value, that will be tested
	 * @return true if given value contains integer, false otherwise
	 */
	public static boolean isInteger(String input) {
		return isInteger(input, 10);
	}

	/**
	 * Parses the given text as a signed integer in the 16 radix
	 *
	 * @param hex hexadecimal value, that will be parsed
	 * @return parsed value
	 */
	public static Integer hexToInt(String hex) {
		return Integer.parseInt(hex.trim(), 16);
	}

	/**
	 * Transform array of bytes to text representation
	 *
	 * @param array array of bytes
	 * @return text representation
	 */
	public static String toHexString(byte[] array) {
		return DatatypeConverter.printHexBinary(array);
	}

	/**
	 * Transforms text value in the array of bytes
	 *
	 * @param string text representation
	 * @return array of bytes
	 */
	public static byte[] toByteArray(String string) {
		return DatatypeConverter.parseHexBinary(string);
	}

	/**
	 * Transforms array of Bytes into array of bytes
	 *
	 * @param array array of Bytes
	 * @return array of bytes
	 */
	public static byte[] byteToByte(Byte[] array) {
		byte[] data = new byte[array.length];

		for (int i = 0; i < array.length; i++) {
			data[i] = array[i];
		}

		return data;
	}

	/**
	 * Transforms array of bytes into array of Bytes
	 *
	 * @param array array of bytes
	 * @return array of Bytes
	 */
	public static Byte[] byteToByte(byte[] array) {
		Byte[] data = new Byte[array.length];

		for (int i = 0; i < array.length; i++) {
			data[i] = array[i];
		}

		return data;
	}

	/**
	 * Transforms decimal value into hexadecimal
	 *
	 * @param decValue decimal value
	 * @return hexadecimal value
	 */
	public static String decToHex(int decValue) {
		return Integer.toHexString(decValue);
	}

	/**
	 * Transform decimal value into binary
	 *
	 * @param decValue decimal value
	 * @return binary value
	 */
	public static String decToBin(int decValue) {
		return Integer.toBinaryString(decValue);
	}

	/**
	 * Transforms hexadecimal value into decimal
	 *
	 * @param hexValue hexadecimal value
	 * @return decimal value
	 */
	public static Integer hexToDec(String hexValue) {
		return Integer.parseInt(hexValue, 16);
	}

	/**
	 * Transforms hexadecimal value into decimal
	 *
	 * @param hexValue hexadecimal value
	 * @return decimal value
	 */
	public static Integer hexToDec(Hex hexValue) {
		return hexToDec(hexValue.toString());
	}

	/**
	 * Transforms hexadecimal value into binary
	 *
	 * @param hexValue hexadecimal value
	 * @return binary value
	 */
	public static String hexToBin(String hexValue) {
		return decToBin(hexToDec(hexValue));
	}

	/**
	 * Checks, if application is running on Windows
	 *
	 * @return true, if application is running on Windows, false otherwise
	 */
	public static boolean isWindows() {
		return SystemUtils.IS_OS_WINDOWS;
	}

	/**
	 * Checks, if application is running on Linux
	 *
	 * @return true, if application is running on Linux, false otherwise
	 */
	public static boolean isLinux() {
		return SystemUtils.IS_OS_LINUX;
	}

	/**
	 * Checks, if application is running on Mac OS X
	 *
	 * @return true, if application is running on Mac OS X, false otherwise
	 */
	public static boolean isMacOsx() {
		return SystemUtils.IS_OS_MAC_OSX;
	}

	/**
	 * Checks if text contains valid hexadecimal value
	 *
	 * @param value text value
	 * @return true if text contains valid hexadecimal value, false otherwise
	 */
	public static boolean isHex(String value) {
		return isHex.matcher(value).matches();
	}

	/**
	 * Is given number odd?
	 *
	 * @param number number, that will be checked
	 * @return true if given number is odd, false otherwise
	 */
	public static boolean isOdd(int number) {
		return number % 2 == 0;
	}

	/**
	 * Is given number even?
	 *
	 * @param number number, that will be checked
	 * @return true if given number is even, false otherwise
	 */
	public static boolean isEven(int number) {
		return !isOdd(number);
	}
}
