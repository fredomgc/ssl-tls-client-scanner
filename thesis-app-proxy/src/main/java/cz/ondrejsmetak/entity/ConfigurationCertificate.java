package cz.ondrejsmetak.entity;

/**
 *
 * @author Ondřej Směták <posta@ondrejsmetak.cz>
 */
public class ConfigurationCertificate extends BaseEntity {

	public enum Behaviour {
		DEFAULT, REJECT, ACCEPT
	}

	private String name;

	private Behaviour behaviour;
	
	private String path;
	
	private String password;
}
