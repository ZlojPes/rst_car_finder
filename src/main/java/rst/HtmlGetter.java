package rst;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;

public class HtmlGetter {
    static String getURLSource(String url) throws IOException {
        System.out.println("start htmlGetter");
        URL urlObject = new URL(url);
        URLConnection urlConnection = urlObject.openConnection();
        urlConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/30.0.1599.101 Safari/537.36");

        return toString(urlConnection.getInputStream());
    }

    private static String toString(InputStream inputStream) {
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "Cp1251"))
                /*;BufferedWriter writer = new BufferedWriter(new FileWriter(new File("d:\\htmlOutm.html")))*/) {
            String inputLine;
            StringBuilder stringBuilder = new StringBuilder();
            while ((inputLine = bufferedReader.readLine()) != null) {
                stringBuilder.append(inputLine);
//                writer.write(inputLine);
//                writer.newLine();
            }

            System.out.println("end while() toString");
            return stringBuilder.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "connection failed";
    }

    public static void main(String[] args) {
        try {
            getURLSource("http://m.rst.ua/oldcars/daewoo/sens/daewoo_sens_7306453.html");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
