package com.redriverstorm.how.manifest.common;

import java.util.Map;

import com.google.common.collect.Maps;

public class ManifestEntry {
	
	public enum ManifestEntryType {
		IMAGE_JPEG("jpeg"),
		IMAGE_PNG("png"),
		JSON("json"),
		PLIST("plist"),
		FONT("fnt"),
		OTHER("other");
		
		private String value;
		private ManifestEntryType(String value) {
			this.value = value;
		}
		
		public String value() {
			return value;
		}
	}
	
	private static Map<String, ManifestEntryType> manifestMap = Maps.newHashMap();
	static {
		ManifestEntryType[] values = ManifestEntryType.values();
		for (ManifestEntryType type : values) {
			manifestMap.put(type.value(), type);
		}
	}
	private String baseUrl;
	private String key;
	private String checksum;
	private ManifestEntryType type;
	private boolean isLocal;
	
	public ManifestEntry(String baseUrl, String key, String checksum, boolean isLocal) {
		this.baseUrl = baseUrl;
		this.key = key;
		this.checksum = checksum;
		this.isLocal = isLocal;
		setType();
	}
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public ManifestEntryType getType() {
		return type;
	}
	public void setType(ManifestEntryType type) {
		this.type = type;
	}
	public String getBaseUrl() {
		return baseUrl;
	}
	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}
	public String getUrl() {
		return baseUrl + key;
	}
	public String getChecksum() {
		return checksum;
	}
	public void setChecksum(String checksum) {
		this.checksum = checksum;
	}
	public boolean isLocal() {
		return isLocal;
	}
	public void setLocal(boolean isLocal) {
		this.isLocal = isLocal;
	}
	private void setType() {
		int indexOfDot = key.lastIndexOf(".");
		if (indexOfDot > -1) {
			String fileExt = key.substring(indexOfDot+1, key.length());
			ManifestEntryType type = manifestMap.get(fileExt);
			setType(type == null ? ManifestEntryType.OTHER : type);
		}
	}
	public boolean isJPEG() {
		return type == ManifestEntryType.IMAGE_JPEG;
	}
	
	public boolean isPNG() {
		return type == ManifestEntryType.IMAGE_PNG;
	}
	public boolean isJSON() {
		return type == ManifestEntryType.JSON;
	}
	public boolean isPLIST() {
		return type == ManifestEntryType.PLIST;
	}
	public boolean isFont() {
		return type == ManifestEntryType.FONT;
	}
	public boolean isImage() {
		return isPNG() || isJPEG();
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + key.hashCode();
		result = prime * result + checksum.hashCode();
		result = prime * result + (isLocal ? 1 : 0);
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		ManifestEntry entry = (ManifestEntry)obj;
		if (!this.key.equals(entry.key)) {
			return false;
		}
		if (!this.checksum.equals(entry.checksum)) {
			return false;
		}
		return true;
	}
	
	public boolean isSupported() {
		return this.type != ManifestEntryType.OTHER;
	}
}
