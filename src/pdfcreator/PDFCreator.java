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

import com.itextpdf.text.Document;
import com.itextpdf.text.Image;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.ICC_Profile;
import com.itextpdf.text.pdf.PdfWriter;
import gnu.getopt.Getopt;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author hochsten
 */
public class PDFCreator {
    public static final int PDF_SPACE_UNIT = 72;
    public static String  colorProfile = "pdfcreator/sRGB.profile";
    public static String  creator = "PDFCreator by Unviversiteitsbibliotheek Gent";
    public static String  align = "rll";
    public static String  includeFile = null;
    public static String  excludeFile = null;
    public static float   width  = 0;
    public static float   height = 0;
    public static String  pdfxConformance = "PDFA1B";
    public static String  pdfVersion = "1.4";
    public static boolean verbose = false;
    public static String  out = "/dev/stdout";

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

        Rectangle size = rescale(maxImageSize(images), width, height);

        verbose(filename + ": open");

        // Set pageSize and margins before opening the document
        // to make them active on the first page
        doc.setPageSize(size);
        doc.setMargins(0, 0, 0, 0);

        doc.open();
        
        doc.addCreationDate();
        doc.addCreator(creator);

        for (int i = 0 ; i < images.length ; i++) {
            verbose(" +" + images[i]);

            Image img = Image.getInstance(images[i]);     
            img.scaleToFit(size.getWidth(), size.getHeight());

            // Switch the alignment of book pages based on the pageNumber
            if (i == 0) {
                img.setAlignment(align.charAt(0) == 'r' ? img.ALIGN_RIGHT : img.ALIGN_LEFT);
            }
            else if (i == images.length - 1) {
                img.setAlignment(align.charAt(1) == 'r' ? img.ALIGN_RIGHT : img.ALIGN_LEFT);
            }
            else {
                img.setAlignment(align.charAt(2) == 'r' ? img.ALIGN_RIGHT : img.ALIGN_LEFT);
            }

            doc.add(img);

            doc.newPage();
        }

        ICC_Profile icc = ICC_Profile.getInstance(getColorProfile());
        writer.setOutputIntents("Custom", "", "http://www.color.org", "sRGB IEC61966-2.1", icc);
        
        writer.createXmpMetadata();
        
        doc.close();

        verbose(filename + ": close");
    }

    protected byte[] getColorProfile() throws IOException {
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

        return bas.toByteArray();
    }

    // Rescale to a specified maximum width and height in inches
    protected Rectangle rescale(Rectangle r, float width, float height) {
        float w = r.getWidth();
        float h = r.getHeight();

        if (width == 0 && height == 0) {
            return r;
        }
        else if (width == 0) {
            return new Rectangle(PDF_SPACE_UNIT * w * height/ h , PDF_SPACE_UNIT *height);
        }
        else if (height == 0) {
            return new Rectangle(PDF_SPACE_UNIT * width , PDF_SPACE_UNIT * h * width / w);
        }
        else if (width > height) {
            return new Rectangle(PDF_SPACE_UNIT * w * height/ h , PDF_SPACE_UNIT *height);
        }
        else {
            return new Rectangle(PDF_SPACE_UNIT * width , PDF_SPACE_UNIT * h * width / w);
        }
    }

    // Return the maximum image size in PDF user units
    protected Rectangle maxImageSize(String[] images) throws Exception {
        float w = 0;
        float h = 0;
        float dpiX = 0;
        float dpiY = 0;

        for (int i = 0 ; i < images.length ; i++) {
            Image img = Image.getInstance(images[i]);

            dpiX = img.getDpiX();
            dpiY = img.getDpiY();

            if (img.getWidth() > w) {
                w = img.getWidth();
            }
            if (img.getHeight() > h) {
                h = img.getHeight();
            }
        }

        if (dpiX == 0 || dpiY == 0) {
            return new Rectangle(w,h);
        }
        else {
            return new Rectangle(PDF_SPACE_UNIT * w/dpiX, PDF_SPACE_UNIT * h/dpiY);
        }
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
                           "  -a 1A|1B|NONE - PDF/A compliance\n" +
                           "  -b            - batchmode\n" +
                           "  -i regex      - include files [batchmode]\n" +
                           "  -e regex      - exclude files [batchmode]\n" +
                           "  -o file       - output file\n" +
                           "  -w #          - width in inches\n" +
                           "  -h #          - height in inches\n" +
                           "  -r rll        - alignment of first page, next pages and last page\n" +
                           "                  as 3-character code. 'r' = right, 'l' = left.\n" +
                           "                  default: rll\n" +
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

        Getopt g = new Getopt("PDFCreator", args, "a:be:h:i:o:p:r:vw:x:");
           
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
                        pdfxConformance = "NONE";
                    }
                    break;
                 case 'b':
                    batch = true;
                    break;
                 case 'e':
                    excludeFile = g.getOptarg();
                    break;
                 case 'h':
                    height = Float.parseFloat(g.getOptarg());
                    break;
                 case 'i':
                    includeFile = g.getOptarg();
                    break;
                 case 'o':
                    out = g.getOptarg();
                    break;
                 case 'r':
                    if (g.getOptarg().length() == 3) {
                        align = g.getOptarg();
                    }
                    else {
                        System.err.println("warning: -r syntax error. Needs 3-chars. E.g. rll, lll, llr");
                    }
                    break;
                 case 'v':
                    verbose = true;
                    break;
                 case 'w':
                    width = Float.parseFloat(g.getOptarg());
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