/*
 *  Copyright (c) 2018 . Patrick Hochstenbach <Patrick.Hochstenbach@ugent.be>
 */
package pdfcreator;

import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.parser.ImageRenderInfo;
import com.itextpdf.text.pdf.parser.PdfImageObject;
import com.itextpdf.text.pdf.parser.RenderListener;
import com.itextpdf.text.pdf.parser.TextRenderInfo;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 *
 * @author hochsten
 */
public class ImageRenderListener implements RenderListener {
    /** The new document to which we've added a border rectangle. */
    protected String path = "";
    protected int page = 0;
 
    /**
     * Creates a RenderListener that will look for images.
     */
    public ImageRenderListener(String path) {
        this.path = path;
    }
 
    /**
     * @see com.itextpdf.text.pdf.parser.RenderListener#beginTextBlock()
     */
    public void beginTextBlock() {
    }
 
    /**
     * @see com.itextpdf.text.pdf.parser.RenderListener#endTextBlock()
     */
    public void endTextBlock() {
    }
 
    /**
     * @see com.itextpdf.text.pdf.parser.RenderListener#renderImage(
     *     com.itextpdf.text.pdf.parser.ImageRenderInfo)
     */
    public void renderImage(ImageRenderInfo renderInfo) {
        try {
            String filename;
            FileOutputStream os;
            PdfImageObject image = renderInfo.getImage();
            if (image == null) return;
            filename = String.format("%s/%s.%s",path, ++page, image.getFileType());
            os = new FileOutputStream(filename);
            System.out.println("creating file " + filename);
            os.write(image.getImageAsBytes());
            os.flush();
            os.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
 
    /**
     * @see com.itextpdf.text.pdf.parser.RenderListener#renderText(
     *     com.itextpdf.text.pdf.parser.TextRenderInfo)
     */
    public void renderText(TextRenderInfo renderInfo) {
    }
}
