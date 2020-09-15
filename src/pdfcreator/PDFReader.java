/*
 *  Copyright (c) 2011 . Patrick Hochstenbach <Patrick.Hochstenbach@gmail.com>
 */
package pdfcreator;

import com.itextpdf.text.pdf.PdfReader;
import java.io.File;

/**
 *
 * @author hochsten
 */
public class PDFReader {
    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.err.println("usage: PDFReader file");
            System.exit(1);
        }
        
        String filename = args[0];
    
        PdfReader reader = new PdfReader(filename);
        
        System.out.print("Reading:");
        for (int i = 1; i <= reader.getNumberOfPages(); i++) {
            System.out.print(i + " ");
        }
        System.out.println();
        System.out.println("Ok");
    }
}
