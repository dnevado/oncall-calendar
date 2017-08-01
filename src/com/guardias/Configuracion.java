package com.guardias;

public class Configuracion {

	private String Key;
	private String Value;
	private Long config_IdServicio = new Long(-1);
	public String getKey() {
		return Key;
	}
	public void setKey(String key) {
		Key = key;
	}
	public String getValue() {
		return Value;
	}
	public void setValue(String value) {
		Value = value;
	}
	public Long getConfig_IdServicio() {
		return config_IdServicio;
	}
	public void setConfig_IdServicio(Long config_IdServicio) {
		this.config_IdServicio = config_IdServicio;
	}

}
