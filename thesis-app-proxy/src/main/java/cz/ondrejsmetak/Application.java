package cz.ondrejsmetak;

import cz.ondrejsmetak.tool.Log;
import java.io.IOException;
import java.lang.reflect.Field;
import java.security.Provider;
import java.security.Security;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

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
		Controller controller = new Controller();
		try {
			boolean safe = controller.run();
			
			System.err.println("Je to safe: " + safe);
			System.exit(safe ? 0 : -1);
		} catch (Exception ex) {
			if (ConfigurationRegister.getInstance().isDebug()) {
				Log.debugException(ex);
			} else {
				Log.errorln(ex);
			}
		}
	}

}
