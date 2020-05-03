import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Bot {
    public static void main(String[] args) {
        testImageLoader();
    }

    public static void testImageLoader(){
        ImageLoader test = new ImageLoader("Смоленск", 5);
        int i = 1;
        while (!test.isEmpty()){
            Image img = test.getNextImage();
            if (img == null) break;
            BufferedImage bf = (BufferedImage) img;
            File out = new File("img" + i++ + ".png");
            try {
                ImageIO.write(bf,"png", out);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
