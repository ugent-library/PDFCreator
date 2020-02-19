/*
 *  Copyright (c) 2018 . Patrick Hochstenbach <Patrick.Hochstenbach@ugent.be>
 */
package pdfcreator;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.NullPointerException;
import java.util.stream.IntStream;
import java.util.stream.Collectors;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author hochsten
 */
public class PDFTextExtract {
    public PDFTextExtract() {}

    public String[] extract(String filename) throws IOException {
        PdfReader reader = new PdfReader(filename);
	int pages = reader.getNumberOfPages();

	String[] result = new String[pages];

	for (int i = 0 ; i < pages ; i++) {
	    String text = PdfTextExtractor.getTextFromPage(reader, i + 1);
	    result[i] = text;
        }

        return result;
    }

    public String[] extract(String filename, int start, int end) throws IOException, NullPointerException {
	PdfReader reader = new PdfReader(filename);
        
	List<Integer> pageNrs = IntStream.rangeClosed(start, end)
	    .boxed()
	    .collect(Collectors.toList());
      
	String[] result = new String[pageNrs.size()]; 

	int i = 0;
	for (int pageNr : pageNrs) {
	    result[i] = PdfTextExtractor.getTextFromPage(reader, pageNr);
	    i++;
	}

	return result;
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("usage: PDFTextExtract file out");
            System.exit(1);
        }
        
        String filename = args[0];
        String output = args[1];
        String range = (args.length == 3) ? args[2] : "";
         
	int start = 0;
	int end = 0;

        if (! (new File(output)).isDirectory()) {
            System.err.println("output " + output + " isn't a directory");
	    System.exit(1);
        }

	String [] pages = new String[0];
	if (! (range.isEmpty()) ) {
	    if (! range.matches("^[0-9]*\\.\\.[0-9]*")) {
	        System.err.println("Range doesn't match expected format n..m");
		System.exit(1);
	    }
	    
	    List<Integer> bounds = Arrays.stream(range.split("\\.\\."))
		.map(Integer::parseInt)
		.collect(Collectors.toList());

	    start = bounds.get(0);
	    end = bounds.get(1);

	    if (start >= end) {
		System.err.println("The start page can't be greater or equal then the end page.");
		System.exit(1);
	    }

	    PDFTextExtract tool = new PDFTextExtract();
	    try {
	        pages = tool.extract(filename, start, end);
	    } catch (IOException | NullPointerException e) {
		System.err.println("Something went wrong. Likely, the provided range exceeded the range of available pages.");
		System.exit(1);
	    }
		
	} else {
	    PDFTextExtract tool = new PDFTextExtract();
	    pages = tool.extract(filename);
	} 

	for (int i = 0 ; i < pages.length ; i++) {
	    int pageNr = (start > 0) ? start + i : i + 1;
	    String out_file = output + "/page_" + pageNr + ".txt";
	    System.out.println("creating file " + out_file);
            PrintWriter out = new PrintWriter(out_file);
	    out.print(pages[i]);
	    out.close();
        }
    }
}
