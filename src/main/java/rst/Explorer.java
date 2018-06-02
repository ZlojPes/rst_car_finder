package rst;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.compile;

public class Explorer {
    private Map<Integer, Car> base;
    private DiscManager discManager;
    private Pattern carHtmlBlock;
    private static Properties prop;
//    private Pattern mainPhotoPattern;

    public static void main(String[] args) {
        initProperties();
        Explorer explorer = new Explorer();
        explorer.go();
    }

    private Explorer() {
        carHtmlBlock = compile("<a class=\"rst-ocb-i-a\" href=\".*?\\d\\d</div></div>");
        base = new HashMap<>();
        discManager = new DiscManager();
//        mainPhotoPattern = compile("-i-i\".+?src=\"(.+?)\"><h3"); //no-photo.png
    }

    private void go() {
        long start = System.currentTimeMillis();
        String startUrl = prop.getProperty("start_url");
        if (discManager.initBaseFromDisc(base)) {
            Car.deepCheck(base, discManager);
        }
        System.out.println("\nScanning html");
        int pageNum = 1;
        int topId = 0, markerId = 0;
        long startCycle, fullDelay = Integer.parseInt(prop.getProperty("page_load_interval_seconds")) * 1000;
        long deepCheckDelay = Integer.parseInt(prop.getProperty("deep_check_interval_hours")) * 3600000;
        boolean firstCycle = true;
        while (true) {
            startCycle = System.currentTimeMillis();
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
                        if (markerId == id || pageNum == 1000) {
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
                            if (!car.isSoldOut() && car.isCarAlive()) { //Add car to base
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
                long delay = fullDelay - (System.currentTimeMillis() - startCycle);
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
            if (System.currentTimeMillis() - start > deepCheckDelay) {
                Car.deepCheck(base, discManager);
                System.out.println("deepCheck() forced");
                start = System.currentTimeMillis();
            }
        }
    }

    private void checkCarForUpdates(Car car, boolean sendEmail) {
        Car oldCar = base.get(car.getId());
        boolean hasChanges = false;

        if (oldCar.getPrice() != car.getPrice()) {
            hasChanges = true;
            String comment = "Цена изменена с " + oldCar.getPrice() + "$ на " + car.getPrice() + "$ " + CalendarUtil.getTimeStamp();
            oldCar.getComments().add(comment);
            oldCar.setPrice(car.getPrice());
            System.out.print(comment + " - " + car.getId());
        }
        String desc = car.getDescription(), oldDesc = oldCar.getDescription();
        if (!desc.equals("big") && !desc.equals(oldDesc) && !desc.equals("") && oldDesc.length() < 120) {
            hasChanges = true;
            String comment = "Старое описание: " + oldCar.getDescription() + " " + CalendarUtil.getTimeStamp();
            oldCar.getComments().add(comment);
            oldCar.setDescription(car.getDescription());
            System.out.print(comment + " - " + car.getId());
        }
        if (car.isSoldOut()) {
            hasChanges = true;
            oldCar.setSoldOut();
            base.remove(oldCar.getId());
            String comment = "Автомобиль продан! Время отметки: " + CalendarUtil.getTimeStamp();
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
}
