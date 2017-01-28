package cz.ondrejsmetak;

import cz.ondrejsmetak.tool.Log;

/**
 * Main application class
 *
 * @author Ondřej Směták <posta@ondrejsmetak.cz>
 */
public class Application {

	/**
	 * Main method
	 * @param args arguments from command line
	 */
	public static void main(String[] args) {
		Controller controller = new Controller();
		try {
			controller.run();
		} catch (Exception ex) {
			if (ConfigurationRegister.getInstance().isDebug()) {
				Log.debugException(ex);
			} else {
				Log.errorln(ex);
			}
		}
	}
}
