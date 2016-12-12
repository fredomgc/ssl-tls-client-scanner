package cz.ondrejsmetak.tool;

import cz.ondrejsmetak.CipherSuiteRegister;
import cz.ondrejsmetak.ResourceManager;
import cz.ondrejsmetak.entity.CipherSuite;
import cz.ondrejsmetak.entity.Mode;
import cz.ondrejsmetak.other.XmlParserException;
import java.io.File;
import java.io.IOException;
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
 * Storage for configuration directives
 * 
 * @author Ondřej Směták <posta@ondrejsmetak.cz>
 */
public class ConfigurationParser extends BaseParser {

	public static final String FILE = "configuration.xml";

	private static final String TAG_CONFIGURATION = "configuration";
	private static final String TAG_CIPHER_SUITES = "cipherSuites";
	private static final String TAG_CIPHER_SUITE = "cipherSuite";

	private static final String ATTRIBUTE_HEX_VALUE = "hexValue";
	private static final String ATTRIBUTE_NAME = "name";
	private static final String ATTRIBUTE_MODE = "mode";

	@Override
	public void createDefault() throws IOException {
		Path source = ResourceManager.getDefaultConfigurationXml().toPath();
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
		ArrayList supportedTags = new ArrayList<>(Arrays.asList(new String[]{TAG_CONFIGURATION, TAG_CIPHER_SUITE, TAG_CIPHER_SUITES}));

		if (!supportedTags.contains(node.getNodeName())) {
			throw new XmlParserException("Unknown tag [%s]. You must use only supported tags!", node.getNodeName());
		}

		/**
		 * Tag "cipherSuite" must have "name" and "hexValue" atribute
		 */
		if (node.getNodeName().equals(TAG_CIPHER_SUITE)) {
			checkAttributesOfNode(node, ATTRIBUTE_NAME, ATTRIBUTE_HEX_VALUE, ATTRIBUTE_MODE);
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

			/**
			 * Parse configuration directives and store them
			 */
			for (int i = 0; i < tags.getLength(); i++) {
				Node node = tags.item(i);
				checkNode(node);

				if (node.getNodeType() == Node.ELEMENT_NODE) {
					parseCipherSuites((Element) node);
				}
			}

		} catch (ParserConfigurationException | SAXException | IllegalArgumentException | IOException ex) {
			throw new XmlParserException(ex);
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
		checkAttributesOfNode(node, ATTRIBUTE_NAME, ATTRIBUTE_HEX_VALUE, ATTRIBUTE_MODE);

		Element element = (Element) node;
		String name = element.getAttribute(ATTRIBUTE_HEX_VALUE);
		String hexValue = element.getAttribute(ATTRIBUTE_HEX_VALUE);
		checkHexValue(hexValue, 4);
		Mode mode = parseMode(element.getAttribute(ATTRIBUTE_MODE), TAG_CIPHER_SUITE);

		CipherSuite cipherSuite = new CipherSuite(hexValue, name, mode);
		if (CipherSuiteRegister.getInstance().containsCipherSuite(cipherSuite)) {
			throw new XmlParserException("Cipher suite [%s] is already present!", cipherSuite);
		}

		CipherSuiteRegister.getInstance().addCipherSuite(cipherSuite);
	}

	/**
	 * Check if given value is in valid hexadecimal with expected digits count
	 *
	 * @param value
	 * @param size
	 * @throws XmlParserException
	 */
	private void checkHexValue(String value, int digits) throws XmlParserException {
		if (!Helper.isHex(value)) {
			throw new XmlParserException("Value [%s] isn't valid hexadecimal value!", value);
		}

		if (value.length() != digits) {
			throw new XmlParserException("Value [%s] must have exactly [%s] digit(s)!", value, digits);
		}

	}
}
