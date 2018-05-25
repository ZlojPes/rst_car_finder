package rst;

import sun.plugin2.message.Message;

import javax.swing.*;
import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.compile;

public class Explorer {
    static final String MAIN_PATH = new JFileChooser().getFileSystemView().getDefaultDirectory().toString() + "\\rstcars";
    private File mainDir = new File(MAIN_PATH);
    private String startUrl = "http://rst.ua/oldcars/daewoo/?price[]=101&price[]=2600&year[]=2003&year[]=0&condition=1&engine[]=0&engine[]=0&fuel=0&gear=0&drive=0&results=4&saled=0&notcust=&sort=1&city=0&region[]=23&region[]=24&region[2]=5&region[3]=8&region[4]=3&model[]=142&model[]=149&from=sform";
    private Map<Integer, Car> base = new HashMap<>();
    private Pattern prefixPattern;
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
    private Pattern photoPattrrn;
    private Pattern idFromFolder;
    private Pattern enginePattern;
    private Pattern conditionPattern;
    private Pattern mainPhotoPattern;

    // TODO E-Mail notifier;
    // TODO parsing/writing JSON data file;
    // TODO hourly check car for update;

    public static void main(String[] args) {
        Explorer explorer = new Explorer();
        explorer.initPatterns();
        explorer.go();
    }

    private void initPatterns() {
        prefixPattern = Pattern.compile("^\\D{4,13}=");
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
        photoPattrrn = compile("var photos = \\[(\\d\\d?(, )?)+?];");
        idFromFolder = compile("^\\d{7,}");
        enginePattern = compile("Двиг\\.:.{32}>(\\d\\.\\d)</span>\\s(.{6,10})\\s\\(.{30}\">(.{7,12})</.{6}</li>");
        mainPhotoPattern = compile("-i-i\".+?src=\"(.+?)\"><h3"); //no-photo.png
        conditionPattern = compile("Состояние:\\s<span class=\"rst-ocb-i-d-l-i-s\">(.+?)</span>");

    }

