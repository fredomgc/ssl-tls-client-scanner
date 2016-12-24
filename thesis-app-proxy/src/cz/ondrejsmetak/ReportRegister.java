/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.ondrejsmetak;

import static cz.ondrejsmetak.ConfigurationRegister.DEBUG;
import cz.ondrejsmetak.entity.ReportMessage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author Ondřej Směták <posta@ondrejsmetak.cz>
 */
public class ReportRegister {

	/**
	 * Instance of this class
	 */
	private static ReportRegister instance = null;

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
	
	public void addReportMessages(Collection<ReportMessage> messages){
		register.addAll(messages);
	}
	
	public List<ReportMessage> getReportMessages(){
		return register;
	}
}
