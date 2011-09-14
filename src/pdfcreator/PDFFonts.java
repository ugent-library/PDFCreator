package pdfcreator;

import com.itextpdf.text.pdf.PdfDictionary;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfReader;
import gnu.getopt.Getopt;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 */
public class PDFFonts {
    private final String[] base14 = new String[] {
        "Courier" , "Courier-Bold" , "Courier-Oblique" , "Courier-BoldOblique" ,
        "Times-Roman" , "Times-Bold" , "Times-Italic" , "Times-BoldItalic" ,
        "Helvetica" , "Helvetica-Bold", "Helvetica-Oblique" , "Helvetica-BoldOblique" ,
        "Symbol" ,
        "ZapfDingbats"
    };

    /**
     * Creates a Set containing information about the fonts in the src PDF file.
     * @param src the path to a PDF file
     */
    public Set<String> listFonts(String src) {
        Set<String> set = new TreeSet<String>();

        try {
            PdfReader reader = new PdfReader(src);
            PdfDictionary resources;
            for (int k = 1; k <= reader.getNumberOfPages(); ++k) {
                resources = reader.getPageN(k).getAsDict(PdfName.RESOURCES);
                processResource(set, resources);
            }
        }
        catch (IOException ex) {
            System.err.println("ERROR: " + src + " " + ex.getMessage());
        }
        
        return set;
    }

    /**
     * Create a Map containing information about the fonts available per page
     * @param src the path to a PDF file
     */
    public Map<String,Integer> listFontDistribution(String src) {
        Map<String,Integer> map = new TreeMap<String, Integer>();

        try {
            PdfReader reader = new PdfReader(src);

            for (int k = 1; k <= reader.getNumberOfPages(); ++k) {
                Set<String> set = new TreeSet<String>();
                PdfDictionary resources = reader.getPageN(k).getAsDict(PdfName.RESOURCES);
                processResource(set, resources);

                for (String fontname : set) {
                    if (map.containsKey(fontname)) {
                        Integer count = map.get(fontname);
                        map.put(fontname, new Integer(count.intValue() + 1));
                    }
                    else {
                        map.put(fontname, new Integer(1));
                    }
                }
            }
        }
        catch (IOException ex) {
            System.err.println("ERROR: " + src + " " + ex.getMessage());
        }

        return map;
    }

    /**
     * Create a list containing information about which fonts are being used on what
     * page.
     * @param src thr path to a PDF file
     */
    public List<Set<String>> listFontPerPage(String src) {
        List<Set<String>> list = new ArrayList<Set<String>>();

        try {
            PdfReader reader = new PdfReader(src);

            for (int k = 1; k <= reader.getNumberOfPages(); ++k) {
                Set<String> set = new TreeSet<String>();
                PdfDictionary resources = reader.getPageN(k).getAsDict(PdfName.RESOURCES);
                processResource(set, resources);

                list.add(set);
            }
        }
        catch (IOException ex) {
            System.err.println("ERROR: " + src + " " + ex.getMessage());
        }

        return list;
    }

    /**
     * Extracts the font names from page or XObject resources.
     * @param set the set with the font names
     * @param resources the resources dictionary
     * @thorws IOException
     */
    public static void processResource(Set<String> set, PdfDictionary resource) throws IOException {
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
                throw new IOException("ERROR: empty font for key: " + key );
            }

            if (font.getAsName(PdfName.BASEFONT) == null) {
                throw new IOException("ERROR: empty font name for font: " + font );
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

    public boolean isEmbedded(String fontname) {
        return fontname.matches(".*embedded$");
    }

    public boolean isAtRisk(String fontname) {
        if (isEmbedded(fontname)) {
            return false;
        }

        for (int i = 0 ; i < base14.length ; i++) {
            if (fontname.equals(base14[i]))
                return false;
        }

        return true;
    }

    public static void listing(String[] files) throws Exception {
        PDFFonts pf = new PDFFonts();

        for (int i = 0 ; i < files.length ; i++) {
            String path = files[i];

            Set<String> set = pf.listFonts(path);

            int embedded = 0;
            int risk = 0;

            for (String fontname : set) {
                if (pf.isEmbedded(fontname))
                    embedded++;

                if (pf.isAtRisk(fontname))
                    risk++;

                System.out.println(path + " : " + fontname);
            }

            int n = set.size();
            int p = n == 0 ? 0 : 100 * embedded / n;

            System.out.println(path + " : " + n + " fonts; " + p + "% embedded; " + risk + " at risk");
        }
    }

    public static void distribution(String[] files) throws Exception {
        PDFFonts pf = new PDFFonts();

        for (int i = 0 ; i < files.length ; i++) {
            String path = files[i];

            Map<String,Integer> map = pf.listFontDistribution(path);

            map.entrySet();

            for (Map.Entry<String, Integer> entry : map.entrySet()) {
                System.out.println(path + " : " + entry.getKey() + " count " + entry.getValue());
            }
        }
    }

    public static void pages(String[] files) throws Exception {
        PDFFonts pf = new PDFFonts();

        for (int i = 0 ; i < files.length ; i++) {
            String path = files[i];

            List<Set<String>> list = pf.listFontPerPage(path);

            int page = 0;
            for (Set<String> set : list) {
                page++;

                for (String fontname: set) {
                     System.out.println(path + "[" + page + "] : " + fontname);
                }
            }
        }
    }

    public static void main(String[] args) throws Exception {
        boolean doDistribution = false;
        boolean doPages = false;

        if (args.length == 0) {
            System.err.println("usage: PDFFonts [-d] [-p] file [file...]");
            System.exit(1);
        }

        Getopt g = new Getopt("PDFCreator", args, "dp");

        int c;
        while ((c = g.getopt()) != -1) {
             switch(c) {
                 case 'd':
                   doDistribution = true;
                   break;
                 case 'p':
                   doPages = true;
                   break;
             }
        }

        String[] rest = new String[args.length - g.getOptind()];

        for (int i = 0 ; i < rest.length ; i++) {
            rest[i] = args[i + g.getOptind()];
        }

        if (doDistribution)
            distribution(rest);
        else if (doPages)
            pages(rest);
        else
            listing(rest);
    }
}
