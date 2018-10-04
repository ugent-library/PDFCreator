package pdfcreator;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author Nicolas Franck
 */
public class HOCRReader implements Iterable<HOCRBox>{
    private File file;
    private HOCRBoxGranularity boxGranularity = HOCRBoxGranularity.WORD;

    public HOCRReader(File file){
        this.file = file;
    }

    public HOCRReader(File file,HOCRBoxGranularity boxGranularity){
        this.file = file;
        this.boxGranularity = boxGranularity;
    }

    public File getFile(){
        return file;
    }

    public HOCRBoxGranularity getBoxGranularity() {
        return boxGranularity;
    }

    public void setBoxGranularity(HOCRBoxGranularity boxGranularity) {
        this.boxGranularity = boxGranularity;
    }

    @Override
    public Iterator<HOCRBox> iterator() {
        return new HOCRReader.HOCRBoxIterator();
    }

    private class HOCRBoxIterator implements Iterator {

        private HOCRBox hocrBox;
        private boolean getNext = true;
        private Document document;
        private Elements pages;
        private int currentPageIndex = -1;
        private int [] currentPageCoordinates;
        private Elements boxes;

        private Document getDocument() throws IOException {

            if ( document == null){
                document = Jsoup.parse(getFile(),"UTF-8");
            }
            return document;

        }

        private Elements getPages() throws IOException{

            if (pages == null){
                pages = getDocument().getElementsByClass("ocr_page");
            }
            return pages;

        }

        private void getNextHocrBox() throws IOException{

            hocrBox = null;

            if( boxes == null || boxes.isEmpty()){
                currentPageIndex++;
                if(currentPageIndex >= getPages().size())
                    return;
                Element page = getPages().get(currentPageIndex);
                currentPageCoordinates = titleExtractBbox(page.attr("title"));

                if (boxGranularity == HOCRBoxGranularity.LINE){
                    boxes = page.getElementsByClass("ocr_line");
                } else {
                    boxes = page.getElementsByClass("ocr_word");
                    boxes.addAll(page.getElementsByClass("ocrx_word"));
                }
            }

            Element box = boxes.first();

            if(box == null) return;

            boxes.remove(0);

            int[] coordinates = titleExtractBbox(box.attr("title"));

            hocrBox = new HOCRBox(currentPageIndex,currentPageCoordinates, box.text(), coordinates);
            hocrBox.setBoxGranularity(boxGranularity);

        }

        private int [] titleExtractBbox(String value){

            Map<String,String>ocrAttributes = parseTitle(value);
            String bbox = ocrAttributes.get("bbox");

            String [] strCoords = bbox.split(" ");
            int [] coords = {Integer.parseInt(strCoords[0]),
                Integer.parseInt(strCoords[1]),
                Integer.parseInt(strCoords[2]),
                Integer.parseInt(strCoords[3])};
            return coords;
        }

        private Map<String,String>parseTitle(String value){
            HashMap<String,String> map = new HashMap<String,String>();

            String [] pairs = value.split(";");

            for(String pair:pairs){
                //replace spaces at start and end!
                String p = pair.trim();
                int pos = p.indexOf(' ');
                if( pos > 0){
                    String k = p.substring(0, pos);
                    String v = p.substring(pos+1);
                    map.put(k,v);
                }
                else{
                    map.put(pair,"");
                }

            }

            return map;
        }

        private Map<String,String>parseStyle(String value){
            HashMap<String,String> map = new HashMap<String,String>();

            String [] pairs = value.split(";");

            for(String pair:pairs){
                int pos = pair.indexOf(':');
                String k = pair.substring(0, pos).trim();
                String v = pair.substring(pos+1).trim().replace("\"","");
                map.put(k,v);
            }

            return map;
        }

        @Override
        public boolean hasNext() {

            if(getNext){

                try {
                    getNextHocrBox();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                getNext = false;

            }

            return hocrBox != null;
        }

        @Override
        public HOCRBox next() {

            getNext = hocrBox != null;
            return hocrBox;

        }

        @Override
        public void remove() {

        }
    }
}