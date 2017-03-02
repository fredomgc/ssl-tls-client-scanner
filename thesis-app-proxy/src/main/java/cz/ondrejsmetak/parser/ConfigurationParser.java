package cz.ondrejsmetak.parser;

import cz.ondrejsmetak.parser.BaseParser;
import cz.ondrejsmetak.CipherSuiteRegister;
import cz.ondrejsmetak.ConfigurationRegister;
import cz.ondrejsmetak.ResourceManager;
import cz.ondrejsmetak.entity.CipherSuite;
import cz.ondrejsmetak.entity.Mode;
import cz.ondrejsmetak.entity.Protocol;
import cz.ondrejsmetak.other.XmlParserException;
import cz.ondrejsmetak.tool.Helper;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Parses configuration
 *
 * @author Ondřej Směták <posta@ondrejsmetak.cz>
 */
public class ConfigurationParser extends BaseParser {

	public static final String FILE = "configuration.xml";

	private static final String TAG_CONFIGURATION = "configuration";
	private static final String TAG_DIRECTIVES = "directives";
	private static final String TAG_DIRECTIVE = "directive";
	private static final String TAG_PROTOCOLS = "protocols";
	private static final String TAG_HIGHEST_SUPPORTED_PROTOCOL = "highestSupportedProtocol";
	private static final String TAG_OTHER = "other";
	private static final String TAG_TLS_FALLBACK_SCSV = "tlsFallbackScsv";
	private static final String TAG_CIPHER_SUITES = "cipherSuites";
	private static final String TAG_CIPHER_SUITE = "cipherSuite";

	private static final String ATTRIBUTE_NAME = "name";
	private static final String ATTRIBUTE_VALUE = "value";
	private static final String ATTRIBUTE_MODE = "mode";

	@Override
	public void createDefault() throws IOException {
		InputStream source = ResourceManager.getDefaultConfigurationXml();
		Path destination = new File(FILE).toPath();
		Files.copy(source, destination);
	}

	@Override
	public boolean hasFile() {
		return Files.exists(new File(FILE).toPath());
	}

	/**
	 * Checks if node is recognized by this parser. If not, exception is thrown.
	 * We are strict during parsing content of xml. Only supported tags and
	 * atributes must be used.
	 *
	 * @param node node, that will be checked
	 * @throws XmlParserException
	 */
	private void checkNode(Node node) throws XmlParserException {
		ArrayList supportedTags = new ArrayList<>(Arrays.asList(new String[]{
			TAG_CONFIGURATION,
			TAG_DIRECTIVES,
			TAG_DIRECTIVE,
			TAG_PROTOCOLS,
			TAG_HIGHEST_SUPPORTED_PROTOCOL,
			TAG_CIPHER_SUITES,
			TAG_CIPHER_SUITE,
			TAG_OTHER,
			TAG_TLS_FALLBACK_SCSV}));

		if (!supportedTags.contains(node.getNodeName())) {
			throw new XmlParserException("Unknown tag [%s]. You must use only supported tags!", node.getNodeName());
		}

		/**
		 * Tag "directive" must have "name" and "value" atribute
		 */
		if (node.getNodeName().equals(TAG_DIRECTIVE)) {
			checkAttributesOfNode(node, ATTRIBUTE_NAME, ATTRIBUTE_VALUE);
		}

		/**
		 * Tag "protocol" must have "name" and "mode" atribute
		 */
		if (node.getNodeName().equals(TAG_DIRECTIVE)) {
			checkAttributesOfNode(node, ATTRIBUTE_NAME, ATTRIBUTE_VALUE);
		}

		/**
		 * Tag "tlsFallbackScsv" must have "mode" atribute
		 */
		if (node.getNodeName().equals(TAG_TLS_FALLBACK_SCSV)) {
			checkAttributesOfNode(node, ATTRIBUTE_MODE);
		}

		/**
		 * Tag "cipherSuite" must have "name" and "mode" atribute
		 */
		if (node.getNodeName().equals(TAG_CIPHER_SUITE)) {
			checkAttributesOfNode(node, ATTRIBUTE_NAME, ATTRIBUTE_MODE);
		}
	}

