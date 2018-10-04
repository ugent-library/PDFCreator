package pdfcreator;

/**
 *
 * @author Nicolas Franck
 *
 */
public class HOCRBox {

    private String text;
    private int [] coordinates;
    private int page;
    private int [] pageCoordinates;
    private HOCRBoxGranularity boxGranularity = HOCRBoxGranularity.WORD;

    /**
     *
     * @param page
     * @param pageCoordinates
     * @param text
     * @param coordinates
     */
    public HOCRBox(int page,int [] pageCoordinates,String text, int [] coordinates){

        this.page = page;
        this.pageCoordinates = pageCoordinates;
        this.text = text;
        this.coordinates = coordinates;

    }
    /**
     * getText
     *
     * returns text string
     *
     * @return String
     */
    public String getText() {
        return text;
    }

    /**
     * getCoordinates
     *
     * returns array of integer coordinates (x1,y1,x2,y2) using the top-left corner as the origin
     * This is how it written in HOCR
     *
     * @return int[]
     */
    public int[] getCoordinates() {
        return coordinates;
    }

    /**
     * getBottomCoordinates
     *
     * returns array of integer coordinates (x1,y1,x2,y2) using the bottom-left corner as the origin
     * this value is derived from the top-left coordinates
     *
     * @return int[]
     */
    public int[] getBottomCoordinates(){

        int pageHeight = pageCoordinates[3];
        int llx = coordinates[0];
        int lly = pageHeight - coordinates[3];
        int urx = coordinates[2];
        int ury = pageHeight - coordinates[1];
        return new int [] { llx,lly, urx, ury };

    }

    /**
     * getPage
     *
     * returns page number, starting at 0
     *
     * @return int
     */
    public int getPage() {
        return page;
    }

    /**
     * getPageWidth
     *
     * returns page width of the current page
     *
     * @return int
     */
    public int getPageWidth(){
        return pageCoordinates[2] - pageCoordinates[0];
    }

    /**
     * getPageHeight
     *
     * returns page height of the current page
     *
     * @return int
     */
    public int getPageHeight(){
        return pageCoordinates[3] - pageCoordinates[1];
    }

    /**
     * getPageCoordinates
     *
     * returns array of integer coordinates (x1,y1,x2,y2) for page, using the top-left corner as the origin
     *
     * @return int[]
     */
    public int [] getPageCoordinates() {
        return pageCoordinates;
    }

    /**
     * getPageBbox
     *
     * get "bbox" value for the current ocr_page
     *
     * @return String
     */
    public String getPageBbox(){
        return pageCoordinates[0]+","+pageCoordinates[1]+","+pageCoordinates[2]+","+pageCoordinates[3];
    }

    /**
     * getBbox
     *
     * get "bbox" value for this box
     *
     * @return
     */
    public String getBbox(){
        return coordinates[0]+","+coordinates[1]+","+coordinates[2]+","+coordinates[3];
    }

    /**
     * getBoxGranularity
     *
     * get granularity. A box can be either a "ocr_word" or a "ocr_line".
     *
     * @return HOCRBoxGranularity
     */
    public HOCRBoxGranularity getBoxGranularity() {
        return boxGranularity;
    }
    public void setBoxGranularity(HOCRBoxGranularity boxGranularity) {
        this.boxGranularity = boxGranularity;
    }
}