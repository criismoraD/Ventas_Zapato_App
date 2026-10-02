package com.mycompany.senati_zapato;

import java.net.URL;
import java.io.File;
import javax.imageio.ImageIO;
import java.awt.Image;

public class TestImageLoad {
    public static void main(String[] args) {
        try {
            String imgPath = "zapatos_imagenes/zapato2.jpeg";
            File file = new File(imgPath);
            String fileName = file.getName();
            System.out.println("fileName: " + fileName);
            
            URL url = TestImageLoad.class.getResource("/images/" + fileName);
            System.out.println("URL: " + url);
            
            if (url != null) {
                Image image = ImageIO.read(url);
                System.out.println("Image loaded successfully? " + (image != null));
            } else {
                System.out.println("URL is null!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
