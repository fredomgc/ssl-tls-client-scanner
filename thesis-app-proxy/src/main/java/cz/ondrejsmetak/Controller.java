package cz.ondrejsmetak;

import cz.ondrejsmetak.entity.Mode;
import cz.ondrejsmetak.entity.ReportClientHello;
import cz.ondrejsmetak.entity.ReportMessage;
import cz.ondrejsmetak.export.HtmlExport;
import cz.ondrejsmetak.other.XmlParserException;
import cz.ondrejsmetak.parser.CipherParser;
import cz.ondrejsmetak.parser.ConfigurationParser;
import cz.ondrejsmetak.tool.Log;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

/**
 * Main controller of application
 *
 * @author Ondřej Směták <posta@ondrejsmetak.cz>
 */
public class Controller {

	/**
	 * Proxy server
	 */
	private ProxyServer proxy = new ProxyServer();

	/**
	 * Timestamp of moment, when proxy server started
	 */
	private Date timestampOfStart;

	/**
	 * Run a whole life cycle of application
	 *
	 * @throws XmlParserException in case of any error related to parsing XML
	 * @throws IOException in case of any error
	 * @return true, if no vulnerable behaviour captured, false otherwise
	 */
	public boolean run() throws XmlParserException, IOException {
		boolean ok = startup();
		if (!ok) {
			return false;
		}

		doMenu();
		return dispose();
	}

	/**
	 * Prints and hanles console based menu
	 */
	private void doMenu() {
		boolean headerPrinted = false;
		try (Scanner scanner = new Scanner(System.in)) {
			String userInput = null;

			while (!"0".equals(userInput)) {
				if (!headerPrinted) {
					System.out.print(getMenuHeader());
					headerPrinted = true;
				}
				System.out.print("Type number of choice: ");

				userInput = scanner.nextLine();
				if ("0".equals(userInput)) {
					/*do something before application exit ?*/
				} else if ("1".equals(userInput)) {
					ProxyServer.keystore = "two";
					proxy.reload();
					
//					System.setProperty("javax.net.ssl.keyStore", "/home/fredomgc/Plocha/keystore/mySrvKeystore2");
//					System.setProperty("javax.net.ssl.keyStorePassword", "lollol");
//
//					System.out.print("KeyStore changed");
//
//					System.err.println("A: " + HttpsURLConnection.getDefaultSSLSocketFactory());
//					
//					KeyStore ts;
//					try {
//						ts = KeyStore.getInstance("JKS");
//
//						ts.load(new FileInputStream(new File("/home/fredomgc/Plocha/keystore/mySrvKeystore2")), "lollol".toCharArray());
//
//						KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
//						kmf.init(ts, "lollol".toCharArray());
//						
//						TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
//						tmf.init(ts);
//
//						SSLContext sslContext = SSLContext.getInstance("TLS");
//						sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
//
//						HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
//						
//						//ProxyServer.custom = sslContext.getServerSocketFactory();
//						
//						//SSLSocketFactory
//						//sslContext.getSocketFactory().
//						
//						
//					} catch (Exception ex) {
//						ex.printStackTrace();
//					}
//
//					proxy.stop();
//					//proxy.reload();
//					
//					proxy.run();
					
					userInput = null;
				} else {
					System.out.println("Unrecognized input. You must type supported integer value!");
					userInput = null;
				}
			}
		}
	}

	/**
	 * Returns text header of console based menu
	 *
	 * @return header of console based menu
	 */
	private String getMenuHeader() {
		StringBuilder sb = new StringBuilder();
		sb.append("SSL/TLS proxy server is running on port ").append(ConfigurationRegister.getInstance().getLocalPort()).append(".").append("\n");
		sb.append("Available choices: ").append("\n");
		sb.append("0) Shutdown proxy sever and save log of captured events").append("\n");
		sb.append("1) Change keystore").append("\n");
		sb.append("\n");
		return sb.toString();
	}

	/**
	 * Runs actions required to transition to listening of SSL/TLS communication
	 *
	 * @return true, if transition was succesfull, false otherwise
	 * @throws XmlParserException in case of any error
	 */
	private boolean startup() throws XmlParserException, IOException {
		ConfigurationParser configurationParser = new ConfigurationParser();
		CipherParser cipherParser = new CipherParser();

		boolean stop = false;

		if (!configurationParser.hasFile()) {
			configurationParser.createDefault();
			Log.infoln("Creating default " + ConfigurationParser.FILE + " in application folder.");
			Log.infoln("Please review configuration in XML file and run application again.");
			stop = true;
		}

		if (stop) {
			return false;
		}

		cipherParser.parse();

		Log.infoln("Parsing " + ConfigurationParser.FILE + " for application configuration.");
		configurationParser.parse();

		List<String> missingDirectives = ConfigurationRegister.getInstance().getMissingDirectives();
		if (!missingDirectives.isEmpty()) {
			Log.errorln("Following configurationd directives are missing " + missingDirectives + ", can't continue without them!");
			return false;
		}

		boolean ok = ProxyServer.checkPort();
		if (!ok) {
			return false;
		}

		proxy.run();
		timestampOfStart = new Date();
		return true;
	}

	/**
	 * Runs actions related with shutting down proxy server
	 *
	 * @throws XmlParserException in case of any error related to parsing XML
	 * @throws IOException in case of any error
	 * @return true, if no vulnerable behaviour captured, false otherwise
	 */
	private boolean dispose() throws XmlParserException, IOException {
		Log.infoln("Shutting down proxy server...");
		proxy.stop();
		Log.infoln("Proxy server closed.");

		doHtmlExport();

		return ReportRegister.getInstance().getReportsClientHello().isEmpty();
	}

	/**
	 * Returns collection of report messages, that will be contained in HTML
	 * export
	 *
	 * @return collection of report messages
	 */
	private List<ReportMessage> getReportMessages() {
		if (!ReportRegister.getInstance().getReportsClientHello().isEmpty()) {
			return new ArrayList<>(); //nothing to report
		}

		ReportMessage rm = new ReportMessage("No SSL/TLS communication was recorded.", ReportMessage.Category.OTHER, new Mode(Mode.Type.MUST_BE), ReportMessage.Type.ERROR);
		return new ArrayList<>(Arrays.asList(new ReportMessage[]{rm}));
	}

	/**
	 * Returns collection of report Client Hello messages, that will be
	 * contained in HTML export
	 *
	 * @return collection of report Client Hello messages
	 */
	private List<ReportClientHello> getReportClientHello() {
		List<ReportClientHello> done = new ArrayList<>();

		for (ReportClientHello report : ReportRegister.getInstance().getReportsClientHello()) {
			if (!report.getReportMessages().isEmpty()) {
				done.add(report);
			} else {
				ReportMessage safeMessage = new ReportMessage("All tests performed with the given configuration over captured Client Hello passed successfully.", ReportMessage.Category.OTHER, null, ReportMessage.Type.SUCCESS);
				ReportClientHello safeReport = new ReportClientHello(report.getClientHelloId(), new ArrayList<>(Arrays.asList(new ReportMessage[]{safeMessage})));
				done.add(safeReport);
			}
		}

		return done;
	}

	/**
	 * Executes HTML export
	 *
	 * @throws IOException in case of any error
	 */
	private void doHtmlExport() throws IOException {

		HtmlExport export = new HtmlExport();
		String path = export.export(getReportMessages(), getReportClientHello(), timestampOfStart);

		Log.infoln("Log saved in " + path);
	}

}
