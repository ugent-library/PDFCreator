/*
 *  Copyright (c) 2011 . Patrick Hochstenbach <Patrick.Hochstenbach@gmail.com>
 */
package pdfcreator;

import com.itextpdf.text.Document;
import com.itextpdf.text.Image;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import gnu.getopt.Getopt;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author hochsten
 */
public class PDFCreator {
    public static int fistPageAlign  = 1;
    public static int otherPageAlign = -1;
    public static int lastPageAlign  = -1;

    public static int MAX_PIXELS  = 3000;
    public static boolean verbose = false;
    public static String out = "/dev/stdout";

    @SuppressWarnings("static-access")
    protected void createPdf(String filename, String[] images) throws Exception {
        Document doc = new Document();
        PdfWriter writer = PdfWriter.getInstance(doc, new FileOutputStream(filename));
        
        writer.setPDFXConformance(PdfWriter.PDFA1A);
        
        Rectangle size = rescale(
                                maxImageSize(images) ,
                                MAX_PIXELS
                                );
    
        doc.open();
        doc.addCreationDate();
        doc.addCreator("PDFCreator by Unviversiteitsbibliotheek Gent");

        for (int i = 0 ; i < images.length ; i++) {
            verbose("Processing: " + images[i]);

            Image img = Image.getInstance(images[i]);     
            img.scaleToFit(size.getWidth(), size.getHeight());

            // Switch the alignment of book pages based on the pageNumber
            if (i == 0) {
                img.setAlignment(fistPageAlign == 1 ? img.ALIGN_RIGHT : img.ALIGN_LEFT);
            }
            else if (i == images.length - 1) {
                img.setAlignment(lastPageAlign == 1 ? img.ALIGN_RIGHT : img.ALIGN_LEFT);
            }
            else {
                img.setAlignment(otherPageAlign == 1 ? img.ALIGN_RIGHT : img.ALIGN_LEFT);
            }

            doc.setPageSize(size);
            doc.setMargins(0, 0, 0, 0);
            doc.newPage();
            doc.add(img);
        }
        
        doc.close();
    }

    protected Rectangle rescale(Rectangle r, int maxpixels) {
        float w = r.getWidth();
        float h = r.getHeight();

        if (w < maxpixels) {
            return r;
        }
        else {
            return  new Rectangle((float) maxpixels , h * maxpixels / w);
        }
    }

    protected Rectangle maxImageSize(String[] images) throws Exception {
        float width = 0;
        float height = 0;

        for (int i = 0 ; i < images.length ; i++) {
            Image img = Image.getInstance(images[i]);

            if (img.getWidth() > width) {
                width = img.getWidth();
            }
            if (img.getHeight() > height) {
                height = img.getHeight();
            }
        }

        return new Rectangle(width,height);
    }

    protected void verbose(String msg) {
        if (verbose) {
            System.err.println(msg);
        }
    }

    protected static void usage() {
        System.err.println("usage: PDFCreator [-v] [-o file] [-p MAX_PIXELS] file [file...]");
        System.exit(1);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        Getopt g = new Getopt("PDFCreator", args, "do:p:v");
           
        int c;
        String arg;
        while ((c = g.getopt()) != -1) {
             switch(c) {
                 case 'o':
                    out = g.getOptarg();
                    break;
                 case 'p':
                    MAX_PIXELS = Integer.parseInt(g.getOptarg());
                    break;
                 case 'v':
                    verbose = true;
                    break;
               }
        }
        
        if (g.getOptind() == args.length) {
            usage();
        }

        List images = new ArrayList();

        for (int i = g.getOptind(); i < args.length ; i++) {
            images.add(args[i]);
        }

        PDFCreator m = new PDFCreator();
        m.createPdf(out, (String[]) images.toArray(new String[] {}));
    }
}