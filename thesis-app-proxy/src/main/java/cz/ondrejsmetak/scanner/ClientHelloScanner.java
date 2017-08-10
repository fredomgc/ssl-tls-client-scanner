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

	/**
	 * Client Hello, that will be scanned
	 */
	private ClientHello clientHello;

	/**
	 * Collection of messages describing result of scans
	 */
	private List<ReportMessage> reportMessages = new ArrayList<>();

	/**
	 * Creates a new scanner for the given Client Hello
	 *
	 * @param clientHello client hello, that will be scanned
	 */
	public ClientHelloScanner(ClientHello clientHello) {
		this.clientHello = clientHello;
		doScan();
	}

	/**
	 * Returns collection of messages describing scans of Client Hello
	 *
	 * @return
	 */
	public List<ReportMessage> getReportMessages() {

		return reportMessages;
	}

	/**
	 * Scan content of Client Hello
	 */
	private void doScan() {
		//currenlty disabled. See ClientHelloScanner, method "doScanTlsFallbackScsv" for more info
		doScanCipherSuites();
	}

	/**
	 * Scan offered cipher suites
	 */
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

	/**
	 * Scan offered cipher suites for support of TLS_FALLBACK_SCSV
	 *
	 * @deprecated currently disabled REASON:
	 *
	 * "domluvili jsme se na tom, že TLS_FALLBACK_SCSV má nevýhodný poměr z
	 * hlediska pracnosti a přínosu, takže to můžete brát jako něco, co má malou
	 * prioritu. Kdyby na to náhodou zbyl čas, bylo by to fajn, ale není to
	 * nutné. Akorát dokud to nebude funkční, nemělo by se to tvářit, že to
	 * TLS_FALLBACK_SCSV testuje."
	 */
	private void doScanTlsFallbackScsv() {
		Mode mode = ConfigurationRegister.getInstance().getTlsFallbackScsv();
		String message = null;

		if (mode.isCanBe()) {
			//in this case, we don't care
		} else if (mode.isMustBe() && !this.clientHello.isTlsFallbackScsv()) {
			message = "TLS_FALLBACK_SCSV MUST BE supported, but this cipher suite IS NOT supported!";
		} else if (mode.isMustNotBe() && this.clientHello.isTlsFallbackScsv()) {
			message = "TLS_FALLBACK_SCSV MUST NOT BE supported, but this cipher suite IS supported!";
		}

		if (message != null) {
			ReportMessage rp = new ReportMessage(message, ReportMessage.Category.CIPHER, mode, ReportMessage.Type.ERROR);
			reportMessages.add(rp);
		}
	}

}
