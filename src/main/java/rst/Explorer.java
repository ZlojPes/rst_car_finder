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
    private Pattern carHtmlBlock;
    private static Properties prop;
    private static Set<String> keys;
//    private Pattern mainPhotoPattern;

    public static void main(String[] args) {
        initProperties();
        keys = new HashSet<>(Arrays.asList(args));
        Explorer explorer = new Explorer();
        explorer.go();
    }

    private Explorer() {
        carHtmlBlock = compile("<a class=\"rst-ocb-i-a\" href=\".*?\\d\\d</div></div>");
        base = new HashMap<>();
//        mainPhotoPattern = compile("-i-i\".+?src=\"(.+?)\"><h3"); //no-photo.png
    }

    private void go() {
        long start = System.currentTimeMillis();
        String startUrl = prop.getProperty("start_url");
        boolean alwaysSendEmail = keys.contains("-m");
        if (DiscManager.initBaseFromDisc(base)) {
            System.out.println(base.size() + " alive cars found and added to base");
            System.out.print("Checking each car...");
            deepCheck(alwaysSendEmail);
            System.out.println("complete");
        }
        System.out.print("Scanning html");
        int pageNum = 1, previousCycleMaxPage = 1000;
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
                        if (markerId == id || pageNum -1 > previousCycleMaxPage) {
                            markerId = topId;
                            if (firstCycle) {
                                System.out.print("\nPage " + (pageNum - 1) + " is last (" +
                                        ((System.currentTimeMillis() - start) / 1000) + "s), repeating");
                            } else {
                                System.out.print("|");
                            }
                            previousCycleMaxPage = pageNum;
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
                                Seller.addNewSeller(car);
                                DiscManager.writeCarOnDisc(car, true);
                                DiscManager.writeSellersBase();
                                base.put(id, car);
                                report(car);
                                if (!firstCycle || alwaysSendEmail) {
                                    Mail.sendCar("Обнаружено новое авто!", car, "Краткое описание:");
                                }
                                if (car.getImages() != null) {
                                    imageGetter.downloadImages(car, null);
                                }
                            } //else ignore this car
                        } else {
                            checkCarForUpdates(car, !firstCycle || alwaysSendEmail);
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
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e1) {
                    System.out.println("NF");
                }
            }
            if (System.currentTimeMillis() - start > deepCheckDelay) {
                System.out.print("deepCheck()");
                deepCheck(true);
                start = System.currentTimeMillis();
            }
        }
    }

    private void checkCarForUpdates(Car carFromSite, boolean sendEmail) {
        Car carFromBase = base.get(carFromSite.getId());
        boolean hasChanges = false;
        String message = null;
        if (carFromBase.getPrice() != carFromSite.getPrice()) {
            hasChanges = true;
            String comment = "Цена изменена с " + carFromBase.getPrice() + "$ на " + carFromSite.getPrice() + "$ (" + CalendarUtil.getTimeStamp() + ")";
            carFromBase.addComment(comment);
            message = comment;
            carFromBase.setPrice(carFromSite.getPrice());
            System.out.print("\n" + comment + " - " + carFromSite.getId());
        }
        String desc = carFromSite.getDescription(), oldDesc = carFromBase.getDescription();
        if (!desc.equals("big") && !desc.equals(oldDesc) && !desc.equals("") && oldDesc.length() < 120) {
            hasChanges = true;
            String comment = "Старое описание: " + carFromBase.getDescription() + " (" + CalendarUtil.getTimeStamp() + ")";
            carFromBase.addComment(comment);
            message = "Правка описания";
            carFromBase.setDescription(carFromSite.getDescription());
            System.out.println("\n" + "desc changed (" + carFromSite.getId() + ")");
        }
        if (carFromSite.isSoldOut()) {
            hasChanges = true;
            carFromBase.setSoldOut();
            base.remove(carFromBase.getId());
            String comment = "Автомобиль продан! (" + CalendarUtil.getTimeStamp() + ")";
            carFromBase.addComment(comment);
            message = comment;
            System.out.println("\n(" + carFromSite.getId() + ")Автомобиль продан!");
        }
        if (hasChanges) {
            DiscManager.writeCarOnDisc(carFromBase, false);
            if (sendEmail) {
                Mail.sendCar("Изменения в авто!", carFromBase, message);
            }
        }
    }

    private void deepCheck(boolean sendMail) {
        int sellerBaseHash = Seller.getBaseHash();
        Set<Map.Entry<Integer, Car>> entrySet = base.entrySet();
        Iterator<Map.Entry<Integer, Car>> iterator = entrySet.iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer, Car> entry = iterator.next();
            Car car = entry.getValue();
            boolean carWasChanged;
            String message = "(см. историю изменений)";
            try {
                String src = HtmlGetter.getURLSource("http://m.rst.ua/" + car.getLink());
                if (!src.contains(String.valueOf(car.getId())) && src.contains("<p>При использовании материалов, ссылка на RST обязательна.</p>")) {
                    message = "Объявление удалено! (" + CalendarUtil.getTimeStamp() + ")";
                    car.addComment(message);
                    System.out.println("\n(" + car.getId() + ")DEL");
                    iterator.remove();
                    car.setSoldOut();
                    carWasChanged = true;
                } else {
                    carWasChanged = car.addDetails(src, true);
                }
                if (carWasChanged) {
                    DiscManager.writeCarOnDisc(car, false);
                    Seller.addNewSeller(car);
                    if (sendMail) {
                        Mail.sendCar("Изменения в авто!", car, message);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (sellerBaseHash != Seller.getBaseHash()) {
                DiscManager.writeSellersBase();
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
