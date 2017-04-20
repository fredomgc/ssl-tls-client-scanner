package cz.ondrejsmetak;

import cz.ondrejsmetak.entity.ReportClientHello;
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
	 * Collection of reports (each report for single captured Client Hello)
	 */
	private final List<ReportClientHello> clientHelloRegister = new ArrayList<>();

	/**
	 * Collection of reports (each report for single certificate)
	 */
	private final List<ReportMessage> certificateRegister = new ArrayList<>();

	/**
	 * Collection of reports (each report for single protocol)
	 */
	private final List<ReportMessage> protocolRegister = new ArrayList<>();

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

	public boolean hasAtLeastOneVulnerableMessage() {
		for (ReportClientHello report : clientHelloRegister) {
			for (ReportMessage rm : report.getReportMessages()) {
				if (rm.isTypeError()) {
					return true;
				}
			}
		}

		for (ReportMessage message : certificateRegister) {
			if (message.isTypeError()) {
				return true;
			}
		}
		
		for (ReportMessage message : protocolRegister) {
			if (message.isTypeError()) {
				return true;
			}
		}
		
		return false;

	}

	public void addReportClientHello(ReportClientHello report) {
		clientHelloRegister.add(report);
	}

	public List<ReportClientHello> getReportsClientHello() {
		return clientHelloRegister;
	}

	public void addReportCertificate(ReportMessage report) {
		certificateRegister.add(report);
	}

	public List<ReportMessage> getReportsCertificate() {
		return certificateRegister;
	}

	public void addReportProtocol(ReportMessage report) {
		protocolRegister.add(report);
	}

	public List<ReportMessage> getReportsProtocol() {
		return protocolRegister;
	}

}
