package cz.ondrejsmetak.entity;

import java.util.Arrays;
import java.util.List;

/**
 *
 * Zakladni trida ze ktere dedi vse, co se nejak podili na SSL/TLS komunikaci
 *
 * @author Ondřej Směták <posta@ondrejsmetak.cz>
 */
public class BasePayloadEntity {

	/**
	 * Usefull hexadecimal constants, that are used inside Client Hello
	 */
	protected static final Hex CONTENT_TYPE = new Hex("16");

	protected static final Hex VERSION_SSL_V_2 = new Hex("0200");
	protected static final Hex VERSION_SSL_V_3 = new Hex("0300");
	protected static final Hex VERSION_TLS_V_1_0 = new Hex("0301");
	protected static final Hex VERSION_TLS_V_1_1 = new Hex("0302");
	protected static final Hex VERSION_TLS_V_1_2 = new Hex("0303");
	protected static final Hex VERSION_TLS_V_1_3 = new Hex("0304");

	protected static final Hex HANDSHAKE_TYPE_CLIENT_HELLO = new Hex("01");
	protected static final Hex HANDSHAKE_TYPE_SERVER_HELLO = new Hex("02");
	protected static final Hex HANDSHAKE_TYPE_CERTIFICATE = new Hex("0b");

	/**
	 * Returns collection containing all possible versions used in Client Hello
	 *
	 * @return collection with all possible versions
	 */
	protected static List<Hex> getAllVersions() {
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

}
