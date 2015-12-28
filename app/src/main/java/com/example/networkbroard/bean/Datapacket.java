package com.example.networkbroard.bean;

import java.io.Serializable;

public class Datapacket implements Serializable{
	
	String ip;
	String  hostname;

	String filename;

	long filesize;

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public long getFilesize() {
		return filesize;
	}

	public void setFilesize(long filesize) {
		this.filesize = filesize;
	}

	public String getHostname() {
		return hostname;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getCommand() {
		return Command;
	}

	public void setCommand(String command) {
		Command = command;
	}

	String Command="";

	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof Datapacket)){
			return false;
		}

		return this.getIp().equals(((Datapacket) obj).getIp());
	}

	@Override
	public int hashCode() {
		return this.getIp().hashCode();
	}
}
