package cz.ondrejsmetak.entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * SSL/TLS protocol
 *
 * @author Ondřej Směták <posta@ondrejsmetak.cz>
 */
public class Protocol extends BaseEntity {

	/**
	 * Type of protocol
	 */
	public enum Type {
		SSLv2, SSLv3, TLSv10, TLSv11, TLSv12, TLSv13
	}

	/**
	 * Type of this protocol
	 */
	private Type type = null;

	/**
	 * Mode, that affects behaviour during scanning this protocol
	 */
	private Mode mode;

	/**
	 * Supported text representation of each protocol type
	 */
	private static final String[] sslv2Aliases = {"SSLv2", "SSL 2.0", "SSL 2", "SSL2", "0200", "0x0200"};
	private static final String[] sslv3Aliases = {"SSLv3", "SSL 3.0", "SSL 3", "SSL3", "0300", "0x0300"};
	private static final String[] tlsv10Aliases = {"TLSv1.0", "TLS 1.0", "TLSv10", "TLS 10", "TLSv1", "0301", "0x0301"};
	private static final String[] tlsv11Aliases = {"TLSv1.1", "TLS 1.1", "TLSv11", "TLS 11", "0302", "0x0302"};
	private static final String[] tlsv12Aliases = {"TLSv1.2", "TLS 1.2", "TLSv12", "TLS 12", "0303", "0x0303"};
	private static final String[] tlsv13Aliases = {"TLSv1.3", "TLS 1.3", "TLSv13", "TLS 13", "0304", "0x0304"};

	/**
	 * Returns all supported protocol types
	 *
	 * @return collection containing all supported protocol types
	 */
	public static List<Protocol.Type> getAllTypes() {
		return new ArrayList<>(Arrays.asList(new Protocol.Type[]{
			Type.SSLv2, Type.SSLv3, Type.TLSv10, Type.TLSv11, Type.TLSv12, Type.TLSv13
		}));
	}

	/**
	 * Creates new protocol with given text representation and unknown mode
	 *
	 * @param protocolCodeName text representation of type's name
	 */
	public Protocol(String protocolCodeName) {
		this(protocolCodeName, null);
	}

	/**
	 * Creates new protocol with precisely given type and unknown mode
	 *
	 * @param type type of protocol
	 */
	public Protocol(Type type) {
		this(type, null);
	}

	/**
	 * Creates new protocol with given text representation and mode
	 *
	 * @param protocolCodeName text representation of type's name
	 * @param mode mode of this protocol
	 */
	public Protocol(String protocolCodeName, Mode mode) {
		if (hasCodeName(protocolCodeName, sslv2Aliases)) {
			type = Type.SSLv2;
		}

		if (hasCodeName(protocolCodeName, sslv3Aliases)) {
			type = Type.SSLv3;
		}

		if (hasCodeName(protocolCodeName, tlsv10Aliases)) {
			type = Type.TLSv10;
		}

		if (hasCodeName(protocolCodeName, tlsv11Aliases)) {
			type = Type.TLSv11;
		}

		if (hasCodeName(protocolCodeName, tlsv12Aliases)) {
			type = Type.TLSv12;
		}

		if (hasCodeName(protocolCodeName, tlsv13Aliases)) {
			type = Type.TLSv13;
		}

		if (type == null) {
			throw new IllegalArgumentException("Unknown protocol codename (" + protocolCodeName + ")");
		}

		this.mode = mode;
	}

	/**
	 * Creates new protocol with precisely given type and mode
	 *
	 * @param type type of protocol
	 * @param mode of this protocol
	 */
	public Protocol(Type type, Mode mode) {
		this.type = type;
		this.mode = mode;
	}

	@Override
	public String toString() {

		if (type == Type.SSLv2) {
			return sslv2Aliases[0];
		}

		if (type == Type.SSLv3) {
			return sslv3Aliases[0];
		}

		if (type == Type.TLSv10) {
			return tlsv10Aliases[0];
		}

		if (type == Type.TLSv11) {
			return tlsv11Aliases[0];
		}

		if (type == Type.TLSv12) {
			return tlsv12Aliases[0];
		}

		if (type == Type.TLSv13) {
			return tlsv13Aliases[0];
		}

		return "";
	}

	/**
	 * Checks, if given codename is supported
	 *
	 * @param codeName codename candidate
	 * @param candidates array of supported codenames
	 * @return true, if given codename is supported, false otherwise
	 */
	private boolean hasCodeName(String codeName, String[] candidates) {
		for (int i = 0; i < candidates.length; i++) {
			if (codeName.equalsIgnoreCase(candidates[i])) {
				return true;
			}
		}
		return false;
	}

	public Type getType() {
		return type;
	}

	public Mode getMode() {
		return mode;
	}

	/**
	 * Returns collection of sorted protocols by its technical timeline up to
	 * the given value
	 *
	 * @param type upper type of the protocol
	 * @param withMode mode, that will be set all protocols in the resulting
	 * collection
	 * @return collection of protocols
	 */
	public static List<Protocol> getProtocolsUpTo(Type type, Mode withMode) {
		List<Protocol> protocols = new ArrayList<>();

		for (Type single : getAllTypes()) {
			if (single.ordinal() <= type.ordinal()) {
				protocols.add(new Protocol(single, withMode));
			}
		}

		return protocols;
	}

	@Override
	public int hashCode() {
		int hash = 5;
		hash = 79 * hash + Objects.hashCode(this.type);
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Protocol other = (Protocol) obj;
		if (this.type != other.type) {
			return false;
		}
		return true;
	}

}
