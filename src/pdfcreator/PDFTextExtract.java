/*
 *  Copyright (c) 2018 . Patrick Hochstenbach <Patrick.Hochstenbach@ugent.be>
 */
package pdfcreator;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

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
    
    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.err.println("usage: PDFTextExtract file out");
            System.exit(1);
        }
        
        String filename = args[0];
        String output = args[1];
        
        if (! (new File(output)).isDirectory()) {
            System.err.println("output " + output + " isn't a directory");
        }
        
        PDFTextExtract tool = new PDFTextExtract();
        String[] pages = tool.extract(filename);
        
        for (int i = 0 ; i < pages.length ; i++) {
            String out_file = output + "/page_" + (i+1) + ".txt";
            System.out.println("creating file " + out_file);
            PrintWriter out = new PrintWriter(out_file);
            out.print(pages[i]);
            out.close();
        }
    }
}
