/*
 *  PDFCreator - creates PDF/A files out of scanned images of textual records
 *
 *  Copyright (C) 2011 Patrick Hochstenbach <Patrick.Hochstenbach@gmail.com>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package pdfcreator;

import com.itextpdf.text.BadElementException;
import com.itextpdf.text.Document;
import com.itextpdf.text.Image;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.ICC_Profile;
import com.itextpdf.text.pdf.PdfAConformanceLevel;
import com.itextpdf.text.pdf.PdfAWriter;
import com.itextpdf.text.pdf.PdfWriter;
import gnu.getopt.Getopt;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author hochsten
 */
public class PDFCreator {
    public static final int PDF_SPACE_UNIT = 72;
    public static String  profileName  = "sRGB IEC61966-2.1";
    public static String  colorProfile = "pdfcreator/sRGB.profile";
    public static String  creator = "Universiteitsbibliotheek Gent";
    public static String  includeFile = null;
    public static String  excludeFile = null;
    public static String  title  = null;
    public static String  pdfxConformance = "PDFA1A";
    public static String  pdfVersion = "1.4";
    public static boolean verbose = false;
    public static String  out = "/dev/stdout";

    @SuppressWarnings("static-access")
    protected void createPdf(String filename, String[] images) throws Exception {
        Document doc = new Document();
        PdfWriter writer;
        
        if (pdfxConformance.equals("PDFA1A")) {
            writer = PdfAWriter.getInstance(doc, new FileOutputStream(filename), PdfAConformanceLevel.PDF_A_1A);
        }
        else if (pdfxConformance.equals("PDFA1B")) {
            writer = PdfAWriter.getInstance(doc, new FileOutputStream(filename), PdfAConformanceLevel.PDF_A_1B);
        }
        else if (pdfxConformance.equals("PDFA2A")) {
            writer = PdfAWriter.getInstance(doc, new FileOutputStream(filename), PdfAConformanceLevel.PDF_A_2A);
        }
        else if (pdfxConformance.equals("PDFA2B")) {
            writer = PdfAWriter.getInstance(doc, new FileOutputStream(filename), PdfAConformanceLevel.PDF_A_2B);
        }
        else if (pdfxConformance.equals("PDFA3A")) {
            writer = PdfAWriter.getInstance(doc, new FileOutputStream(filename), PdfAConformanceLevel.PDF_A_3A);
        }
        else if (pdfxConformance.equals("PDFA3B")) {
            writer = PdfAWriter.getInstance(doc, new FileOutputStream(filename), PdfAConformanceLevel.PDF_A_3B);
        }
        else {
            writer = PdfWriter.getInstance(doc, new FileOutputStream(filename));
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

        verbose(filename + ": open");
        
        doc.addCreationDate();
        doc.addCreator(creator);

        if (title != null) {
            doc.addTitle(title);
        }

        for (int i = 0 ; i < images.length ; i++) {
            verbose(" +" + images[i]);

            Image img = Image.getInstance(images[i]);     
            
            doc.setPageSize(new Rectangle(img.getWidth(), img.getHeight()));
            doc.setMargins(0, 0, 0, 0);
            
            if (doc.isOpen()) {
                doc.newPage();
            } else {
                doc.open();
            }

            doc.add(img);

            doc.newPage();
        }

        ICC_Profile icc = getImageColorProfile(images[0]);

        if (icc == null) {
            System.err.println("warning: no color profile available in " + images[0] + " using " + profileName);
            icc = getDefaultColorProfile();
        }

        writer.setOutputIntents("Custom", "", null, null, icc);

        writer.createXmpMetadata();

        doc.close();

        verbose(filename + ": close");
    }

    protected ICC_Profile getImageColorProfile(String filename) {
        try {
            Image img = Image.getInstance(filename);
            ICC_Profile icc = img.getICCProfile();
            return icc;
        } catch (BadElementException ex) {
            Logger.getLogger(PDFCreator.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MalformedURLException ex) {
            Logger.getLogger(PDFCreator.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(PDFCreator.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

    protected ICC_Profile getDefaultColorProfile() throws IOException {
        InputStream in = this.getClass().getClassLoader().getResourceAsStream(colorProfile);

        if (in == null) {
            throw new IOException("No " + colorProfile + " found");
        }
        
        ByteArrayOutputStream bas = new ByteArrayOutputStream();

        byte[] buffer = new byte[1024];
        int n = 0;
        
        while ((n = in.read(buffer)) != -1 ) {
            bas.write(buffer, 0, n);
        }

        in.close();

        return ICC_Profile.getInstance(bas.toByteArray());
    }

    protected void verbose(String msg) {
        if (verbose) {
            System.err.println(msg);
        }
    }

    protected static void usage() {
        System.err.println("usage:");
        System.err.println();
        System.err.println(" PDFCreator [options] file [file...]");
        System.err.println(" PDFCreator [options] -b < input");
        System.err.println();
        System.err.println("where input like:");
        System.err.println();
        System.err.println("[output file] [image directory]");
        System.err.println("[output file] [image directory]");
        System.err.println("...");
        System.err.println();
        System.err.println("options:");
        System.err.println();
        System.err.println("  -v            - verbose\n" +
                           "  -a 1A|1B|2A|2B|3A|3B|NONE - PDF/A compliance\n" +
                           "  -b            - batchmode\n" +
                           "  -i regex      - include files [batchmode]\n" +
                           "  -e regex      - exclude files [batchmode]\n" +
                           "  -o file       - output file\n" +
                           "  -t title      - title of the document\n" + 
                           "  -x version    - PDF verson (\"1.4\" -> \"1.7\")\n");
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

        Getopt g = new Getopt("PDFCreator", args, "a:bc:e:i:o:p:r:t:vx:");
           
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
                    else if ("2A".equals(g.getOptarg())) {
                        pdfxConformance = "PDFA2A";
                    }
                    else if ("2B".equals(g.getOptarg())) {
                        pdfxConformance = "PDFA2B";
                    }
                    else if ("3A".equals(g.getOptarg())) {
                        pdfxConformance = "PDFA3A";
                    }
                    else if ("3B".equals(g.getOptarg())) {
                        pdfxConformance = "PDFA3B";
                    }
                    else {
                        pdfxConformance = "NONE";
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
                 case 't':
                    title = g.getOptarg();
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
