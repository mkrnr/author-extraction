package de.mkrnr.rse.preproc;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.util.regex.Pattern;

import de.mkrnr.rse.util.FileHelper;

public class RegExReferenceSectionExtractor extends Extractor {

    private static final String REFERENCE_HEADER_REGEX = "[^\\p{IsAlphabetic}]*(Bibliography|"
	    + "BIBLIOGRAPHY|Bibliographie|BIBLIOGRAPHIE|Referenzen|REFERENZEN|References|REFERENCES|Reference List|REFERENCE LIST|Literatur|LITERATUR|Sources|SOURCES|Schrifttum).*";

    private static final String AFTER_REFERENCE_HEADER_REGEX = "[^\\p{IsAlphabetic}]*(Anmerkungen|ANMERKUNGEN|Appendix|"
	    + "ANHANG|Anhang|APPENDIX|Author|AUTHOR|Autor|AUTOR|Bemerkungen|Die Schriftenreihe|Discussion|DISCUSSION|Eingereicht am|Forschungsschwerpunkt|Stiftungsmaterialien|Zur Person|Zusammenfassung).*";

    public static void main(String[] args) throws IOException {
	RegExReferenceSectionExtractor regExReferenceSectionExtractor = new RegExReferenceSectionExtractor();
	regExReferenceSectionExtractor.extractInDir(new File(args[0]), new File(args[1]));
    }

    @Override
    public void extract(File inputFile, File outputFile) {
	try {
	    String input = FileHelper.readFile(inputFile);
	    int totalLines = input.split(System.getProperty("line.separator")).length;
	    BufferedReader bufferedReader = new BufferedReader(new StringReader(input));
	    String line = null;
	    StringBuffer referenceSection = new StringBuffer();
	    boolean referenceSectionFound = false;
	    boolean afterReferenceSectionFound = false;
	    int lineCount = 0;
	    while ((line = bufferedReader.readLine()) != null) {
		if (!afterReferenceSectionFound) {
		    lineCount++;
		    if (referenceSectionFound) {
			if (Pattern.matches(AFTER_REFERENCE_HEADER_REGEX, line)) {
			    afterReferenceSectionFound = true;
			    continue;
			}

			if (Pattern.matches(REFERENCE_HEADER_REGEX, line)) {
			    afterReferenceSectionFound = true;
			    referenceSectionFound = false;
			    break;
			}
			referenceSection.append(line + System.getProperty("line.separator"));
			continue;
		    }

		    if (Pattern.matches(REFERENCE_HEADER_REGEX, line) && (lineCount > (0.7 * totalLines))) {
			referenceSectionFound = true;
		    }
		}
	    }
	    // TODO check parscit paper for ratio
	    if (referenceSectionFound) {
		// if (referenceSectionFound) {
		BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(outputFile));
		bufferedWriter.write(referenceSection.toString());
		bufferedWriter.close();
	    }
	    bufferedReader.close();
	} catch (

	IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }

}