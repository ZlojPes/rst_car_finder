package rst;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class ImageGetter {
    private boolean downloadImage(String sourceUrl, String savePath) {
        URL url = null;
        try {
            url = new URL(sourceUrl);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        if (url != null) {
            try {
                InputStream in = new BufferedInputStream(url.openStream());
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                byte[] buf = new byte[1024];
                int n;
                while (-1 != (n = in.read(buf))) {
                    out.write(buf, 0, n);
                }
                out.close();
                in.close();
                byte[] response = out.toByteArray();
                FileOutputStream fos = new FileOutputStream(savePath);
                fos.write(response);
                fos.close();
            } catch (IOException e) {
                return false;
            }
        } else {
            return false;
        }
        return true;
    }

    void downloadImages(Car car, ArrayList<Integer> list) {
        String path = DiscManager.getMainPath() + "\\" + car.getId() + "_" + car.getBrand() + "_" + car.getModel() + "\\";
        for (int i : car.getImages()) {
            if (list == null || list.contains(i)) {
                new Thread(() -> {
                    String fullPath = path + i + ".jpg";
                    String url = "http://img.rstcars.com/oldcars/" + car.getBrand() + "/" + car.getModel() + "/big/" + car.getId() + "-" + i + ".jpg";
                    String secondUrl;
                    if (!downloadImage(url, fullPath)) {
                        secondUrl = "http://img1.rstcars.com/oldcars/" + car.getBrand() + "/" + car.getModel() + "/big/" + car.getId() + "-" + i + ".jpg";
                        downloadImage(secondUrl, fullPath);
                    }
                    if (list != null) {
                        System.out.println("Загружено новое фото " + i + " в папку авто " + car.getId() + ";");
                    }
                }).start();
            }
        }
    }

    public static void main(String[] args) {
        new ImageGetter().downloadImage("http://img.rstcars.com/oldcars/daewoo/sens/big/7485197-4.jpg", "d://saved_image.jpg");
    }
}
