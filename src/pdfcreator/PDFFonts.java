package pdfcreator;

import com.itextpdf.text.exceptions.InvalidPdfException;
import com.itextpdf.text.pdf.PdfDictionary;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfReader;
import java.io.IOException;
import java.util.Set;
import java.util.TreeSet;

/**
 */
public class PDFFonts {
    /**
     * Creates a Set containing information about the fonts in the src PDF file.
     * @param src the path to a PDF file
     * @throws IOException
     */
    public Set<String> listFonts(String src) throws IOException {
        Set<String> set = new TreeSet<String>();

        try {
            PdfReader reader = new PdfReader(src);
            PdfDictionary resources;
            for (int k = 1; k <= reader.getNumberOfPages(); ++k) {
                resources = reader.getPageN(k).getAsDict(PdfName.RESOURCES);
                processResource(set, resources);
            }
        }
        catch (InvalidPdfException ex) {
            System.err.println("ERROR: " + src + " " + ex.getMessage());
        }
        
        return set;
    }

    /**
     * Extracts the font names from page or XObject resources.
     * @param set the set with the font names
     * @param resources the resources dictionary
     */
    public static void processResource(Set<String> set, PdfDictionary resource) {
        if (resource == null)
            return;
        PdfDictionary xobjects = resource.getAsDict(PdfName.XOBJECT);
        if (xobjects != null) {
            for (PdfName key : xobjects.getKeys()) {
                processResource(set, xobjects.getAsDict(key));
            }
        }
        PdfDictionary fonts = resource.getAsDict(PdfName.FONT);
        if (fonts == null)
            return;
        PdfDictionary font;
        for (PdfName key : fonts.getKeys()) {
            font = fonts.getAsDict(key);

            if (font == null) {
                System.err.println("ERROR: empty font for key: " + key );
                continue;
            }

            String name = font.getAsName(PdfName.BASEFONT).toString();
            
            if (name.length() > 8 && name.charAt(7) == '+') {
                name = String.format("%s subset (%s) embedded", name.substring(8), name.substring(1, 7));
            }
            else {
                name = name.substring(1);
                PdfDictionary desc = font.getAsDict(PdfName.FONTDESCRIPTOR);
                if (desc == null)
                    name += " nofontdescriptor embedded";
                else if (desc.get(PdfName.FONTFILE) != null)
                    name += " (Type 1) embedded";
                else if (desc.get(PdfName.FONTFILE2) != null)
                    name += " (TrueType) embedded";
                else if (desc.get(PdfName.FONTFILE3) != null)
                    name += " (" + font.getAsName(PdfName.SUBTYPE).toString().substring(1) + ") embedded";
            }
            set.add(name);
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.err.println("usage: PDFFonts file [file...]");
            System.exit(1);
        }

        for (int i = 0 ; i < args.length ; i++) {
            String path = args[i];

            Set<String> set = new PDFFonts().listFonts(path);

            int embedded = 0;

            for (String fontname : set) {
                if (fontname.matches(".*embedded$"))
                    embedded++;

                System.out.println(path + " : " + fontname);
            }

            int n = set.size();
            int p = n == 0 ? 0 : 100 * embedded / n;
            int risk = n - embedded;

            System.out.println(path + " : " + n + " fonts; " + p + "% embedded; " + risk + " at risk");
        }
    }
}
