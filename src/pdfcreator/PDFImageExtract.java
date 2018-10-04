/*
 *  Copyright (c) 2018 . Patrick Hochstenbach <Patrick.Hochstenbach@ugent.be>
 */
package pdfcreator;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfReaderContentParser;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

/**
 *
 * @author hochsten
 */
public class PDFImageExtract {
    public PDFImageExtract() {}
    
    public void extract(String filename, String path) throws IOException {
        PdfReader reader = new PdfReader(filename);
        ImageRenderListener listener = new ImageRenderListener(path);
        
        PdfReaderContentParser parser = new PdfReaderContentParser(reader);
        
        for (int i = 1; i <= reader.getNumberOfPages(); i++) {
            parser.processContent(i, listener);
        }
        
    }
    
    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.err.println("usage: PDFImageExtract file out");
            System.exit(1);
        }
        
        String filename = args[0];
        String output = args[1];
        
        if (! (new File(output)).isDirectory()) {
            System.err.println("output " + output + " isn't a directory");
        }
        
        PDFImageExtract tool = new PDFImageExtract();
        
        tool.extract(filename,output);
    }
}
