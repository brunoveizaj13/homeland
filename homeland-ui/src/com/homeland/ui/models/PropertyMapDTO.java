package com.homeland.ui.models;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@JsonIgnoreProperties(ignoreUnknown = true)
public class PropertyMapDTO implements Serializable {

	private static final long serialVersionUID = 1L;
	
	String data;

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	
	
}
