package cz.ondrejsmetak;

import cz.ondrejsmetak.export.HtmlExport;
import cz.ondrejsmetak.other.XmlParserException;
import cz.ondrejsmetak.parser.ConfigurationParser;
import cz.ondrejsmetak.tool.Log;
import java.io.IOException;
import java.util.Scanner;

/**
 *
 * @author Ondřej Směták <posta@ondrejsmetak.cz>
 */
public class Controller {

	private ProxyServer proxy = new ProxyServer();

	public void run() throws XmlParserException, IOException {
		startup();
		doMenu();
		dispose();
	}

	private void doMenu() {
		boolean headerPrinted = false;
		try (Scanner scanner = new Scanner(System.in)) {
			String userInput = null;

			while (!"0".equals(userInput)) {
				if (!headerPrinted) {
					System.out.print(getHeader());
					headerPrinted = true;
				}
				System.out.print("Type number of choice: ");

				userInput = scanner.nextLine();
				if ("0".equals(userInput)) {
					/*do something before application exit ?*/
				} /*else if ("1".equals(userInput)) {
					userInput = null;
				} */ else {
					System.out.println("Unrecognized input. You must type valid integer value!");
					userInput = null;
				}
			}
		}
	}

	private String getHeader() {
		StringBuilder sb = new StringBuilder();
		sb.append("SSL/TLS proxy server is running on port ").append(ConfigurationRegister.getInstance().getPort()).append(".").append("\n");
		sb.append("Available choices: ").append("\n");
		sb.append("0) Shutdown proxy sever and save log of captured events").append("\n");
		sb.append("\n");
		return sb.toString();
	}

	private void startup() throws XmlParserException {
		ConfigurationParser parser = new ConfigurationParser();
		parser.parse();

		proxy.run();
	}

	private void dispose() throws XmlParserException, IOException {
		Log.infoln("Shutting down proxy server...");
		proxy.stop();
		Log.infoln("Proxy server closed.");

		doHtmlExport();
	}

	private void doHtmlExport() throws IOException {
		HtmlExport export = new HtmlExport();
		String path = export.export(ReportRegister.getInstance().getReportMessages());

		Log.infoln("Log saved in " + path);
	}

}
