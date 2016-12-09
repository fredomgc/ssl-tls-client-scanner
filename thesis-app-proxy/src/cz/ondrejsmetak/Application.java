package cz.ondrejsmetak;

/**
 *
 * @author Ondřej Směták <posta@ondrejsmetak.cz>
 */
public class Application {

	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		ProxyServer proxy = new ProxyServer();
		proxy.run();
	}
	
}
