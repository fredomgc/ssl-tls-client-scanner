package cz.ondrejsmetak;

import cz.ondrejsmetak.entity.Mode;
import cz.ondrejsmetak.entity.Protocol;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Holds all the configuration directives. Singleton pattern.
 *
 * @author Ondřej Směták <posta@ondrejsmetak.cz>
 */
public class ConfigurationRegister {

	/**
	 * Instance of this class
	 */
	private static ConfigurationRegister instance = null;
	/**
	 * Register of all directives
	 */
	private final HashMap<String, Object> register = new HashMap<>();

	/**
	 * Directives in a text form
	 */
	public static final String DEBUG = "debug";
	public static final String LOCAL_PORT = "localPort";
	public static final String REMOTE_HOST = "remoteHost";
	public static final String REMOTE_PORT = "remotePort";

	/**
	 * Other values
	 */
	//private Protocol highestSupportedProtocol = null;
	//SSLv2, SSLv3, TLSv10, TLSv11, TLSv12, TLSv13
	private Protocol sslv2 = null;
	private Protocol sslv3 = null;
	private Protocol tlsv10 = null;
	private Protocol tlsv11 = null;
	private Protocol tlsv12 = null;

	private Mode tlsFallbackScsv = null;

	protected ConfigurationRegister() {
		//no direct instantiation
	}

	/**
	 * Returns a instance of this class
	 *
	 * @return instance of this class
	 */
	public static ConfigurationRegister getInstance() {
		if (instance == null) {
			instance = new ConfigurationRegister();
			instance.setDirective(DEBUG, true); //by default, debug is enabled
		}
		return instance;
	}

	/**
	 * Sets directive with the given name to the given value
	 *
	 * @param name name of the directive
	 * @param value value
	 */
	private void setDirective(String name, Object value) {
		if (!isSupportedDirective(name)) {
			throw new IllegalArgumentException("Unknown configuration directive [" + name + "] !");
		}

		register.put(name, value);
	}

	/**
	 * Obtains value for the given directive
	 *
	 * @param name name of the directive
	 * @return value of the given directive
	 */
	private Object getDirective(String name) {
		if (!register.containsKey(name)) {
			throw new IllegalArgumentException("Configuration directive [" + name + "] not found!");
		}

		return register.get(name);
	}

	/**
	 * Checks, if this register already contains directive with the given name
	 *
	 * @param name of the directive
	 * @return true, if such the directive already exists, false otherwise
	 */
	private boolean hasDirective(String name) {
		return register.containsKey(name);
	}

	/**
	 * Checks, if this this register contains all the required directives. If
	 * not, this application can't run.
	 *
	 * @return true, if all the directives are set, false otherwise
	 */
	public boolean hasAllDirectives() {
		for (String directive : getDirectives()) {
			if (!hasDirective(directive)) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Returns a collection of the missing directives, that are required for run
	 * of this application
	 *
	 * @return collection of the missing directives
	 */
	public List<String> getMissingDirectives() {
		List<String> missing = new ArrayList<>();
		for (String directive : getDirectives()) {
			if (!hasDirective(directive)) {
				missing.add(directive);
			}
		}

		return missing;
	}

	/**
	 * Returns collection with the names of all the supported directives
	 *
	 * @return collection with the names of all the supported directives
	 */
	private List<String> getDirectives() {
		String[] directives = {DEBUG, LOCAL_PORT, REMOTE_HOST, REMOTE_PORT};
		return new ArrayList<>(Arrays.asList(directives));
	}

	/**
	 * Is the given name recognized as supported directive
	 *
	 * @param name name of the candidate
	 * @return true, if such name is recognized, false otherwise
	 */
	private boolean isSupportedDirective(String name) {
		return getDirectives().contains(name);
	}

	public void setLocalPort(Integer value) {
		setDirective(LOCAL_PORT, value);
	}

	public Integer getLocalPort() {
		return (Integer) getDirective(LOCAL_PORT);
	}

	public void setRemotePort(Integer value) {
		setDirective(REMOTE_PORT, value);
	}

	public Integer getRemotePort() {
		return (Integer) getDirective(REMOTE_PORT);
	}

	public void setRemoteHost(String value) {
		setDirective(REMOTE_HOST, value);
	}

	public String getRemoteHost() {
		return (String) getDirective(REMOTE_HOST);
	}

	public void setDebug(Boolean value) {
		setDirective(DEBUG, value);
	}

	public Boolean isDebug() {
		return (Boolean) getDirective(DEBUG);
	}

	public Mode getTlsFallbackScsv() {
		return tlsFallbackScsv;
	}

	public void setTlsFallbackScsv(Mode tlsFallbackScsv) {
		this.tlsFallbackScsv = tlsFallbackScsv;
	}
	
	public List<String> getMissingProtocols() {
		List<String> missing = new ArrayList<>();
		List<Protocol> protocols = Arrays.asList(sslv2, sslv3, tlsv10, tlsv11, tlsv12);
		List<Protocol.Type> types = Arrays.asList(Protocol.Type.SSLv2, Protocol.Type.SSLv3, Protocol.Type.TLSv10, Protocol.Type.TLSv11, Protocol.Type.TLSv12);

		for (int i = 0; i < protocols.size(); i++) {
			if (protocols.get(i) == null) {
				missing.add(types.get(i).toString());
			}
		}

		return missing;
	}

	private void checkProtocol(Protocol newValue, Protocol.Type type) {
		if (type != newValue.getType()) {
			throw new IllegalArgumentException("Protocol " + type + " is expected!");
		}
	}

	public List<Protocol> getProtocols(){
		return new ArrayList<>(Arrays.asList(sslv2, sslv3, tlsv10, tlsv11, tlsv12));
	}
	
	public void setProtocol(Protocol protocol) {
		switch (protocol.getType()) {
			case SSLv2:
				setProtocolSSLv2(protocol);
				break;

			case SSLv3:
				setProtocolSSLv3(protocol);
				break;

			case TLSv10:
				setProtocolTLSv10(protocol);
				break;

			case TLSv11:
				setProtocolTLSv11(protocol);
				break;

			case TLSv12:
				setProtocolTLSv12(protocol);
				break;

			default:
				throw new IllegalArgumentException(String.format("Unsupported protocol type [%s]!", protocol.getType()));

		}
	}

	private void setProtocolSSLv2(Protocol protocol) {
		checkProtocol(protocol, Protocol.Type.SSLv2);
		this.sslv2 = protocol;
	}

	public Protocol getProtocolSSLv2() {
		return this.sslv2;
	}

	private void setProtocolSSLv3(Protocol protocol) {
		checkProtocol(protocol, Protocol.Type.SSLv3);
		this.sslv3 = protocol;
	}

	public Protocol getProtocolSSLv3() {
		return this.sslv3;
	}

	private void setProtocolTLSv10(Protocol protocol) {
		checkProtocol(protocol, Protocol.Type.TLSv10);
		this.tlsv10 = protocol;
	}

	public Protocol getProtocolTLSv10() {
		return this.tlsv10;
	}

	private void setProtocolTLSv11(Protocol protocol) {
		checkProtocol(protocol, Protocol.Type.TLSv11);
		this.tlsv11 = protocol;
	}

	public Protocol getProtocolTLSv11() {
		return this.tlsv11;
	}

	private void setProtocolTLSv12(Protocol protocol) {
		checkProtocol(protocol, Protocol.Type.TLSv12);
		this.tlsv12 = protocol;
	}

	public Protocol getProtocolTLSv12() {
		return this.tlsv12;
	}
}