	/**
	 * Parse configuration file and store directives
	 *
	 * @throws XmlParserException
	 */
	public void parse() throws XmlParserException {
		try {
			File fXmlFile = new File(FILE);
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(fXmlFile);
			doc.getDocumentElement().normalize();

			NodeList tags = doc.getElementsByTagName("*");
			NodeList configurations = doc.getElementsByTagName(TAG_CONFIGURATION);

			if (configurations.getLength() != 1 || !(configurations.item(0) instanceof Element)) {
				throw new XmlParserException(String.format("Tag [%s] must be specified exatly once.", TAG_CONFIGURATION));
			}

			Element configuration = (Element) configurations.item(0);

			/**
			 * Parse configuration directives and store them
			 */
			for (int i = 0; i < tags.getLength(); i++) {
				Node node = tags.item(i);
				checkNode(node);

				if (node.getNodeType() == Node.ELEMENT_NODE) {

					/**
					 * Directives
					 */
					parseDirectives((Element) node);

					/**
					 * Cipher suites
					 */
					parseCipherSuites((Element) node);
				}
			}

			/**
			 * Highest supported protocol
			 */
			Protocol highestSupportedProtocol = parseHighestSupportedProtocol(getElementByTagName(configuration, TAG_HIGHEST_SUPPORTED_PROTOCOL));
			if (highestSupportedProtocol != null) {
				ConfigurationRegister.getInstance().setHighestSupportedProtocol(highestSupportedProtocol);
			}

			/**
			 * TLS_FALLBACK_SCSV
			 */
			Mode tlsFallbackScsv = parseTlsFallbackScsv(getElementByTagName(configuration, TAG_TLS_FALLBACK_SCSV));
			if (tlsFallbackScsv != null) {
				ConfigurationRegister.getInstance().setTlsFallbackScsv(tlsFallbackScsv);
				CipherSuite cs = new CipherSuite(CipherSuiteRegister.TLS_FALLBACK_SCSV_HEX, CipherSuiteRegister.TLS_FALLBACK_SCSV_NAME, tlsFallbackScsv);
				CipherSuiteRegister.getInstance().addCipherSuite(cs);
			}
		} catch (ParserConfigurationException | SAXException | IllegalArgumentException | IOException ex) {
			throw new XmlParserException(ex);
		}
	}

	/**
	 * Parse "<highestSupportedProtocol>" tag
	 *
	 * @param node tag
	 * @return protocol, that will be used during analysis of Client Hello
	 * @throws XmlParserException in case of any error
	 */
	private Protocol parseHighestSupportedProtocol(Node node) throws XmlParserException {
		if (!(node instanceof Element) || !((Element) node).getTagName().equals(TAG_HIGHEST_SUPPORTED_PROTOCOL)) {
			return null;
		}
		checkAttributesOfNode(node, ATTRIBUTE_MODE, ATTRIBUTE_NAME);

		Element element = (Element) node;
		Mode mode = parseMode(element.getAttribute(ATTRIBUTE_MODE), TAG_HIGHEST_SUPPORTED_PROTOCOL);
		Protocol protocol = new Protocol(element.getAttribute(ATTRIBUTE_NAME), mode);

		return protocol;
	}

	/**
	 * Parse "<tlsFallbackScsv>" tag
	 *
	 * @param node tag
	 * @return mode used during analysis of TLS_FALLBACK_SCSV
	 * @throws XmlParserException in case of any error
	 */
	private Mode parseTlsFallbackScsv(Node node) throws XmlParserException {
		if (!(node instanceof Element) || !((Element) node).getTagName().equals(TAG_TLS_FALLBACK_SCSV)) {
			return null;
		}

		Element element = (Element) node;
		Mode mode = parseMode(element.getAttribute(ATTRIBUTE_MODE), TAG_TLS_FALLBACK_SCSV);

		return mode;
	}

	/**
	 * Parse parent "<directives>" tag
	 *
	 * @param node parent tag
	 * @throws XmlParserException in case of any error
	 */
	private void parseDirectives(Element node) throws XmlParserException {
		if (node.getTagName().equals(TAG_DIRECTIVES)) {
			NodeList directives = node.getElementsByTagName(TAG_DIRECTIVE);
			for (int i = 0; i < directives.getLength(); i++) {
				parseDirective(directives.item(i));
			}
		}
	}

