package cz.ondrejsmetak.parser;

import cz.ondrejsmetak.entity.Mode;
import cz.ondrejsmetak.entity.Mode.Type;
import cz.ondrejsmetak.other.XmlParserException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Base abstract class for all parsers in application
 *
 * @author Ondřej Směták <posta@ondrejsmetak.cz>
 */
public abstract class BaseParser {

	public abstract void createDefault() throws IOException;

	public abstract boolean hasFile();

	/**
	 * Parse mode from string value
	 *
	 * @param codename string value of mode
	 * @param tagName name of tag, that contains mode value, that will be just
	 * parsed
	 * @param forbidden collection of forbidden values
	 * @return mode
	 * @throws XmlParserException if forbidden value is used
	 */
	protected Mode parseMode(String codename, String tagName, Mode.Type... forbidden) throws XmlParserException {
		Mode mode = null;
		try {
			mode = new Mode(codename);
		} catch (IllegalArgumentException ex) {
			throw new XmlParserException(ex);
		}

		for (Type type : forbidden) {
			if (type.equals(mode.getType())) {
				throw new XmlParserException("Mode value [%s] is forbidden in tag [%s]", type, tagName);
			}
		}

		return mode;
	}

	/**
	 * Returns first and only tag with given name
	 *
	 * @param source parent tag
	 * @param tagName child tag
	 * @return child tag with given name or null otherwise
	 * @throws XmlParserException if no child tags found
	 */
	protected Element getElementByTagName(Element source, String tagName) throws XmlParserException {
		NodeList candidates = source.getElementsByTagName(tagName);

		if (candidates.getLength() == 0) {
			throw new XmlParserException("Tag [%s] not found in XML file!", tagName);
		}

		if (candidates.getLength() != 1) {
			return null; //logic error
		}

		Node node = candidates.item(0);

		if (node.getNodeType() != Node.ELEMENT_NODE) {
			return null;
		}

		return (Element) node;
	}

	/**
	 * Returns list of all atributes of given tag
	 *
	 * @param tag tag, that will be checked
	 * @return collection of atributes
	 */
	protected List<String> getAttributesByTag(Node tag) {
		List<String> done = new ArrayList<>();

		NamedNodeMap attributes = tag.getAttributes();
		for (int i = 0; i < attributes.getLength(); i++) {
			Node attribute = attributes.item(i);
			done.add(attribute.getNodeName());
		}

		return done;
	}

	/**
	 * Checks, if tag contains only expected atributes
	 *
	 * @param node tag, that will be checked
	 * @param expectedAttributes collection of expected atributes. Can be empty
	 * @throws XmlParserException if tag contains unexpected atribute
	 */
	protected void checkAttributesOfNode(Node node, String... expectedAttributes) throws XmlParserException {
		List<String> expectedAttributesList = new ArrayList<>(Arrays.asList(expectedAttributes));
		List<String> actualAttributes = getAttributesByTag(node);

		if (expectedAttributesList.isEmpty()) {
			//in this case, we don't expect any attribute
			if (!actualAttributes.isEmpty()) {
				throw new XmlParserException("Following attribute(s) %s is/are not recognized for tag [%s]!", actualAttributes, node.getNodeName());
			}
		} else {
			//in this case, we expect only specified attribute(s)
			for (String actualAttribute : getAttributesByTag(node)) {

				if (!expectedAttributesList.contains(actualAttribute)) {
					throw new XmlParserException("Unknown attribute [%s]. You must use only supported attributes!", actualAttribute);
				}
			}
		}
	}

}
