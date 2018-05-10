package rst;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;

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
                e.printStackTrace();
                return false;
            }
        } else {
            return false;
        }
        return true;
    }

    void downloadAllImages(Car car) {
        String path = Main.MAIN_PATH + "\\" + car.getId() + "_" + car.getBrand() + "_" + car.getModel() + "\\";
        for (int i : car.getImages()) {
            new Thread(() -> {
                String fullPath = path + i + ".jpg";
                String url = "http://img.rstcars.com/oldcars/" + car.getBrand() + "/" + car.getModel() + "/big/" + car.getId() + "-" + i + ".jpg";
                downloadImage(url, fullPath);
            }).start();
        }
    }

    public boolean downloadAbsentImages(Car car) {
        return true;
    }

    public static void main(String[] args) {
        new ImageGetter().downloadImage("http://img.rstcars.com/oldcars/daewoo/sens/big/7485197-4.jpg", "d://saved_image.jpg");
    }
}
