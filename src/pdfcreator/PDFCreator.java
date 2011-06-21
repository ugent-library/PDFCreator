/*
 *  Copyright (c) 2011 . Patrick Hochstenbach <Patrick.Hochstenbach@gmail.com>
 */
package pdfcreator;

import com.itextpdf.text.Document;
import com.itextpdf.text.Image;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfWriter;
import gnu.getopt.Getopt;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author hochsten
 */
public class PDFCreator {
    public static int fistPageAlign  = 1;
    public static int otherPageAlign = -1;
    public static int lastPageAlign  = -1;
    public static String includeFile = null;
    public static String excludeFile = null;
    public static int MAX_PIXELS  = 3000;
    public static String pdfxConformance = "NONE";
    public static String pdfVersion = "1.4";
    public static boolean verbose = false;
    public static String out = "/dev/stdout";

    @SuppressWarnings("static-access")
    protected void createPdf(String filename, String[] images) throws Exception {
        Document doc = new Document();
        PdfWriter writer = PdfWriter.getInstance(doc, new FileOutputStream(filename));

        if (pdfxConformance.equals("PDFA1B")) {
            writer.setPDFXConformance(PdfWriter.PDFA1B);
        }
        else if (pdfxConformance.equals("PDFA1A")) {
            writer.setPDFXConformance(PdfWriter.PDFA1A);
        }
        else {
            writer.setPDFXConformance(PdfWriter.PDFXNONE);
        }

        if (pdfVersion.equals("1.4")) {
            writer.setPdfVersion(PdfWriter.VERSION_1_4);
        }
        else if (pdfVersion.equals("1.5")) {
            writer.setPdfVersion(PdfWriter.VERSION_1_5);
        }
        else if (pdfVersion.equals("1.6")) {
            writer.setPdfVersion(PdfWriter.VERSION_1_6);
        }
        else if (pdfVersion.equals("1.7")) {
            writer.setPdfVersion(PdfWriter.VERSION_1_7);
        }
        else {
            writer.setPdfVersion(PdfWriter.VERSION_1_4);
        }
        
        Rectangle size = rescale(
                                maxImageSize(images) ,
                                MAX_PIXELS
                                );

        verbose(filename + ": open");

        doc.open();
        
        doc.addCreationDate();
        doc.addCreator("PDFCreator by Unviversiteitsbibliotheek Gent");

        for (int i = 0 ; i < images.length ; i++) {
            verbose(" +" + images[i]);

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

        writer.createXmpMetadata();
        
        doc.close();

        verbose(filename + ": close");
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
        System.err.println("usage:");
        System.err.println();
        System.err.println(" PDFCreator [-v] [-o file] [-p MAX_PIXELS] file [file...]");
        System.err.println(" PDFCreator [-v] [-b] [-i regex] [-e regex] [-p MAX_PIXELS] < input");
        System.err.println();
        System.err.println("where input like");
        System.err.println();
        System.err.println("[output file] [image directory]");
        System.err.println("[output file] [image directory]");
        System.err.println("...");
        System.exit(1);
    }

    protected static String[] scanDirectory(String filename, String include, String exclude) {
        List images = new ArrayList();

        File f = new File(filename);
        File[] list = f.listFiles();

        for (int i = 0 ; i < list.length ; i++) {
            
            if (include != null && ! list[i].getName().matches(include)) {
                continue;
            }
            
            if (exclude != null && list[i].getName().matches(exclude)) {
                continue;
            }

            if (list[i].isFile() && ! list[i].isHidden()) {
                images.add(list[i].getAbsolutePath());
            }
        }

        Collections.sort(images);
        
        return (String []) images.toArray(new String[] {});
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        boolean batch = false;

        Getopt g = new Getopt("PDFCreator", args, "a:be:i:o:p:vx:");
           
        int c;
        String arg;
        while ((c = g.getopt()) != -1) {
             switch(c) {
                 case 'a':
                    if ("1A".equals(g.getOptarg())) {
                        pdfxConformance = "PDFA1A";
                    }
                    else if ("1B".equals(g.getOptarg())) {
                        pdfxConformance = "PDFA1B";
                    }
                    else {
                        pdfxConformance = "PDFA1A";
                    }
                    break;
                 case 'b':
                    batch = true;
                    break;
                 case 'e':
                    excludeFile = g.getOptarg();
                    break;
                 case 'i':
                    includeFile = g.getOptarg();
                    break;
                 case 'o':
                    out = g.getOptarg();
                    break;
                 case 'p':
                    MAX_PIXELS = Integer.parseInt(g.getOptarg());
                    break;
                 case 'v':
                    verbose = true;
                    break;
                 case 'x':
                     pdfVersion = g.getOptarg();
                     break;
                 default:
                     usage();
                     break;
               }
        }
        
        if (g.getOptind() == args.length && ! batch) {
            usage();
        }

        if (batch) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

            PDFCreator m = new PDFCreator();
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.length() == 0 || line.matches("\\s*") || line.matches("\\s*#.*")) {
                    continue;
                }

                String[] parts  = line.split("\\s+");
                String[] images = scanDirectory(parts[1], includeFile, excludeFile);

                m.createPdf(parts[0], images);
            }
        }
        else {
            List images = new ArrayList();

            for (int i = g.getOptind(); i < args.length ; i++) {
                images.add(args[i]);
            }

            PDFCreator m = new PDFCreator();
            m.createPdf(out, (String[]) images.toArray(new String[] {}));
        }
    }
}