package cz.ondrejsmetak.export;

import cz.ondrejsmetak.resource.ResourceManager;
import cz.ondrejsmetak.entity.ReportClientHello;
import cz.ondrejsmetak.entity.ReportMessage;
import cz.ondrejsmetak.tool.Helper;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Creates HTML file, that contains results of completed scans
 *
 * @author Ondřej Směták <posta@ondrejsmetak.cz>
 */
public class HtmlExport extends BaseExport {

	/**
	 * Specifies a empty place in HTML template, that will be replaced with
	 * content
	 */
	private static final String CONTENT_HOOK = "#CONTENT#";

	/**
	 * Returns prepared HTML template with no content.
	 *
	 * @return HTML template
	 * @throws FileNotFoundException
	 */
	private String getTemplate() throws FileNotFoundException {
		return Helper.getContentOfFile(ResourceManager.getHtmlTemplate());
	}

	/**
	 * Creates content of HTML export. This content may contains multiple scan
	 * reports
	 *
	 * @param reports collection of completed scan reports
	 * @param timestamp date and time of finished scans
	 * @return body of HTML file
	 * @throws FileNotFoundException
	 */
	private String getContent(List<ReportMessage> messages, List<ReportClientHello> reportClientHello, Date timestampOfStart, Date timestampOfEnd) throws FileNotFoundException {
		StringBuilder sb = new StringBuilder();
		String timestampOfStartStr = Helper.getFormattedDateTime(timestampOfStart, false);
		String timestampOfEndStr = Helper.getFormattedDateTime(timestampOfEnd, false);

		sb.append(String.format("<h1>SSL/TLS analysis <small>%s to %s</small></h1>", timestampOfStartStr, timestampOfEndStr));

		/**
		 * General messages
		 */
		if (!messages.isEmpty()) {
			sb.append(doReport(messages));
		}

		/**
		 * Analysis of captured Client Hello(s)
		 */
		for (ReportClientHello report : reportClientHello) {
			sb.append(String.format("<h2>Captured Client Hello #%s</h2>", report.getClientHelloId()));
			sb.append(doReport(report.getReportMessages()));
		}

		String template = getTemplate();
		template = template.replace(CONTENT_HOOK, sb.toString());
		return template;
	}

	/**
	 * Create HTML table of one report
	 *
	 * @param report report of completed scan
	 * @return HTML table
	 */
	private String doReport(List<ReportMessage> messages) {
		List<ReportMessage> protocol = new ArrayList<>();
		List<ReportMessage> cipher = new ArrayList<>();
		List<ReportMessage> other = new ArrayList<>();

		for (ReportMessage reportMessage : messages) {
			if (reportMessage.getCategory().equals(ReportMessage.Category.PROTOCOL)) {
				protocol.add(reportMessage);
			}

			if (reportMessage.getCategory().equals(ReportMessage.Category.CIPHER)) {
				cipher.add(reportMessage);
			}

			if (reportMessage.getCategory().equals(ReportMessage.Category.OTHER)) {
				other.add(reportMessage);
			}

		}

		StringBuilder sb = new StringBuilder();
		sb.append("<table class=\"table table-striped table-hover\">");
		sb.append("<thead><tr><th>Category</th><th>Mode</th><th>Status</th></tr></thead>");
		sb.append("<tbody>");
		sb.append(doCreateTableSegment("Protocols", protocol));
		sb.append(doCreateTableSegment("Cipher suites", cipher));
		sb.append(doCreateTableSegment("Other", other));
		sb.append("</tbody>");
		sb.append("</table>");
		return sb.toString();
	}

	/**
	 * Create one table segment. Each segment represents one category of report
	 * messages
	 *
	 * @param segmentName name of segment
	 * @param messages collection containg report messages
	 * @return HTML of table segment
	 */
	private String doCreateTableSegment(String segmentName, List<ReportMessage> messages) {
		if (messages.isEmpty()) {
			return "";
		}

		StringBuilder sb = new StringBuilder();
		sb.append(String.format("<tr> <th scope=\"row\" rowspan=\"%s\" class=\"col-xs-1\">%s</th>", messages.size(), segmentName));

		boolean first = true;
		for (ReportMessage message : messages) {
			if (!first) {
				sb.append("<tr>");
			}
			sb.append(String.format("<td>%s</td>", message.getRequiredModeHuman()));
			sb.append(String.format("<td class=\"%s\">%s</td>", typeToCssClass(message), message.getMessage()));
			sb.append("</tr>");

			first = false;
		}

		return sb.toString();
	}

	/**
	 * According to type of report message, return valid CSS class
	 *
	 * @param message report message
	 * @return css class
	 */
	private String typeToCssClass(ReportMessage message) {
		if (message.getRequiredMode() != null && message.getRequiredMode().isCanBe()) {
			return "";
		}

		if (message.getType().equals(ReportMessage.Type.ERROR)) {
			return "danger";
		}

		if (message.getType().equals(ReportMessage.Type.SUCCESS)) {
			return "success";
		}

		return "";
	}

	/**
	 * Exports collection of report messages to HTML file
	 *
	 * @param generalMessages collection of general report messages
	 * @param reportClientHello collection of reports (each report for single Client Hello)
	 * @param timestampOfStart timestamp of moment, when proxy server started
	 * @return path to newly created HTMl file
	 * @throws IOException in case of any error
	 */
	public String export(List<ReportMessage> generalMessages, List<ReportClientHello> reportClientHello, Date timestampOfStart) throws IOException {
		Date timestampOfEnd = new Date();
		String timestampOfEndStr = Helper.getFormattedDateTime(timestampOfEnd, true);

		File target = new File(Helper.getWorkingDirectory() + File.separator + "report_" + timestampOfEndStr + ".htm");
		try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(target.getAbsolutePath()), "utf-8"))) {
			writer.write(getContent(generalMessages, reportClientHello, timestampOfStart, timestampOfEnd));
			writer.flush();
			writer.close();
		}

		return target.getAbsolutePath();
	}

}
