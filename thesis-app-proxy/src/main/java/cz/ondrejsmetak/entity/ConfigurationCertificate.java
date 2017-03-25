package cz.ondrejsmetak.entity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Ondřej Směták <posta@ondrejsmetak.cz>
 */
public class ConfigurationCertificate extends BaseEntity {

	private String name;

	private Mode mode;

	private KeyStore keystore;

	public ConfigurationCertificate(String name, Mode mode, String path, String password) {
		this.name = name;
		this.mode = mode;
		this.keystore = createKeyStoreFromPath(path, password);
	}

	private KeyStore createKeyStoreFromPath(String path, String password) {
		try {
			KeyStore ks = KeyStore.getInstance("JKS");
			ks.load(new FileInputStream(new File(path)), password.toCharArray());
			return ks;
		} catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException ex) {
			throw new IllegalArgumentException("Can't initialize KeyStore.", ex);
		}
	}

}
