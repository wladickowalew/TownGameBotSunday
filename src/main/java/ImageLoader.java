import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;

public class ImageLoader {

    private final String SERVER = "https://yandex.ru/images/search";

    private int image_count;
    private ArrayList<String> imageUrls;

    public ImageLoader(String town, int image_count){
        this.image_count = image_count;
        imageUrls = getImageURLs(town);
    }

    public ImageLoader(ImageLoader loader){
        image_count = loader.getImage_count();
        imageUrls = (ArrayList<String>) loader.getImageUrls().clone();
    }

    protected ImageLoader clone(){
        return new ImageLoader(this);
    }

    public int getImage_count() {
        return image_count;
    }

    public ArrayList<String> getImageUrls() {
        return imageUrls;
    }

    public boolean isEmpty(){
        return imageUrls.isEmpty();
    }

    public Image getNextImage(){
        try {
            return ImageIO.read(new URL(imageUrls.remove(0)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getNextImageURL(){
        return imageUrls.remove(0);
    }

    private ArrayList<String> getImageURLs(String town){
        try{
            String URL = SERVER + "?text=" + town;
            Document html = Jsoup.connect(URL).get();
            Elements imgs = html.select(".serp-item__link img");
            System.out.println(imgs.size());
            ArrayList<String> urls = new ArrayList();
            for (Element img: imgs){
                String url = img.absUrl("src");
                urls.add(url);
            }
            Collections.shuffle(urls);
            urls = new ArrayList(urls.subList(0, image_count));
            return urls;
        }catch(IOException e){
            e.printStackTrace();
        }
        return null;
    }
}
