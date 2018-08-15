package rst;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.*;

public class HtmlGetter {
    static String getURLSource(String url) throws IOException {
        URL urlObject = new URL(url);
        URLConnection urlConnection = urlObject.openConnection();
        urlConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:61.0) Gecko/20100101 Firefox/61.0");
        urlConnection.setRequestProperty("Referer", "http://rst.ua/oldcars/daewoo/lanos/");
        urlConnection.setRequestProperty("Cookie", "_rst=5b71d42c244f71.96947827.18; __utma=220962331.1453490248.1534186539.1534200411.1534352755.4; __utmz=220962331.1534186539.1.1.utmcsr=(direct)|utmccn=(direct)|utmcmd=(none); _rst_u=5b71d7e89be2f7.49225810.17; PHPSESSID=68901vn436sp42hs4hhc6iusi2; __utmc=220962331; c8557071a593cd9c53c8af71a2b542a8=d0d8456be7c273eb21abb83ce6e24ccf; __utmb=220962331.1.10.1534352756; __utmt=1");
        return toString(urlConnection.getInputStream());
    }

    private static String toString(InputStream inputStream) {
        ExecutorService executor = Executors.newCachedThreadPool();
        Callable<String> task = () -> {
            String inputLine;
            StringBuilder stringBuilder = new StringBuilder();
            try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "Cp1251"))
//                 ;BufferedWriter writer = new BufferedWriter(new FileWriter(new File("d:\\httpGetterOut.htm")))
            ) {
                while ((inputLine = bufferedReader.readLine()) != null) {
                    stringBuilder.append(inputLine);
//                    writer.write(inputLine);
//                    writer.newLine();
                }
            } catch (IOException e) {
                System.out.print(e.getMessage());
            }
            return stringBuilder.toString();
        };
        Future<String> future = executor.submit(task);
        String result = "Connection failed!";
        try {
            result = future.get(10, TimeUnit.SECONDS);
        } catch (TimeoutException ex) {
            System.out.print("TimeOut");
        } catch (InterruptedException e) {
            System.out.print("InterruptedException");
        } catch (ExecutionException e) {
            System.out.print("ExecutionException");
        } finally {
            future.cancel(true);
        }
        return result;
    }

    public static void main(String[] args) {
        try {
            System.out.println(getURLSource("http://m.rst.ua/oldcars/opel/omega/opel_omega_8063720.html"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
