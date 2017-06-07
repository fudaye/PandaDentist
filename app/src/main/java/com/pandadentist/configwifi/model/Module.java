package com.pandadentist.configwifi.model;

import java.text.DecimalFormat;

public class Module {

	private int id;
	private String mac;
	private String ip;
	private String moduleID;
	private DecimalFormat format = new DecimalFormat("00");

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}
	/**
	 * @param id the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}
	/**
	 * @return the mac
	 */
	public String getMac() {
		return mac;
	}
	/**
	 * @param mac the mac to set
	 */
	public void setMac(String mac) {
		this.mac = mac;
	}
	/**
	 * @return the ip
	 */
	public String getIp() {
		return ip;
	}
	/**
	 * @param ip the ip to set
	 */
	public void setIp(String ip) {
		this.ip = ip;
	}
	/**
	 * @return the moduleID
	 */
	public String getModuleID() {
		return moduleID;
	}
	/**
	 * @param moduleID the moduleID to set
	 */
	public void setModuleID(String moduleID) {
		this.moduleID = moduleID;
	}

	public Module(int id, String mac, String ip, String moduleID) {
		super();
		this.id = id;
		this.mac = mac;
		this.ip = ip;
		this.moduleID = moduleID;
	}

	public Module() {
		super();
	}

	@Override
	public String toString() {
		return String.format("%s. %s  %s  %s", format.format(id), mac, ip, moduleID == null ? "" : moduleID);
	}
}
