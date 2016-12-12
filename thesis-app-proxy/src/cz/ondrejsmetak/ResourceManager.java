package cz.ondrejsmetak;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import javafx.scene.image.Image;

/**
 * Resource manager
 *
 * @author Ondřej Směták <posta@ondrejsmetak.cz>
 */
public class ResourceManager {

	
	public static File getDefaultConfigurationXml() {
		return new File(getResource("configuration.xml").getFile());
	}
	
	
	public static File getDefaultTargetsXml() {
		return new File(getResource("targets.xml").getFile());
	}
	
	public static File getHtmlTemplate() {
		return new File(getResource("template.html").getFile());
	}

	/**
	 * Finds resource in "resources" folder and returns it
	 *
	 * @param name name of resource, that will be found in folder
	 * @return URL of resource
	 */
	public static URL getResource(String name) {
		return ResourceManager.class.getResource("resources/" + name);
	}

	/**
	 * Finds resource in "resources" folder and returns it
	 *
	 * @param name name of resource, that will be found in folder
	 * @return stream of resource
	 */
	public static InputStream getResourceAsStream(String name) {
		return ResourceManager.class.getResourceAsStream("resources/" + name);
	}

}
