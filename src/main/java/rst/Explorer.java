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
    private Map<Integer, Car> base = new HashMap<>();
    private DiscManager discManager = new DiscManager();
    private Pattern idPattern;
    private Pattern pricePattern;
    private Pattern buildYearPattern;
    private Pattern descriptionPattern;
    private Pattern datePattern;
    private Pattern linkToCarPage;
    private Pattern carHtmlBlock;
    private Pattern regionPattern;
    private Pattern bigDescription;
    private Pattern townPattern;
    private Pattern contactsPattern;
    private Pattern namePattern;
    private Pattern telPattern;
    private Pattern photoPattern;
    private Pattern enginePattern;
    private Pattern conditionPattern;
    private static Properties prop;
//    private Pattern mainPhotoPattern;

    // TODO parsing/writing JSON data file;
    // TODO regular check car for update;

    public static void main(String[] args) {
        initProperties();
        Explorer explorer = new Explorer();
        explorer.initPatterns();
        explorer.go();
    }

    private void initPatterns() {
        datePattern = compile("(?:размещено|обновлено).+?((?:\\d{2}\\.){0,2}\\d{2}:?\\d{2})</div>");
        idPattern = compile("\\d{7,}\\.html$");
        pricePattern = compile("данные НБУ\">\\$\\d{0,3}'?\\d{3}</span>");
        buildYearPattern = compile("d-l-i-s\">(\\d{4})");
        descriptionPattern = compile("-d-d\">(.*?)</div>");
        linkToCarPage = compile("oldcars/.+\\d+\\.html");
        carHtmlBlock = compile("<a class=\"rst-ocb-i-a\" href=\".*?\\d\\d</div></div>");
        regionPattern = compile("Область:.+?>([А-Яа-я]+?)</span>");
        bigDescription = compile("desc rst-uix-block-more\">.+?</div>");
        townPattern = compile("(\">(\\D+?)</a></span>Город<)|(Город</td>.+?title=\".*?авто.*?\">(\\D+?)</a>)");
        contactsPattern = compile("<h3>Контакты:</h3.+?</div></div>");
        namePattern = compile("<strong>.+</strong>");
        telPattern = compile("тел\\.: <a href=\"tel:\\d{10}");
        photoPattern = compile("var photos = \\[((?:\\d\\d?(?:, )?)+?)];");
        enginePattern = compile("Двиг\\.:.{32}>(\\d\\.\\d)</span>\\s(.{6,10})\\s\\(.{30}\">(.{7,12})</.{6}</li>");
        conditionPattern = compile("Состояние:\\s<span class=\"rst-ocb-i-d-l-i-s\">(.+?)</span>");
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
        long startTime;
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
                    Car car = getCarFromHtml(carHtml);
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
                                addCarDetails(car);
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
                long delay = 5000 - (System.currentTimeMillis() - startTime);
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

    private void addCarDetails(Car car) {
        try {
            String carHtml = HtmlGetter.getURLSource("http://m.rst.ua/" + car.getLink());
            if (car.getDescription().equals("big")) {
                Matcher m = bigDescription.matcher(carHtml);
                if (m.find()) {
                    car.setDescription(m.group().substring(49, m.group().length() - 26));
                }
            }
            Matcher m2 = townPattern.matcher(carHtml);
            if (m2.find()) {
                car.setTown(m2.group(2) == null ? m2.group(4) : m2.group(2)/*.substring(2, m2.group().length() - 17)*/);
            }
            Matcher m3 = contactsPattern.matcher(carHtml);
            if (m3.find()) {
                String contacts = m3.group();
                Matcher name = namePattern.matcher(contacts);
                if (name.find()) {
                    car.setOwnerName(name.group().substring(8, name.group().length() - 9));
                }
                StringBuilder tels = new StringBuilder();
                Matcher tel = telPattern.matcher(contacts);
                int counter = 0;
                while (tel.find()) {
                    tels.append(tel.group(), 19, tel.group().length());
                    counter++;
                }
                String allTels = tels.toString();
                String[] phones = new String[counter];
                for (int i = 0; i < counter; i++) {
                    phones[i] = allTels.substring(10 * i, 10 * i + 10);
                }
                car.setContacts(phones);
            }
            Matcher photo = photoPattern.matcher(carHtml);
            if (photo.find()) {
                String[] src = photo.group(1).split(", ");
                car.setImages(src);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Car getCarFromHtml(String carHtml) {
        Car car = new Car();
        String link;
        Matcher m = linkToCarPage.matcher(carHtml);
        if (m.find()) {
            link = m.group();
            car.setLink(link);
            String[] name = link.split("/");
            car.setBrand(name[1]);
            car.setModel(name[2]);
        } else {
            return null;
        }
        Matcher m2 = idPattern.matcher(link);
        if (m2.find()) {
            String ids = m2.group();
            car.setId(Integer.parseInt(ids.substring(0, ids.length() - 5)));
        }
        car.setSoldOut(carHtml.contains("Уже ПРОДАНО"));
        car.setFreshDetected(carHtml.contains("rst-ocb-i-s-fresh"));
        car.setExchange(carHtml.contains("ocb-i-exchange"));
        Matcher m3 = pricePattern.matcher(carHtml);
        if (m3.find()) {
            char[] priceArray = m3.group().substring(13, m3.group().length() - 7).toCharArray();
            StringBuilder sb = new StringBuilder();
            for (char c : priceArray) {
                if (c > 47 && c < 58) {
                    sb.append(c);
                }
            }
            car.setPrice(Integer.parseInt(sb.toString()));
        }
        Matcher m4 = buildYearPattern.matcher(carHtml);
        if (m4.find()) {
            car.setBuildYear(Integer.parseInt(m4.group(1)));
        }
        Matcher m5 = descriptionPattern.matcher(carHtml);
        if (m5.find()) {
            String desc = m5.group(1);
            if (desc.length() == 120) {
                desc = "big";
            }
            car.setDescription(desc);
        }
        Matcher m6 = datePattern.matcher(carHtml);
        if (m6.find()) {
            if (m6.group().contains("сегодня")) {
                car.setDetectedDate(CalendarHelper.getTodayDateString() + " " + m6.group(1));
            } else if (m6.group().contains("вчера")) {
                car.setDetectedDate(CalendarHelper.getYesterdayDateString() + " " + m6.group(1));
            } else {
                car.setDetectedDate(m6.group(1) + " 00:00");
            }
        }
        Matcher m7 = regionPattern.matcher(carHtml);
        if (m7.find()) {
            car.setRegion(m7.group(1));
        }
        Matcher m8 = enginePattern.matcher(carHtml);
        if (m8.find()) {
            car.setEngine(m8.group(1) + "-" + m8.group(2) + "-" + m8.group(3));
        }
        Matcher m9 = conditionPattern.matcher(carHtml);
        if (m9.find()) {
            car.setCondition(m9.group(1));
        }
        return car;
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
                car.getBrand(), car.getModel(), car.getId(), car.getRegion(), car.getTown(), car.isSoldOut(), car.isFreshDetected(), car.getPrice(),
                car.getBuildYear(), car.getDetectedDate(), car.getEngine(), car.getDescription());
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

    private static class CalendarHelper {
        private static DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
        private static DateFormat fullDateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

        private static Date yesterday() {
            final Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DATE, -1);
            return cal.getTime();
        }

        private static String getYesterdayDateString() {
            return dateFormat.format(yesterday());
        }

        private static String getTodayDateString() {
            return dateFormat.format(new Date());
        }

        private static String getTimeStamp() {
            return fullDateFormat.format(new Date());
        }
    }
}
