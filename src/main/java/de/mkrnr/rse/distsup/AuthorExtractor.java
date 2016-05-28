package de.mkrnr.rse.distsup;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public abstract class AuthorExtractor {

    protected String separator = "\t";

    protected Map<String, Integer> firstNamesMap;
    protected Map<String, Integer> firstNameVariationsMap;
    protected Map<String, Integer> lastNamesMap;
    protected Map<String, Integer> lastNameSplitsMap;
    protected Map<String, Integer> namesMap;

    private void addNameVariationsToMap(String firstNames, Map<String, Integer> firstNameVariationsMap) {
	Set<String> firstNameVariations = Name.getFirstNameVariations(firstNames);
	for (String firstNameVariation : firstNameVariations) {
	    this.addNameSplitToMap(firstNameVariation, firstNameVariationsMap);
	}

    }

    protected void addNameSplitToMap(String names, Map<String, Integer> map) {
	String[] namesSplit = names.split("\\s");
	for (String name : namesSplit) {
	    this.addNameToMap(name, map);
	}
    }

    protected void addNamesToMaps(String firstNames, String lastNames) {
	this.addNameToMap(firstNames, this.firstNamesMap);
	this.addNameVariationsToMap(firstNames, this.firstNameVariationsMap);

	this.addNameToMap(lastNames, this.lastNamesMap);
	this.addNameSplitToMap(lastNames, this.lastNameSplitsMap);

	this.addNameToMap(firstNames + this.separator + lastNames, this.namesMap);
    }

    protected void addNameToMap(String name, Map<String, Integer> map) {
	if (map.containsKey(name)) {
	    map.put(name, map.get(name) + 1);
	} else {
	    // check if name contains at least one letter
	    if (name.matches(".*\\p{Alpha}+.*")) {
		map.put(name, 1);
	    }
	}
    }

    protected void initializeMaps() {
	this.firstNamesMap = new HashMap<String, Integer>();
	this.firstNameVariationsMap = new HashMap<String, Integer>();
	this.lastNamesMap = new HashMap<String, Integer>();
	this.lastNameSplitsMap = new HashMap<String, Integer>();
	this.namesMap = new HashMap<String, Integer>();

    }

    protected void writeMaps(File outputDirectory) {
	this.writeMapToFile(this.firstNamesMap,
		new File(outputDirectory.getAbsolutePath() + File.separator + "first-names.csv"));
	this.writeMapToFile(this.firstNameVariationsMap,
		new File(outputDirectory.getAbsolutePath() + File.separator + "first-name-variations.csv"));
	this.writeMapToFile(this.lastNamesMap,
		new File(outputDirectory.getAbsolutePath() + File.separator + "last-names.csv"));
	this.writeMapToFile(this.lastNameSplitsMap,
		new File(outputDirectory.getAbsolutePath() + File.separator + "last-name-splits.csv"));
	this.writeMapToFile(this.namesMap, new File(outputDirectory.getAbsolutePath() + File.separator + "names.csv"));

    }

    protected void writeMapToFile(Map<String, Integer> map, File outputFile) {
	try {
	    BufferedWriter nameWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile)));

	    for (Entry<String, Integer> entry : map.entrySet()) {
		nameWriter.write(entry.getKey() + "\t" + entry.getValue() + System.lineSeparator());
	    }

	    nameWriter.close();
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

}
