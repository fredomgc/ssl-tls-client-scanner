package cz.ondrejsmetak.scanner;

import cz.ondrejsmetak.CipherSuiteRegister;
import cz.ondrejsmetak.ConfigurationRegister;
import cz.ondrejsmetak.entity.CipherSuite;
import cz.ondrejsmetak.entity.ClientHello;
import cz.ondrejsmetak.entity.Protocol;
import cz.ondrejsmetak.entity.ReportMessage;
import cz.ondrejsmetak.entity.Mode;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Ondřej Směták <posta@ondrejsmetak.cz>
 */
public class ClientHelloScanner extends BaseScanner {

	private ClientHello clientHello;

	private List<ReportMessage> reportMessages = new ArrayList<>();

	public ClientHelloScanner(ClientHello clientHello) {
		this.clientHello = clientHello;
		doScan();
	}

	public List<ReportMessage> getReportMessages() {
		return reportMessages;
	}

	private void doScan() {
		doScanProtocols();
		doScanTlsFallbackScsv();
		doScanCipherSuites();
	}

	private void doScanCipherSuites() {
		for (CipherSuite cipherSuite : CipherSuiteRegister.getInstance().getCipherSuites()) {
			if (cipherSuite.getHex().equals(CipherSuiteRegister.TLS_FALLBACK_SCSV_HEX)) {
				continue; //skip this cipher suite, we have own test in method "doScanTlsFallbackScsv"
			}
			String message = null;

			if (cipherSuite.getMode().isCanBe()) {
				//in this case, we don't care
			} else if (cipherSuite.getMode().isMustBe() && !clientHello.getCipherSuites().contains(cipherSuite)) {
				message = String.format("Cipher suite [%s] MUST BE supported, but is missing in Client Hello!", cipherSuite.toString());
			} else if (cipherSuite.getMode().isMustNotBe() && clientHello.getCipherSuites().contains(cipherSuite)) {
				message = String.format("Cipher suite [%s] MUST NOT BE supported, but is present in Client Hello!", cipherSuite.toString());
			}

			if (message != null) {
				ReportMessage rp = new ReportMessage(message, ReportMessage.Category.CIPHER, cipherSuite.getMode(), ReportMessage.Type.ERROR);
				reportMessages.add(rp);
			}
		}

	}

	private void doScanTlsFallbackScsv() {
		Mode mode = ConfigurationRegister.getInstance().getTlsFallbackScsv();
		String message = null;

		if (mode.isCanBe()) {
			//in this case, we don't care
		} else if (mode.isMustBe() && !this.clientHello.isTlsFallbackScsv()) {
			message = "TLS_FALLBACK_SCSV MUST BE supported, but this cipher suite IS supported!";
		} else if (mode.isMustNotBe() && this.clientHello.isTlsFallbackScsv()) {
			message = "TLS_FALLBACK_SCSV MUST NOT BE supported, but this cipher suite IS supported!";
		}

		if (message != null) {
			ReportMessage rp = new ReportMessage(message, ReportMessage.Category.CIPHER, mode, ReportMessage.Type.ERROR);
			reportMessages.add(rp);
		}
	}

	private void doScanProtocols() {
		Protocol highestSupportedProtocol = ConfigurationRegister.getInstance().getHighestSupportedProtocol();
		String message = null;

		if (highestSupportedProtocol.getMode().isCanBe()) {
			//in this case, we don't care
		} else if (highestSupportedProtocol.getMode().isMustBe()) {
			List<Protocol> missing = new ArrayList<>(ConfigurationRegister.getInstance().getSupportedProtocols());
			missing.removeAll(clientHello.getSupportedProtocolsDuringHandshake());

			List<Protocol> indwelling = new ArrayList<>(clientHello.getSupportedProtocolsDuringHandshake());
			indwelling.removeAll(ConfigurationRegister.getInstance().getSupportedProtocols());

			if (!missing.isEmpty()) {
				message = String.format("Highest supported protocol MUST BE [%s], but following protocol(s) is/are NOT supported: %s!",
						highestSupportedProtocol.toString(), missing);
			}

			if (!indwelling.isEmpty()) {
				message = String.format("Highest supported protocol MUST BE [%s], but following protocol(s) is/are ALSO supported: %s!",
						highestSupportedProtocol.toString(), indwelling);
			}
		} else if (highestSupportedProtocol.getMode().isMustNotBe()) {

			if (clientHello.getSupportedProtocolsDuringHandshake().contains(highestSupportedProtocol)) {
				message = String.format("Highest supported protocol MUST NOT BE [%s], but this protocol IS supported!",
						highestSupportedProtocol);
			}
		}

		if (message != null) {
			ReportMessage rp = new ReportMessage(message, ReportMessage.Category.PROTOCOL, highestSupportedProtocol.getMode(), ReportMessage.Type.ERROR);
			reportMessages.add(rp);
		}
	}

}
