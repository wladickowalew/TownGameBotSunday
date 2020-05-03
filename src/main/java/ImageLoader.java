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
    private ArrayList<URL> imageUrls;

    public ImageLoader(String town, int image_count){
        this.image_count = image_count;
        imageUrls = getImageURLs(town);
    }

    public boolean isEmpty(){
        return imageUrls.isEmpty();
    }

    public Image getNextImage(){
        try {
            return ImageIO.read(imageUrls.remove(0));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private ArrayList<URL> getImageURLs(String town){
        try{
            String URL = SERVER + "?text=" + town;
            Document html = Jsoup.connect(URL).get();
            Elements imgs = html.select(".serp-item__link img");
            System.out.println(imgs.size());
            ArrayList<URL> urls = new ArrayList();
            for (Element img: imgs){
                String url = img.absUrl("src");
                urls.add(new URL(url));
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
