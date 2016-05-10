package de.mkrnr.rse.distsup;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.mkrnr.rse.util.FileHelper;

public class NameMatcher {

    public static void main(String[] args) throws IOException {
	File nameFile = new File(args[0]);
	File taggedDir = new File(args[1]);
	File outputDir = new File(args[2]);

	long startTime = System.currentTimeMillis();
	NameMatcher nameContextExtractor = new NameMatcher(nameFile, "firstName", "lastName", "author");

	long endTime = System.currentTimeMillis();
	nameContextExtractor.matchDirectory(taggedDir, outputDir);
	System.out.println("This took " + (endTime - startTime) + " milliseconds");
	// Getting the runtime reference from system
	Runtime runtime = Runtime.getRuntime();

	int mb = 1024 * 1024;
	System.out.println("##### Heap utilization statistics [MB] #####");

	// Print used memory
	System.out.println("Used Memory:" + ((runtime.totalMemory() - runtime.freeMemory()) / mb));

	// Print free memory
	System.out.println("Free Memory:" + (runtime.freeMemory() / mb));

	// Print total available memory
	System.out.println("Total Memory:" + (runtime.totalMemory() / mb));

	// Print Maximum available memory
	System.out.println("Max Memory:" + (runtime.maxMemory() / mb));
    }

    // generate name lookup
    // read file
    // remove linebreaks
    // sliding window:
    // at position i, check if name at i with name at i+1 is in database (check
    // all possibilities)
    // if yes, get context and print it/store in array

    /**
     * Format of names: Map<lastName,Set<firstNameVariation>>
     */
    private Map<String, Set<String>> namesLookup;
    private String firstNameTag;
    private String lastNameTag;
    private String authorTag;

    /**
     * Constructor that generates a lookup for the names in nameFile TODO:
     * specify format of nameFile
     *
     * @param nameFile
     * @throws IOException
     */
    public NameMatcher(File nameFile, String firstNameTag, String lastNameTag, String authorTag) throws IOException {
	this.namesLookup = this.generateNamesLookUp(nameFile);
	this.firstNameTag = firstNameTag;
	this.lastNameTag = lastNameTag;
	this.authorTag = authorTag;
    }

    public void matchDirectory(File inputDirectory, File outputDirectory) {
	if (!outputDirectory.exists()) {
	    outputDirectory.mkdirs();
	}
	try {
	    for (File inputFile : inputDirectory.listFiles()) {
		this.matchFile(inputFile,
			new File(outputDirectory.getAbsolutePath() + File.separator + inputFile.getName()));
	    }
	} catch (NullPointerException e) {
	    e.printStackTrace();
	}
    }

    public void matchFile(File inputFile, File outputFile) {
	String inputString = null;
	try {
	    inputString = FileHelper.readFile(inputFile);
	} catch (IOException e) {
	    e.printStackTrace();
	}

	NameSplit nameSplit = new NameSplit(inputString);

	for (int i = 0; i < nameSplit.size(); i++) {
	    String iNameSplitTag = nameSplit.getTag(i);
	    if (iNameSplitTag == null) {
		continue;
	    }

	    // if i is not the last word in nameSplit
	    if ((i + 1) < nameSplit.size()) {
		if ((this.firstNameTag.equals(iNameSplitTag) && this.lastNameTag.equals(nameSplit.getTag(i + 1)))) {
		    if (this.containsAuthor(nameSplit.getName(i), nameSplit.getName(i + 1))) {
			nameSplit.formatTaggedName(i, this.firstNameTag);
			nameSplit.formatTaggedName(i + 1, this.lastNameTag);
			nameSplit.addTag(i, i + 1, this.authorTag);
			continue;
		    }
		}
		if (this.lastNameTag.equals(iNameSplitTag) && this.firstNameTag.equals(nameSplit.getTag(i + 1))) {
		    if (this.containsAuthor(nameSplit.getName(i + 1), nameSplit.getName(i))) {
			nameSplit.formatTaggedName(i, this.lastNameTag);
			nameSplit.formatTaggedName(i + 1, this.firstNameTag);
			nameSplit.addTag(i, i + 1, this.authorTag);
			continue;
		    }
		}
	    }

	    // no author name starts with name at position i
	    nameSplit.removeNameTag(i);
	}
	try {
	    BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(outputFile));
	    bufferedWriter.write(nameSplit.toString());
	    bufferedWriter.close();
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    private boolean containsAuthor(String firstName, String lastName) {
	if (this.namesLookup.containsKey(lastName)) {
	    if (this.namesLookup.get(lastName).contains(firstName)) {
		return true;
	    }
	}
	return false;

    }

    private Map<String, Set<String>> generateNamesLookUp(File nameFile) throws IOException {
	Map<String, Set<String>> namesLookup = new HashMap<String, Set<String>>();
	BufferedReader nameReader = new BufferedReader(new FileReader(nameFile));

	String line;
	while ((line = nameReader.readLine()) != null) {
	    String[] lineSplit = line.split("\t");
	    if (lineSplit.length != 3) {
		nameReader.close();
		throw new IOException("line length != 3: \"" + line + "\"");
	    }
	    Set<String> firstNameVariations = Name.getFirstNameVariations(lineSplit[0]);
	    String lastName = lineSplit[1];
	    if (namesLookup.containsKey(lastName)) {
		for (String firstNameVariation : firstNameVariations) {
		    namesLookup.get(lastName).add(firstNameVariation);
		}
	    } else {
		namesLookup.put(lastName, new HashSet<String>(firstNameVariations));
	    }
	}
	nameReader.close();

	return namesLookup;
    }
}