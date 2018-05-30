package rst;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.compile;

public class Explorer {
    private Map<Integer, Car> base;
    private DiscManager discManager;
    private Pattern carHtmlBlock;
    private Pattern photoPattern;
    private static Properties prop;
//    private Pattern mainPhotoPattern;

    public static void main(String[] args) {
        initProperties();
        Explorer explorer = new Explorer();
        explorer.go();
    }

    private Explorer() {
        carHtmlBlock = compile("<a class=\"rst-ocb-i-a\" href=\".*?\\d\\d</div></div>");
        photoPattern = compile("var photos = \\[((?:\\d\\d?(?:, )?)+?)];");
        base = new HashMap<>();
        discManager = new DiscManager();
//        mainPhotoPattern = compile("-i-i\".+?src=\"(.+?)\"><h3"); //no-photo.png
    }

    private void go() {
        long start = System.currentTimeMillis();
        String startUrl = prop.getProperty("start_url");
        if (discManager.initBaseFromDisc(base)) {
            deepCheck();
        }

        System.out.println("\nScanning html");
        int pageNum = 1;
        int topId = 0, markerId = 0;
        long startTime, fullDelay = Integer.parseInt(prop.getProperty("page_load_interval")) * 1000;
        boolean firstCycle = true;
        while (true) {
            startTime = System.currentTimeMillis();
            try {
                String html = HtmlGetter.getURLSource(startUrl + "&start=" + pageNum);
                ArrayList<String> carsHtml = new ArrayList<>(40);
                Matcher m = carHtmlBlock.matcher(html);
                while (m.find()) {
                    carsHtml.add(m.group());
                }
                for (String carHtml : carsHtml) {
                    Car car = Car.getCarFromHtml(carHtml);
                    if (car != null) {
                        int id = car.getId();
                        if (carsHtml.indexOf(carHtml) == 0) {
                            topId = id;
                        }
                        if (markerId == id) {
                            markerId = topId;
                            if (firstCycle) {
                                System.out.print("\nPage " + (pageNum - 1) + " is last (" +
                                        ((System.currentTimeMillis() - start) / 1000) + "s), repeating");
                            } else {
                                System.out.print("/");
                            }
                            pageNum = 1;
                            firstCycle = false;
                        }
                        if (markerId == 0) {
                            markerId = topId;
                        }
                        if (!base.containsKey(id)) {
                            if (!car.isSoldOut()) { //Add car to base
                                ImageGetter imageGetter = new ImageGetter();
                                car.addDetails();
                                discManager.writeCarOnDisc(car, true);
                                base.put(id, car);
                                report(car);
                                if (!firstCycle) {
                                    Mail.sendCar(car);
                                }
                                if (car.getImages() != null) {
                                    imageGetter.downloadImages(car, null);
                                }
                            } //else ignore this car
                        } else {
                            checkCarForUpdates(car, !firstCycle);
                        }
                    }
                }
                pageNum++;
                System.out.print(".");
                long delay = fullDelay - (System.currentTimeMillis() - startTime);
                if (delay > 0) {
                    Thread.sleep(delay);
                }
            } catch (IOException | InterruptedException e) {
                String err = e.getMessage();
                System.out.print(err);
                if (err.contains("403 for URL")) {
                    Mail.alarmByEmail("Ahtung!!!", "Всё пропало!\n403 FORBIDDEN!");
                    System.exit(1);
                }
            }
        }
    }

    private void checkCarForUpdates(Car car, boolean sendEmail) {
        Car oldCar = base.get(car.getId());
        boolean hasChanges = false;

        if (oldCar.getPrice() != car.getPrice()) {
            hasChanges = true;
            String comment = "Цена изменена с " + oldCar.getPrice() + "$ на " + car.getPrice() + "$ " + CalendarHelper.getTimeStamp();
            oldCar.getComments().add(comment);
            oldCar.setPrice(car.getPrice());
            System.out.print(comment + " - " + car.getId());
        }
        String desc = car.getDescription(), oldDesc = oldCar.getDescription();
        if (!desc.equals("big") && !desc.equals(oldDesc) && !desc.equals("") && oldDesc.length() < 120) {
            hasChanges = true;
            String comment = "Старое описание: " + oldCar.getDescription() + " " + CalendarHelper.getTimeStamp();
            oldCar.getComments().add(comment);
            oldCar.setDescription(car.getDescription());
            System.out.print(comment + " - " + car.getId());
        }
        if (car.isSoldOut()) {
            hasChanges = true;
            oldCar.setSoldOut(true);
            base.remove(oldCar.getId());
            String comment = "Автомобиль продан! Время отметки: " + CalendarHelper.getTimeStamp();
            oldCar.getComments().add(comment);
            System.out.println("\n(" + car.getId() + ")Автомобиль продан!");
        }
        if (hasChanges) {
            discManager.writeCarOnDisc(oldCar, false);
            if (sendEmail) {
                Mail.sendCar("Изменения в авто!", oldCar, "см. изменения в комментариях");
            }
        }
    }

    private void deepCheck() {
        Set<Map.Entry<Integer, Car>> entrySet = base.entrySet();
        Iterator<Map.Entry<Integer, Car>> iterator = entrySet.iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer, Car> entry = iterator.next();
            Car car = entry.getValue();
            try {
                String src = HtmlGetter.getURLSource("http://m.rst.ua/" + car.getLink());
                if (!src.contains(String.valueOf(car.getId())) && src.contains("<p>При использовании материалов, ссылка на RST обязательна.</p>")) {
                    car.setSoldOut(true);
                    String comment = "Объявление удалено! Время отметки: " + CalendarHelper.getTimeStamp();
                    car.getComments().add(comment);
                    System.out.println("\n(" + car.getId() + ")Объявление удалено!");
                    discManager.writeCarOnDisc(car, false);
                    iterator.remove();
                }
                Matcher photo = photoPattern.matcher(src);
                if (photo.find()) {
                    String[] ar = photo.group(1).split(", ");
                    if (ar.length > 0) {
                        ArrayList<Integer> list = new ArrayList<>();
                        for (String s : ar) {
                            int i = Integer.parseInt(s);
                            if (!car.getImages().contains(i)) {
                                list.add(i);
                                car.getImages().add(i);
                            }
                        }
                        if (list.size() > 0) {
                            new ImageGetter().downloadImages(car, list);
                            discManager.writeCarOnDisc(car, false);
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void report(Car car) {
        System.out.printf("%n%6s %-6s%-8dРегион:%-15sГород:%-11sПродано:%-6sСвежее:%-5s%5s$%5s%17s %-26s%s%n",
                car.getBrand(), car.getModel(), car.getId(), car.getRegion(), car.getTown(), car.isSoldOut(), car.isFreshDetected(),
                car.getPrice(), car.getBuildYear(), car.getDetectedDate(), car.getEngine(), car.getDescription());
    }

    private static void initProperties() {
        prop = new Properties();
        try (InputStream input = new FileInputStream("./config.properties")) {
            prop.load(input);
        } catch (IOException ex) {
            System.out.println("File 'config.properties' is not found!");
            System.exit(1);
        }
    }

    static Properties getProp() {
        return prop;
    }

    static class CalendarHelper {
        private static DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
        private static DateFormat fullDateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

        private static Date yesterday() {
            final Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DATE, -1);
            return cal.getTime();
        }

        static String getYesterdayDateString() {
            return dateFormat.format(yesterday());
        }

        static String getTodayDateString() {
            return dateFormat.format(new Date());
        }

        private static String getTimeStamp() {
            return fullDateFormat.format(new Date());
        }
    }
}
