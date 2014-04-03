package com.redriverstorm.how.manifest.common;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class Manifest {

	private String entrySeparator = ",";
	private Map<String, ManifestEntry> data = Maps.newHashMap();
	
	public Manifest() {
		
	}

	public Manifest(Map<String, ManifestEntry> content) {
		this.data.putAll(content);
	}

	public void put(String entry) throws Exception {
		String[] parts = entry.split(entrySeparator);
		String key = parts[0];
		String checksum = parts[1];
		boolean isLocal = Boolean.parseBoolean(parts[2]);
		put(key, new ManifestEntry(null, key, checksum, isLocal));
	}
	
	public void put(String file, ManifestEntry entry) {
		data.put(file, entry);
	}
	
	public int size() {
		return data.size();
	}
	
	public boolean isEmpty() {
		return data.isEmpty();
	}
	
	public ManifestEntry get(String key) {
		return data.get(key);
	}
	
	public Set<String> getKeys() {
		return data.keySet();
	}
	
	public List<ManifestEntry> getValues() {
		return Lists.newArrayList(data.values());
	}
	
	public String getChecksum(String key) {
		return data.get(key).getChecksum();
	}
	
	public boolean isLocal(String key) {
		if (containsKey(key)) {
			return data.get(key).isLocal();
		}
		return false;
	}
	
	public void setChecksum(String key, String checksum) {
		if (!containsKey(key)) {
			put(key, new ManifestEntry(null, key, checksum, false));
		}
		else {
			data.get(key).setChecksum(checksum);
		}
	}
	
	public boolean containsKey(String key) {
		return data.containsKey(key);
	}
	
	public static Manifest compare(Manifest orig, Manifest updated) {
		Map<String, ManifestEntry> result = Maps.newHashMap();
		for (String key : updated.getKeys()) {
			ManifestEntry updatedEntry = updated.get(key);
			if (updatedEntry.isSupported()) {
				if (!orig.containsKey(key)) {	// case: new entry in the updated manifest
					result.put(key, updatedEntry);
				}
				else {	// case: checksum updated for an existing entry
					String checksumOrig = orig.getChecksum(key);
					String checksumUpdated = updatedEntry.getChecksum();
					if (!checksumOrig.equals(checksumUpdated)) {
						result.put(key, updated.get(key));
					}
				}
				//TODO: should we handle the case for a deleted entry in updated manifest?
			}
			else {
				System.err.println("Ignoring an unsupported entry in the updated manifest: " + key);
			}
		}
		return new Manifest(result);
	}
	
	public String getContent() {
		StringBuilder fileContent = new StringBuilder("");
		for (String key : data.keySet()) {
			ManifestEntry entry = data.get(key);
			fileContent.append(key);
			fileContent.append(entrySeparator);
			fileContent.append(entry.getChecksum());
			fileContent.append(entrySeparator);
			fileContent.append(entry.isLocal());
			fileContent.append("\n");
		}
		return fileContent.toString();
	}
}
