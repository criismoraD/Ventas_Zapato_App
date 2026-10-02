package com.mycompany.senati_zapato;

public class TestImageLogic {
    public static void main(String[] args) {
        try {
            String imgPath = "zapatos_imagenes/zapato2.jpeg";
            String fName = imgPath;
            if (fName.contains("/")) fName = fName.substring(fName.lastIndexOf("/") + 1);
            if (fName.contains("\\")) fName = fName.substring(fName.lastIndexOf("\\") + 1);

            java.awt.Image image = null;

            java.net.URL url = TestImageLogic.class.getResource("/images/" + fName);
            if (url != null) {
                try { 
                    image = javax.imageio.ImageIO.read(url);
                    System.out.println("Classpath load OK. URL: " + url + " Image: " + image);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (image == null) {
                String basePath = System.getProperty("user.dir");
                if (basePath.endsWith("target") || basePath.endsWith("target" + java.io.File.separator)) {
                    basePath = new java.io.File(basePath).getParent();
                }
                java.io.File devFile = new java.io.File(basePath, "src/main/resources/images/" + fName);
                if (devFile.exists()) {
                    try { 
                        image = javax.imageio.ImageIO.read(devFile); 
                        System.out.println("DevFile load OK. Path: " + devFile.getAbsolutePath() + " Image: " + image);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    System.out.println("DevFile does not exist: " + devFile.getAbsolutePath());
                }
            }
            
            System.out.println("Final image: " + image);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
