package cz.ondrejsmetak;

import cz.ondrejsmetak.entity.CipherSuite;
import cz.ondrejsmetak.entity.ClientCertificate;
import cz.ondrejsmetak.entity.Hex;
import cz.ondrejsmetak.entity.Mode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

/**
 * Holds all configuration certificates used in application
 *
 * @author Ondřej Směták <posta@ondrejsmetak.cz>
 */
public class ConfigurationCertificateRegister {

	/**
	 * Instance of this class
	 */
	private static ConfigurationCertificateRegister instance = null;

	/**
	 * Register of all configuration certificates
	 */
	private final HashMap<String, ClientCertificate> register = new HashMap<>();

	/**
	 * Returns a instance of this class
	 *
	 * @return instance of this class
	 */
	public static ConfigurationCertificateRegister getInstance() {
		if (instance == null) {
			instance = new ConfigurationCertificateRegister();
		}
		return instance;
	}

	/**
	 * Adds the given configuration certificate to this register
	 *
	 * @param configurationCertificate cipher suite, that will be added
	 */
	public void addConfigurationCertificate(ClientCertificate configurationCertificate) {
		register.put(configurationCertificate.getName(), configurationCertificate);
	}

	public boolean containsConfigurationCertificate(ClientCertificate configurationCertificate) {
		return containsConfigurationCertificate(configurationCertificate.getName());
	}

	public boolean containsConfigurationCertificate(String nameOfConfigurationCertificate) {
		return getByName(nameOfConfigurationCertificate) != null;
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
	 * Returns all configuration certificates in this register
	 *
	 * @return collection of the configuration certificates
	 */
	public Collection<ClientCertificate> getConfigurationCertificates() {
		return register.values();
	}
	
	public ArrayList<ClientCertificate> getConfigurationCertificatesIndexable() {
		return new ArrayList<>(register.values());
	}
	

	public ClientCertificate getByName(String nameOfConfigurationCertificate) {
		for (ClientCertificate cerificate : getConfigurationCertificates()) {
			if (cerificate.getName().equals(nameOfConfigurationCertificate)) {
				return cerificate;
			}
		}

		return null;
	}

	public ClientCertificate getFirstMustBe() {
		for (ClientCertificate certificate : getConfigurationCertificates()) {
			if (certificate.getMode().isMustBe()) {
				return certificate;
			}
		}

		return null;
	}

	public boolean hasAtLeastOneMustBe() {
		return getFirstMustBe() != null;
	}

}
