package cz.ondrejsmetak.parser;

import cz.ondrejsmetak.CipherSuiteRegister;
import cz.ondrejsmetak.entity.CipherSuite;
import cz.ondrejsmetak.entity.Mode;
import cz.ondrejsmetak.other.XmlParserException;
import cz.ondrejsmetak.ResourceManager;
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
 *
 * @author Ondřej Směták <posta@ondrejsmetak.cz>
 */
public class CipherParser extends BaseParser {

	public static final String FILE = "ciphers.xml";

	private static final String TAG_CIPHER_SUITES = "cipherSuites";
	private static final String TAG_CIPHER_SUITE = "cipherSuite";

	private static final String ATTRIBUTE_HEX_VALUE = "hexValue";
	private static final String ATTRIBUTE_NAME = "name";

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

	private void checkNode(Node node) throws XmlParserException {
		ArrayList supportedTags = new ArrayList<>(Arrays.asList(new String[]{
			TAG_CIPHER_SUITES,
			TAG_CIPHER_SUITE}));

		if (!supportedTags.contains(node.getNodeName())) {
			throw new XmlParserException("Unknown tag [%s]. You must use only supported tags!", node.getNodeName());
		}

		/**
		 * Tag "cipherSuite" must have "name" and "hexValue" atribute
		 */
		if (node.getNodeName().equals(TAG_CIPHER_SUITE)) {
			checkAttributesOfNode(node, ATTRIBUTE_HEX_VALUE, ATTRIBUTE_NAME);
		}
	}

	public void parse() throws XmlParserException {
		try {
			File fXmlFile = new File(FILE);
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(fXmlFile);
			doc.getDocumentElement().normalize();

			NodeList tags = doc.getElementsByTagName("*");
			NodeList cipherSuites = doc.getElementsByTagName(TAG_CIPHER_SUITES);

			if (cipherSuites.getLength() != 1 || !(cipherSuites.item(0) instanceof Element)) {
				throw new XmlParserException(String.format("Tag [%s] must be specified exatly once.", TAG_CIPHER_SUITES));
			}

			Element configuration = (Element) cipherSuites.item(0);

			/**
			 * Parse cipher suites and store them
			 */
			for (int i = 0; i < tags.getLength(); i++) {
				Node node = tags.item(i);
				checkNode(node);

				if (node.getNodeType() == Node.ELEMENT_NODE) {

					/**
					 * Cipher suite
					 */
					parseCipherSuite((Element) node);
				}
			}

		} catch (ParserConfigurationException | SAXException | IllegalArgumentException | IOException ex) {
			throw new XmlParserException(ex);
		}
	}

	private void parseCipherSuite(Node node) throws XmlParserException {
		if (!(node instanceof Element)) {
			return;
		}

		Element element = (Element) node;

		if (element.getTagName().equals(TAG_CIPHER_SUITE)) {
			String hexValue = element.getAttribute(ATTRIBUTE_HEX_VALUE);
			checkHexValue(hexValue, 4);
			String name = element.getAttribute(ATTRIBUTE_NAME);

			CipherSuite cipherSuite = new CipherSuite(hexValue, name, new Mode(Mode.Type.CAN_BE));

			if (cipherSuite.getHex().equals(CipherSuiteRegister.TLS_FALLBACK_SCSV_HEX)) {
				throw new XmlParserException("Cipher suite TLS_FALLBACK_SCSV is supported by directive \"tlsFallbackScsv\". "
						+ "Please, remove this cipher suite from \"cipherSuites\" section!", cipherSuite);
			}

			if (CipherSuiteRegister.getInstance().containsCipherSuite(cipherSuite)) {
				throw new XmlParserException("Cipher suite [%s] is already present!", cipherSuite);
			}

			CipherSuiteRegister.getInstance().addCipherSuite(cipherSuite);
		}
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
