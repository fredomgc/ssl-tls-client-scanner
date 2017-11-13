package cz.ondrejsmetak;

import cz.ondrejsmetak.entity.ClientCertificate;
import cz.ondrejsmetak.entity.Mode;
import cz.ondrejsmetak.entity.Protocol;
import cz.ondrejsmetak.entity.ReportClientHello;
import cz.ondrejsmetak.entity.ReportMessage;
import cz.ondrejsmetak.export.HtmlExport;
import cz.ondrejsmetak.other.XmlParserException;
import cz.ondrejsmetak.parser.CipherParser;
import cz.ondrejsmetak.parser.ConfigurationParser;
import cz.ondrejsmetak.tool.Helper;
import cz.ondrejsmetak.tool.Log;
import cz.ondrejsmetak.tool.Pair;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main controller of application
 *
 * @author Ondřej Směták <posta@ondrejsmetak.cz>
 */
public class Controller {

	/**
	 * Delay between tests
	 */
	private static final int TEST_DELAY_SECONDS = 10;
	private static final int TEST_DELAY_AFTER_UPDATE = 2;

	/**
	 * Proxy server
	 */
	private final ProxyServer proxy = new ProxyServer();

	/**
	 * Timestamp of moment, when proxy server started
	 */
	private Date timestampOfStart;

	/**
	 * Name of configuration file
	 */
	private String configurationFileName = null;

	/**
	 * Creates a new controller, that uses a given configuration file
	 *
	 * @param configurationFileName
	 */
	public Controller(String configurationFileName) {
		this.configurationFileName = configurationFileName;
	}

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

		scheduleTests();

