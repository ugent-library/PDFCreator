package pdfcreator;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.BadPdfFormatException;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfImportedPage;
import com.itextpdf.text.pdf.PdfReader;
import gnu.getopt.Getopt;
import java.io.IOException;

/*
 *  Copyright (c) 2020 . Patrick Hochstenbach <Patrick.Hochstenbach@gmail.com>
 */

/**
 *
 * @author hochsten
 */
public class PDFUpdater {
    private int pageNumber = -1;
    private boolean delete = false;
    private boolean blur   = false;
    
    public PDFUpdater(int pageNumber, boolean delete, boolean blur) {
        this.pageNumber = pageNumber;
        this.delete     = delete;
        this.blur       = blur;
    }
    
    private void copyInsertedPages(PdfCopy copier, PdfReader reader, String filename) throws IOException,BadPdfFormatException {
        if (reader != null) {
            for (int l = 1 ; l <= reader.getNumberOfPages(); l += 1) {
                System.err.println("Reading: " + filename + " pg. " + l);
                PdfImportedPage page = copier.getImportedPage(reader, l);
                copier.addPage(page);
            }
        }
    } 
    
    protected void updatePage(String sourceFile, String insertFile) throws IOException {
        PdfReader sourceReader  = new PdfReader(sourceFile);
        PdfReader insertReader  = null;
        
        if (insertFile != null) {
            insertReader= new PdfReader(insertFile);
        }
        
        Document doc = new Document(sourceReader.getPageSize(1));
        
        try {
            PdfCopy copier = new PdfCopy(doc, System.out);
            
            doc.open();
            
            for (int k = 1; k <= sourceReader.getNumberOfPages(); k += 1) {
                System.err.println("Reading: " + sourceFile + " pg. " + k);
                    
                if (k == this.pageNumber) {
                    // Skip this page if required
                    if (delete) {
                        System.err.println("Deleted: " + sourceFile + " pg. " + k);
                    }
                    else {
                        // Get page from the source
                        PdfImportedPage page = copier.getImportedPage(sourceReader, k);
                       
                        if (this.blur) {
                            System.err.println("blur not yet supported");
                           
//                            Image image = Image.getInstance("https://hochstenbach.files.wordpress.com/2019/12/20190528_sktchy.jpg?w=710");
//                           
//                            copier.addDirectImageSimple(image);
                        }
                        else {
                            copier.addPage(page);
                        }
                    }
                    
                    // Copy the pages from the insert file if available
                    copyInsertedPages(copier, insertReader, insertFile);
                }
                else {
                    // Get page from the source
                    PdfImportedPage page = copier.getImportedPage(sourceReader, k);
                    copier.addPage(page);
                }
            }
            
            // Copy at the end if no page number has been provided
            if (this.pageNumber == -1) {
               // Copy the pages from the insert file if available
               copyInsertedPages(copier, insertReader, insertFile);
            }
            
            doc.close();
            copier.close();
            sourceReader.close();
            
            if (insertReader != null) {
                insertReader.close();
            }
        }
        catch (DocumentException ex) {
            throw new IOException(ex);
        }
    }
         
    protected static void usage() {
        System.err.println("usage:");
        System.err.println();
        System.err.println(" PDFUpdater [-d] [-i pagenr] file.pdf [pages.pdf] > output.pdf");
        System.err.println();
        System.err.println("options:");
        System.err.println();
        System.err.println("   -d  delete the page first");
        System.err.println("   -i  insert pages.pdf at this page, without this option the pages will be inserted at the end");
        System.exit(1);
    }
    
    /**
     * @param args the command line arguments
     * @throws Exception in case of PDF creation errors
     */
    public static void main(String[] args) throws Exception {
        int pageNumber = -1;
        boolean delete = false;
        boolean blur   = false;
    
        Getopt g = new Getopt("PDFCreator", args, "bdi:");
        
        int c;
        
        while ((c = g.getopt()) != -1) {
            switch(c) {
                case 'b':
                    blur = true;
                    break;
                case 'd':
                    delete = true;
                    break;
                case 'i':
                    pageNumber = Integer.parseInt(g.getOptarg());
                    break;
                default:
                    usage();
                    break;
            }
        }
        
        if (args.length - g.getOptind() < 1) {
            usage();
        }
        
        PDFUpdater updater = new PDFUpdater(pageNumber,delete, blur);
        
        String pdfFile = args[g.getOptind() + 0];
        String insertFile = null;
        
        if (args.length - g.getOptind() == 2) {
            insertFile = args[g.getOptind() + 1];
        }
        
        updater.updatePage(pdfFile, insertFile);
    }
}
