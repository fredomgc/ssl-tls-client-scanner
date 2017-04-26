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
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;
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
	 * Delay between tests
	 */
	private static final int TEST_DELAY = 1000 * 10; //1O seconds

	/**
	 * Proxy server
	 */
	private ProxyServer proxy = new ProxyServer();

	/**
	 * Timestamp of moment, when proxy server started
	 */
	private Date timestampOfStart;

	/**
	 * Is infinite loop running?
	 */
	private boolean infiniteLoop = true;

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

		startInfiniteLoop();

		return dispose();
	}

	/**
	 * Starts the inifinite looú
	 */
	private void startInfiniteLoop() {
		while (infiniteLoop) {

			try {
				Thread.sleep(1000);
			} catch (InterruptedException ex) {
				Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}

	/**
	 * Stops the infinite loop
	 */
	private void stopInfiniteLoop() {
		infiniteLoop = false;
	}

	/**
	 * Schedules all future tests
	 */
	private void scheduleTests() {
		Log.infoln("Starting SSL/TLS Handshake test");

		schedule(new TimerTask() {
			@Override
			public void run() {
				Log.infoln("Stopped SSL/TLS Handshake test.");
				proxy.setSecureSocket(true);
				proxy.setClientProtocol(new Protocol(Protocol.Type.TLSv12));
				proxy.setConfigurationCertificate(ConfigurationCertificateRegister.getInstance().getFirstMustBe());
				proxy.reload();
				
			}
		}, TEST_DELAY - 500);

		
		/*
		scheduleTestsOfCertificates(() -> {
			stopInfiniteLoop();
		});*/
		scheduleTestsOfProtocols(() -> {
			scheduleTestsOfCertificates(() -> {
				stopInfiniteLoop();
			});
		});
	}

	/**
	 * Schedules futures tests of all certificates specified by a user
	 *
	 * @param callback
	 */
	private void scheduleTestsOfProtocols(Runnable callback) {
		int delay = TEST_DELAY;
		int counter = 1;

		List<Protocol> protocols = ConfigurationRegister.getInstance().getProtocols();

		for (int i = 0; i < protocols.size() + 1; i++) {
			final boolean stopTest = (i > 0);
			final boolean scheduleNext = (i < protocols.size());
			final boolean lastRun = (i == protocols.size());
			final Protocol protocol = scheduleNext ? protocols.get(i) : null;

			schedule(new TimerTask() {
				@Override
				public void run() {
					if (stopTest) {
						Log.infoln("Stopped protocol test for [%s]. Handshake occured: [%s]",
								proxy.getClientProtocol(), proxy.stopProtocolTest());
					}

					if (lastRun) {
						callback.run();
					}

					if (scheduleNext) {
						Log.infoln("Starting protocol test for [%s].", protocol);
						proxy.setClientProtocol(protocol);
						proxy.startProtocolTest();
						proxy.reload();
					}
				}
			}, delay);

			delay = TEST_DELAY * ++counter;
		}

	}

	/**
	 * Schedules futures tests of all certificates specified by a user
	 *
	 * @param callback
	 */
	private void scheduleTestsOfCertificates(Runnable callback) {
		int delay = TEST_DELAY;
		int counter = 1;
		ArrayList<ClientCertificate> certificates = ConfigurationCertificateRegister.getInstance().getConfigurationCertificatesIndexable();

		for (int i = 0; i < certificates.size() + 1; i++) {
			final boolean stopTest = (i > 0);
			final boolean scheduleNext = (i < certificates.size());
			final boolean lastRun = (i == certificates.size());
			final ClientCertificate certificate = scheduleNext ? certificates.get(i) : null;

			schedule(new TimerTask() {
				@Override
				public void run() {
					if (stopTest) {
						Log.infoln("Stopped certificate test for [%s]. Handshake occured: [%s]",
								proxy.getClientCertificate().getName(), proxy.stopCertificateTest());
					}

					if (lastRun) {
						callback.run();
					}

					if (scheduleNext) {
						Log.infoln("Starting certificate test for [%s].", certificate.getName());
						proxy.setConfigurationCertificate(certificate);
						proxy.startCertificateTest();
						proxy.reload();
					}
				}
			}, delay);

			delay = TEST_DELAY * ++counter;
		}
	}

	/**
	 * Schedules execution of the given TimerTask after specified delay
	 *
	 * @param task task that will be executed
	 * @param delay specified delay
	 */
	private void schedule(TimerTask task, int delay) {
		new Timer().schedule(task, delay);
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
		Log.infoln("Starting automated testing. Each test takes around " + TEST_DELAY / 1000 + " seconds.");
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
				ReportMessage safeMessage = new ReportMessage("All tests performed with the given configuration over captured Client Hello passed successfully.", ReportMessage.Category.OTHER, null, ReportMessage.Type.SUCCESS);
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
}