		return dispose();
	}

	/**
	 * Schedules all future tests
	 */
	private void scheduleTests() {
		try {
			Thread t;

			//SSL/TLS handshake
			t = new Thread(new TestHandshake());
			t.start();
			t.join();

			//SSL/TLS protocols
			for (Protocol protocol : ConfigurationRegister.getInstance().getProtocols()) {
				t = new Thread(new TestProtocol(protocol));
				t.start();
				t.join();
			}

			//SSL/TLS certificates
			for (ClientCertificate certificate : ConfigurationCertificateRegister.getInstance().getConfigurationCertificatesIndexable()) {
				t = new Thread(new TestCertificate(certificate));
				t.start();
				t.join();
			}
		} catch (InterruptedException ex) {
			Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	/**
	 * Runs actions required to transition to listening of SSL/TLS communication
	 *
	 * @return true, if transition was succesfull, false otherwise
	 * @throws XmlParserException in case of any error
	 */
	private boolean startup() throws XmlParserException, IOException {
		ConfigurationParser configurationParser = new ConfigurationParser(configurationFileName);
		CipherParser cipherParser = new CipherParser();

		boolean stop = false;

		if (configurationFileName == null && !configurationParser.hasDefaultFile()) {
			configurationParser.createDefault();
			Log.infoln("Creating default " + ConfigurationParser.DEFAULT_FILE + " in application folder.");
			Log.infoln("Please review configuration in XML file and run application again.");
			stop = true;
		}

		if (stop) {
			return false;
		}

		cipherParser.parse();

		Log.infoln("Parsing " + configurationParser.getConfigurationFileName() + " for application configuration.");
		configurationParser.parse();

		if (!ConfigurationCertificateRegister.getInstance().hasAtLeastOneMustBe()) {
			Log.errorln("You must specify at least one certificate with mode MUST BE (will be used during initial communication)!");
			return false;
		}

		List<String> missingDirectives = ConfigurationRegister.getInstance().getMissingDirectives();
		if (!missingDirectives.isEmpty()) {
			Log.errorln("Following configurationd directives are missing " + missingDirectives + ", can't continue without them!");
			return false;
		}

		boolean ok = ProxyServer.checkPort();
		if (!ok) {
			return false;
		}

		proxy.setConfigurationCertificate(ConfigurationCertificateRegister.getInstance().getFirstMustBe());
		proxy.run();
		timestampOfStart = new Date();
		Log.infoln("MiTM proxy is up and running on the port " + ConfigurationRegister.getInstance().getLocalPort() + ".");
		Log.infoln("Starting automated testing. Each test takes " + TEST_DELAY_SECONDS + " seconds at maximum.");
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

		return !ReportRegister.getInstance().hasAtLeastOneVulnerableMessage();
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
				ReportMessage safeMessage = new ReportMessage("All tests performed with the given configuration passed successfully.", ReportMessage.Category.OTHER, null, ReportMessage.Type.SUCCESS);
				ReportClientHello safeReport = new ReportClientHello(report.getClientHelloId(), new ArrayList<>(Arrays.asList(new ReportMessage[]{safeMessage})));
				done.add(safeReport);
			}
		}

		return done;
	}

	/**
	 * Returns collection of report messages (regarding protocol tests), that
	 * will be contained in HTML export
	 *
	 * @return collection of report messages
	 */
	private List<ReportMessage> getReportProtocol() {
		return ReportRegister.getInstance().getReportsProtocol();
	}

	/**
	 * Returns collection of report messages (regarding certificate tests), that
	 * will be contained in HTML export
	 *
	 * @return collection of report messages
	 */
	private List<ReportMessage> getReportCertificate() {
		return ReportRegister.getInstance().getReportsCertificate();
	}

	/**
	 * Executes HTML export
	 *
	 * @throws IOException in case of any error
	 */
	private void doHtmlExport() throws IOException {
		HtmlExport export = new HtmlExport();
		String path = export.export(getReportMessages(), getReportClientHello(), getReportProtocol(), getReportCertificate(), timestampOfStart);
		Log.infoln("Log saved in " + path);
	}

	/**
	 * Prints current state of proxy server. Currently unused.
	 */
	private void printCurrentState() {
		Log.infoln("Protocol: " + proxy.getClientProtocol() + ", Certificate: " + proxy.getClientCertificate().getName());
	}

	/**
	 * Runnable responsible for testing handshake
	 */
	private class TestHandshake implements Runnable, Observer {

		private volatile boolean communicationOccured = false;
		private volatile Thread myThread;

		@Override
		public void run() {
			Log.infoln("Starting SSL/TLS Handshake test");
			proxy.addSingleSubscriber(this);
			myThread = Thread.currentThread();

			try {
				Thread.sleep(TimeUnit.SECONDS.toMillis(TEST_DELAY_SECONDS));
			} catch (InterruptedException ex) {
				//intentionally interruped
			}

			if (communicationOccured) {
				Log.infoln("Stopped SSL/TLS Handshake test. Communication occured.");
			} else {
				Log.infoln("Stopped SSL/TLS Handshake test. No communication occured.");
			}
		}

		@Override
		public void update(Observable o, Object arg) {
			if (!communicationOccured) {
				Helper.waitAndCall(TEST_DELAY_AFTER_UPDATE, () -> {
					myThread.interrupt();
				});
			}
			communicationOccured = true;
		}
	}

	/**
	 * Runnable responsible for testing protocol
	 */
	private class TestProtocol implements Runnable, Observer {

		private volatile boolean communicationOccured = false;
		private final Protocol protocol;
		private volatile Thread myThread;

		public TestProtocol(Protocol protocol) {
			this.protocol = protocol;
		}

		@Override
		public void run() {
			Log.infoln("Starting protocol test for [%s].", protocol);
			proxy.addSingleSubscriber(this);
			proxy.setSecureSocket(true);
			proxy.setConfigurationCertificate(ConfigurationCertificateRegister.getInstance().getFirstMustBe());
			proxy.setClientProtocol(protocol);
			proxy.startProtocolTest();
			proxy.reload();
			myThread = Thread.currentThread();

			while (proxy.isReloading()) {
			}

			try {
				Thread.sleep(TimeUnit.SECONDS.toMillis(TEST_DELAY_SECONDS));
			} catch (InterruptedException ex) {
				//intentionally interruped
			}

			if (communicationOccured) {
				Pair<Boolean, Boolean> result = proxy.stopProtocolTest();
				Log.infoln("Stopped protocol test for [%s]. Some communication occured, handshake occured: [%s], communication after handshake occured: [%s]",
						protocol, result.getLeft(), result.getRight());
			} else {
				proxy.stopProtocolTest();
				Log.infoln("Stopped protocol test for [%s]. No communication occured.", protocol);
			}
		}

		@Override
		public void update(Observable o, Object arg) {
			if (!communicationOccured) {
				Helper.waitAndCall(TEST_DELAY_AFTER_UPDATE, () -> {
					myThread.interrupt();
				});
			}
			communicationOccured = true;
		}
	}

	/**
	 * Runnable responsible for testing certificate
	 */
	private class TestCertificate implements Runnable, Observer {

		private volatile boolean communicationOccured = false;
		private final ClientCertificate certificate;
		private volatile Thread myThread;

		public TestCertificate(ClientCertificate protocol) {
			this.certificate = protocol;
		}

		@Override
		public void run() {
			Log.infoln("Starting certificate test for [%s].", certificate.getName());
			proxy.addSingleSubscriber(this);
			proxy.setSecureSocket(true);
			proxy.setConfigurationCertificate(certificate);
			proxy.startCertificateTest();
			proxy.reload();
			myThread = Thread.currentThread();

			while (proxy.isReloading()) {
			}

			try {
				Thread.sleep(TimeUnit.SECONDS.toMillis(TEST_DELAY_SECONDS));
			} catch (InterruptedException ex) {
				//intentionally interruped
			}

			if (communicationOccured) {
				Pair<Boolean, Boolean> result = proxy.stopCertificateTest();
				Log.infoln("Stopped certificate test for [%s]. Some communication occured, handshake occured: [%s], communication after handshake occured: [%s]",
						certificate.getName(), result.getLeft(), result.getRight());
			} else {
				proxy.stopCertificateTest();
				Log.infoln("Stopped protocol test for [%s]. No communication occured.", certificate.getName());
			}
		}

		@Override
		public void update(Observable o, Object arg) {
			if (!communicationOccured) {
				Helper.waitAndCall(TEST_DELAY_AFTER_UPDATE, () -> {
					myThread.interrupt();
				});
			}
			communicationOccured = true;
		}
	}
}
