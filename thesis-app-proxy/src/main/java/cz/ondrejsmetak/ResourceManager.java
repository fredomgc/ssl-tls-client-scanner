package cz.ondrejsmetak;

import java.io.InputStream;

/**
 * Resource manager
 *
 * @author Ondřej Směták <posta@ondrejsmetak.cz>
 */
public class ResourceManager {

	/**
	 * Return file, that is used as default configuration
	 *
	 * @return file used as default configuration
	 */
	public static InputStream getDefaultConfigurationXml() {
		return getResourceAsStream("configuration.xml");
	}

	/**
	 * Return file, that is used as template during generating HTML report
	 *
	 * @return file used as template during generating HTML report
	 */
	public static InputStream getHtmlTemplate() {
		return getResourceAsStream("template.html");
	}

	/**
	 * Finds resource in "resource" folder and returns it
	 *
	 * @param name name of resource, that will be found in folder
	 * @return stream of resource
	 */
	public static InputStream getResourceAsStream(String name) {
		return ResourceManager.class.getResourceAsStream("/" + name);
	}

}
