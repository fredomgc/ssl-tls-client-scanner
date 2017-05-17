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
	 *
	 * @param args arguments from command line
	 */
	public static void main(String[] args) {

		Controller controller = new Controller(parseConfigurationFileName(args));
		try {
			boolean safe = controller.run();
			System.exit(safe ? 0 : -1);
		} catch (Exception ex) {
			if (ConfigurationRegister.getInstance().isDebug()) {
				Log.debugException(ex);
			} else {
				Log.errorln(ex);
			}
		} finally {
			System.exit(-1);
		}
	}

	/**
	 * Attempts to find command line argument, that specifies configuration file
	 *
	 *
	 * @param args array of command line arguments
	 * @return name of the configuration file or null, if not found
	 */
	private static String parseConfigurationFileName(String[] args) {
		if (args.length >= 2) {
			throw new IllegalArgumentException("Too many command line arguments.");
		}

		if (args.length == 0) {
			return null;
		}

		return args[0];
	}

}