    private void go() {
        long start = System.currentTimeMillis();
        if (mainDir.exists()) {
            System.out.print("Reading base from disc");
            initBaseFromFile();
        } else {
            if (!mainDir.mkdir()) {
                System.out.println("Error happens during creating work directory!");
                System.exit(1);
            }
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
                                writeCarOnDisc(car, true);
                                base.put(id, car);
                                report(car);
                                if (!firstCycle) {
                                    Mail.sendCar(car);
                                }
                                if (car.getImages() != null) {
                                    imageGetter.downloadAllImages(car);
                                }
                            } //else ignore this car
                        } else {
                                checkCarForUpdates(car, !firstCycle);

//                            if (car.getImages() != null && (carFromBase == null || !carFromBase.getImages().containsAll(car.getImages()))) {
//                                imageGetter.downloadAbsentImages(carFromBase);
//                            }
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

    private void checkCarForUpdates(Car car, boolean sendEmail) {
        Car oldCar = base.get(car.getId());
        boolean hasChanges = false;
        if (oldCar.getPrice() != car.getPrice()) {
            hasChanges = true;
            String comment = "Цена изменена с " + oldCar.getPrice() + "$ на " + car.getPrice() + "$ " + CalendarHelper.getTimeStamp();
            oldCar.getComments().add(comment);
            oldCar.setPrice(car.getPrice());
            System.out.print(comment);
        }
        if (!car.getDescription().equals("big") && !car.getDescription().equals(oldCar.getDescription())) {
            hasChanges = true;
            String comment = "Старый комментарий: " + oldCar.getDescription() + CalendarHelper.getTimeStamp();
            oldCar.getComments().add(comment);
            oldCar.setDescription(car.getDescription());
            System.out.print("описание обновлено");
        }
        if (car.isSoldOut()) {
            hasChanges = true;
            oldCar.setSoldOut(true);
            base.remove(oldCar.getId());
            String comment = "Автомобиль продан! Время продажи: " + CalendarHelper.getTimeStamp();
            oldCar.getComments().add(comment);
            System.out.print("\nАвтомобиль продан!:" + car.getId());
        }
        if (hasChanges){
            writeCarOnDisc(oldCar, false);
            Mail.sendCar("Изменения в авто!",oldCar, "см. изменения в комментариях");
        }
    }

    private void report(Car car) {
        System.out.printf("%n%6s %-6s%-8dРегион:%-15sГород:%-11sПродано:%-6sСвежее:%-5s%5s$%5s%17s %-26s%s%n",
                car.getBrand(), car.getModel(), car.getId(), car.getRegion(), car.getTown(), car.isSoldOut(), car.isFreshDetected(), car.getPrice(),
                car.getBuildYear(), car.getDetectedDate(), car.getEngine(), car.getDescription());
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
            Matcher photo = photoPattrrn.matcher(carHtml);
            if (photo.find()) {
                String[] src = photo.group().substring(14, photo.group().length() - 2).split(", ");
                car.setImages(getImageSet(src));
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
        report(car);
        return car;
    }

    private void initBaseFromFile() {
        String[] folders = mainDir.list();
        if (folders != null) {
            nextFolder:
            for (String folder : folders) {
                Car car = new Car();
                Matcher m = idFromFolder.matcher(folder);
                if (m.find()) {
                    car.setId(Integer.parseInt(m.group()));
                } else {
                    continue;
                }
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(
                        MAIN_PATH + "\\" + folder + "\\" + "data.txt"), "Cp1251"))) {
                    String line, prefix = "", value;
                    while ((line = reader.readLine()) != null) {
                        if ((value = getValue(line)) == null) {
                            continue;
                        }
                        Matcher pr = prefixPattern.matcher(line);
                        if (pr.find()) {
                            prefix = pr.group().substring(0, pr.group().length() - 1);
                        }
                        switch (prefix) {
                            case ("isSoldOut"):
                                if (Boolean.valueOf(value)) {
                                    continue nextFolder;
                                }
                            case ("brand"):
                                car.setBrand(value);
                                break;
                            case ("model"):
                                car.setModel(value);
                                break;
                            case ("condition"):
                                car.setCondition(value);
                                break;
                            case ("engine"):
                                car.setEngine(value);
                                break;
                            case ("buildYear"):
                                car.setBuildYear(Integer.parseInt(value));
                                break;
                            case ("price"):
                                car.setPrice(Integer.parseInt(value));
                                break;
                            case ("exchange"):
                                car.setExchange(Boolean.valueOf(value));
                                break;
                            case ("region"):
                                car.setRegion(value);
                                break;
                            case ("town"):
                                car.setTown(value);
                                break;
                            case ("name"):
                                car.setOwnerName(value);
                                break;
                            case ("contacts"):
                                car.setContacts(value.split(", "));
                                break;
                            case ("description"):
                                car.setDescription(value);
                                break;
                            case ("isFreshDetected"):
                                car.setFreshDetected(Boolean.valueOf(value));
                                break;
                            case ("date"):
                                car.setDetectedDate(value);
                                break;
                            case ("images"):
                                if (value.equals("null")) {
                                    break;
                                }
                                String[] sub = value.substring(1, value.length() - 1).split(", ");
                                car.setImages(getImageSet(sub));
                                break;
                            case ("link"):
                                car.setLink(value);
                                break;
                            case ("comment"):
                                car.getComments().add(value);
                                break;
                        }
                    }
                    System.out.print(".");
                    base.put(car.getId(), car);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private Set<Integer> getImageSet(String[] input) {
        Set<Integer> images = new LinkedHashSet<>();
        for (String s : input) {
            images.add(Integer.parseInt(s));
        }
        return images;
    }

    private static String getValue(String line) {
        Pattern value = compile("\".+\"$");
        Matcher m = value.matcher(line);
        if (m.find()) {
            String result = m.group();
            return result.substring(1, result.length() - 1);
        }
        return null;
    }

    private void writeCarOnDisc(Car car, boolean createFolder) {
        String path = MAIN_PATH + "\\" + car.getId() + "_" + car.getBrand() + "_" + car.getModel();
        if (!createFolder || new File(path).mkdir()) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(new File(path + "\\data.txt")))) {
                writer.write("isSoldOut=\"" + car.isSoldOut());
                writer.newLine();
                writer.write("brand=\"" + car.getBrand() + "\"");
                writer.newLine();
                writer.write("model=\"" + car.getModel() + "\"");
                writer.newLine();
                writer.write("condition=\"" + car.getCondition() + "\"");
                writer.newLine();
                writer.write("engine=\"" + car.getEngine() + "\"");
                writer.newLine();
                writer.write("buildYear=\"" + car.getBuildYear() + "\"");
                writer.newLine();
                writer.write("price=\"" + car.getPrice() + "\"");
                writer.newLine();
                writer.write("exchange=\"" + car.isExchange() + "\"");
                writer.newLine();
                writer.write("region=\"" + car.getRegion() + "\"");
                writer.newLine();
                writer.write("town=\"" + car.getTown() + "\"");
                writer.newLine();
                writer.write("name=\"" + car.getOwnerName() + "\"");
                writer.newLine();
                writer.write("contacts=\"" + String.join(", ", car.getContacts()) + "\"");
                writer.newLine();
                writer.write("description=\"" + car.getDescription() + "\"");
                writer.newLine();
                writer.write("isFreshDetected=\"" + car.isFreshDetected() + "\"");
                writer.newLine();
                writer.write("date=\"" + car.getDetectedDate() + "\"");
                writer.newLine();
                writer.write("images=\"" + (car.getImages() == null ? "null" : Arrays.deepToString(car.getImages().toArray())) + "\"");
                writer.newLine();
                writer.write("link=\"" + car.getLink() + "\"");
                for (String comment : car.getComments()) {
                    writer.newLine();
                    writer.write("comment=\"" + comment + "\"");

                }
                writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static class CalendarHelper {
        private static DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
        private static DateFormat fullDateFormat = new SimpleDateFormat("dd.MM.yyyy hh-mm-ss");

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
