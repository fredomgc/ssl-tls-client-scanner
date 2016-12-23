package cz.ondrejsmetak.scanner;

import cz.ondrejsmetak.ConfigurationRegister;
import cz.ondrejsmetak.entity.ClientHello;
import cz.ondrejsmetak.entity.Protocol;
import cz.ondrejsmetak.entity.ReportMessage;
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
	}

	//http://security.stackexchange.com/questions/29314/what-is-the-significance-of-the-version-field-in-a-tls-1-1-clienthello-message
	private void doScanProtocols() {
		List<ReportMessage> messages = new ArrayList<>();
		Protocol highestSupportedProtocol = ConfigurationRegister.getInstance().getHighestSupportedProtocol();
		String message;

		if (highestSupportedProtocol.getMode().isCanBe()) {
			//in this case, we don't care
		} else if (highestSupportedProtocol.getMode().isMustBe()) {
			List<Protocol> diff = new ArrayList<>(ConfigurationRegister.getInstance().getSupportedProtocols());
			diff.removeAll(clientHello.getSupportedProtocolsDuringHandshake());

			if (!clientHello.getSupportedProtocolsDuringHandshake().contains(highestSupportedProtocol)) {
				message = String.format("Highest supported protocol MUST BE [%s], but following protocol(s) is/are NOT supported: [%s]",
						highestSupportedProtocol.toString(), diff);
			}

		}
	}

}
