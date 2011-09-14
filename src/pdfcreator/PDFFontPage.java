/*
 *  Copyright (c) 2011 . Patrick Hochstenbach <Patrick.Hochstenbach@gmail.com>
 */

package pdfcreator;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.LineSeparator;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author hochsten
 */
public class PDFFontPage {
    public void createPdf(String filename, String TEXT, String[] FONTS) throws IOException, DocumentException {
        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream(filename));

        document.open();
        BaseFont bf;
        Font font;

        for (int i = 0; i < FONTS.length; i++) {
            try {
                bf = BaseFont.createFont(FONTS[i], "", BaseFont.NOT_EMBEDDED);
                document.add(new Paragraph(String.format(
                     "Font file: %s with encoding %s", FONTS[i], "")));
                document.add(new Paragraph(String.format(
                     "iText class: %s", bf.getClass().getName())));

                font = new Font(bf, 12);
                document.add(new Paragraph(TEXT, font));
                document.add(new LineSeparator(0.5f, 100, null, 0, -5));
            }
            catch (DocumentException ex) {
                ex.printStackTrace();
            }
        }
        document.close();
    }
    
    public static void main(String[] args) throws Exception {
        if (args.length < 3) {
            System.err.println("usage: PDFFontPage filename text font [font ...]\n");
            System.exit(1);
        }

        String filename = args[0];
        String text     = args[1];

        List<String> fonts = new ArrayList<String>();

        for (int i = 2 ; i < args.length ; i++) {
            fonts.add(args[i]);
        }

        PDFFontPage fp = new PDFFontPage();
        
        fp.createPdf(filename, text, (String[]) fonts.toArray(new String[] {}));
    }
}
