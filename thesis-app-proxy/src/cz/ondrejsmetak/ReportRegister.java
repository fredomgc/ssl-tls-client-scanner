package cz.ondrejsmetak;

import cz.ondrejsmetak.entity.ReportMessage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Holds messages, that will be used during creation of report
 *
 * @author Ondřej Směták <posta@ondrejsmetak.cz>
 */
public class ReportRegister {

	/**
	 * Instance of this class
	 */
	private static ReportRegister instance = null;

	/**
	 * Register of all messager
	 */
	private List<ReportMessage> register = new ArrayList<>();

	/**
	 * Returns a instance of this class
	 *
	 * @return instance of this class
	 */
	public static ReportRegister getInstance() {
		if (instance == null) {
			instance = new ReportRegister();
		}
		return instance;
	}

	/**
	 * Adds message to this register
	 *
	 * @param messages message, that will be added
	 */
	public void addReportMessages(Collection<ReportMessage> messages) {
		register.addAll(messages);
	}

	/**
	 * Returns collection of all messages in this register
	 *
	 * @return collection of all messages
	 */
	public List<ReportMessage> getReportMessages() {
		return register;
	}
}