	/**
	 * Parse tag, that contains configuration directive
	 *
	 * @param node tag with configuration directive
	 * @throws XmlParserException in case of any error
	 */
	private void parseDirective(Node node) throws XmlParserException {
		if (!(node instanceof Element)) {
			return;
		}

		Element element = (Element) node;
		String name = element.getAttribute(ATTRIBUTE_NAME);
		String value = element.getAttribute(ATTRIBUTE_VALUE);
		setDirective(name, value);
	}

	/**
	 * Sets given value to given directive
	 *
	 * @param name name of configuration directive
	 * @param value value of configuration directive
	 * @throws XmlParserException in case of any error
	 */
	private void setDirective(String name, String value) throws XmlParserException {
		setDebug(name, value);
		setLocalPort(name, value);
		setRemotePort(name, value);
		setRemoteHost(name, value);
	}

	/**
	 * Sets directive, that can turn on debug mode
	 *
	 * @param name name of directive
	 * @param value value of directive
	 * @throws XmlParserException if given value has unsupported format
	 */
	private void setDebug(String name, String value) throws XmlParserException {
		if (name.equalsIgnoreCase(ConfigurationRegister.DEBUG)) {
			if (!Helper.isBooleanStr(value)) {
				throw new XmlParserException("Value for directive " + ConfigurationRegister.DEBUG + " must be [true] or [false]!");
			}

			ConfigurationRegister.getInstance().setDebug(Helper.parseBooleanStr(value));
		}
	}

	/**
	 * Sets port, that is being used by proxy server
	 *
	 * @param name name of directive
	 * @param value value of directive
	 * @throws XmlParserException if given value has unsupported format
	 */
	private void setLocalPort(String name, String value) throws XmlParserException {
		if (name.equalsIgnoreCase(ConfigurationRegister.LOCAL_PORT)) {
			checkPort(value, ConfigurationRegister.LOCAL_PORT);

			ConfigurationRegister.getInstance().setLocalPort(Integer.parseInt(value));
		}
	}

	private void setRemotePort(String name, String value) throws XmlParserException {
		if (name.equalsIgnoreCase(ConfigurationRegister.REMOTE_PORT)) {
			checkPort(value, ConfigurationRegister.REMOTE_PORT);

			ConfigurationRegister.getInstance().setRemotePort(Integer.parseInt(value));
		}
	}

	private void checkPort(String value, String directiveName) throws XmlParserException {
		if (!Helper.isInteger(value)) {
			throw new XmlParserException("Value for directive " + directiveName + " must be integer value!");
		}
		Integer port = Integer.parseInt(value);
		if (!((port >= 1 && port <= 65535))) {
			throw new XmlParserException("Value for directive " + directiveName + " must be in range [1 - 65535]!");
		}
	}

	private void setRemoteHost(String name, String value) throws XmlParserException {
		if (name.equalsIgnoreCase(ConfigurationRegister.REMOTE_HOST)) {
			ConfigurationRegister.getInstance().setRemoteHost(value);
		}
	}

	/**
	 * Parse parent "cipherSuites" tag and all child "cipherSuite" tags
	 *
	 * @param node tag representing "cipherSuites"
	 * @throws XmlParserException in case of any error
	 */
	private void parseCipherSuites(Element node) throws XmlParserException {
		if (node.getTagName().equals(TAG_CIPHER_SUITES)) {
			NodeList cipherSuites = node.getElementsByTagName(TAG_CIPHER_SUITE);

			for (int i = 0; i < cipherSuites.getLength(); i++) {
				parseCipherSuite(cipherSuites.item(i));
			}
		}
	}

	/**
	 * Parse "cipherSuite" tag
	 *
	 * @param node tag representing "cipherSuite"
	 * @throws XmlParserException in case of any error
	 */
	private void parseCipherSuite(Node node) throws XmlParserException {
		if (!(node instanceof Element)) {
			return;
		}
		checkAttributesOfNode(node, ATTRIBUTE_NAME, ATTRIBUTE_MODE);

		Element element = (Element) node;
		String name = element.getAttribute(ATTRIBUTE_NAME);
		Mode mode = parseMode(element.getAttribute(ATTRIBUTE_MODE), TAG_CIPHER_SUITE);

		CipherSuiteRegister.getInstance().setModeForCipherSuite(name, mode);
	}

}
