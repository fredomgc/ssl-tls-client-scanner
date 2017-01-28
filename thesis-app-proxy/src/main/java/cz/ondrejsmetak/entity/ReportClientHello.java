package cz.ondrejsmetak.entity;

import java.util.List;

/**
 * Represents collection of messages. Messages are related to one captured
 * Client Hello.
 *
 *
 * @author Ondřej Směták <posta@ondrejsmetak.cz>
 */
public class ReportClientHello {

	/**
	 * Internal ID of captured Client Hello
	 */
	private int clientHelloId;

	/**
	 * Collection of messages related to captured Client Hello
	 */
	private List<ReportMessage> messages;

	/**
	 * Creates a new report for Client Hello
	 *
	 * @param clientHelloId internal ID of client Hello
	 * @param messages collection of report messages
	 */
	public ReportClientHello(int clientHelloId, List<ReportMessage> messages) {
		this.clientHelloId = clientHelloId;
		this.messages = messages;
	}

	public int getClientHelloId() {
		return clientHelloId;
	}

	public List<ReportMessage> getReportMessages() {
		return messages;
	}
}
