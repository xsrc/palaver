package de.xsrc.palaver.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import org.datafx.util.EntityWithId;

/**
 * Represents a user account with jid and password
 * @author kalkin
 *
 */
public class Account implements EntityWithId<String> {

	private static final long serialVersionUID = 1L;

	private StringProperty jid;
	
	private StringProperty password;
	
	public Account(String jid, String password) {
		this.jid = new SimpleStringProperty(jid);
		this.password = new SimpleStringProperty(password);
	}

	public String getId() {
		return getJid();
	}
	
	public void setJid(String jid){
		this.jid.set(jid);
	}
	
	public String getPassword() {
		return password.get();
	}
	
	public void setPassword(String password){
		this.password.set(password);
	}

	public String getJid() {
		return jid.get();
	}

}
