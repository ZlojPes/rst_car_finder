package rst;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.compile;

public class Car implements Comparable<Car> {
    private int id;
    private String brand;
    private String model;
    private String link;
    private String ownerName;
    private String region;
    private String town;
    private String engine;
    private int price;
    private int buildYear;
    private String detectedDate;
    private String description;
    private String[] contacts;
    private Set<Integer> images;
    private boolean isSoldOut;
    private boolean freshDetected;
    private boolean exchange;
    private String condition;
    private List<String> comments;

    private static final Pattern datePattern;
    private static final Pattern idPattern;
    private static final Pattern pricePattern;
    private static final Pattern buildYearPattern;
    private static final Pattern descriptionPattern;
    private static final Pattern linkToCarPage;
    private static final Pattern carHtmlBlock;
    private static final Pattern regionPattern;
    private static final Pattern bigDescription;
    private static final Pattern townPattern;
    private static final Pattern contactsPattern;
    private static final Pattern namePattern;
    private static final Pattern telPattern;
    private static final Pattern photoPattern;
    private static final Pattern enginePattern;
    private static final Pattern conditionPattern;

    static {
        datePattern = compile("(?:размещено|обновлено).+?((?:\\d{2}\\.){0,2}\\d{2}:?\\d{2})</div>");
        idPattern = compile("\\d{7,}\\.html$");
        pricePattern = compile("данные НБУ\">\\$(\\d{0,3}'?\\d{3})</span>");
        buildYearPattern = compile("d-l-i-s\">(\\d{4})");
        descriptionPattern = compile("-d-d\">(.*?)</div>");
        linkToCarPage = compile("oldcars/.+\\d+\\.html");
        carHtmlBlock = compile("<a class=\"rst-ocb-i-a\" href=\".*?\\d\\d</div></div>");
        regionPattern = compile("Область:.+?>([А-Яа-я]+?)</span>");
        bigDescription = compile("desc rst-uix-block-more\">.+?</div>");
        townPattern = compile("(\">(\\D+?)</a></span>Город<)|(Город</td>.+?title=\".*?авто.*?\">(\\D+?)</a>)");
        contactsPattern = compile("<h3>Контакты:</h3.+?</div></div>");
        namePattern = compile("<strong>.+</strong>");
        telPattern = compile("тел\\.: <a href=\"tel:(\\d{10})");
        photoPattern = compile("var photos = \\[((?:\\d\\d?(?:, )?)+?)];");
        enginePattern = compile("Двиг\\.:.{32}>(\\d\\.\\d)</span>\\s(.{6,10})\\s\\(.{30}\">(.{7,12})</.{6}</li>");
        conditionPattern = compile("Состояние:\\s<span class=\"rst-ocb-i-d-l-i-s\">(.+?)</span>");
    }
    Car() {
        comments = new LinkedList<>();
        images = new LinkedHashSet<>();
    }


    int getPrice() {
        return price;
    }

    void setPrice(int price) {
        this.price = price;
    }

    String getCondition() {
        return condition;
    }

    void setCondition(String condition) {
        this.condition = condition;
    }

    int getBuildYear() {
        return buildYear;
    }

    void setId(int id) {
        this.id = id;
    }

    void setBrand(String brand) {
        this.brand = brand;
    }

    void setModel(String model) {
        this.model = model;
    }

    void setLink(String link) {
        this.link = link;
    }

    void setRegion(String region) {
        this.region = region;
    }

    void setEngine(String engine) {
        this.engine = engine;
    }

    void setBuildYear(int buildYear) {
        this.buildYear = buildYear;
    }

    void setDetectedDate(String detectedDate) {
        this.detectedDate = detectedDate;
    }

    void setFreshDetected(boolean freshDetected) {
        this.freshDetected = freshDetected;
    }

    void setExchange(boolean exchange) {
        this.exchange = exchange;
    }

    List<String> getComments() {
        return comments;
    }

    int getId() {
        return id;
    }

    String getEngine() {
        return engine;
    }

    boolean isExchange() {
        return exchange;
    }

    String getTown() {
        return town;
    }

    void setTown(String town) {
        this.town = town;
    }

    String getRegion() {
        return region;
    }

    String getBrand() {
        return brand;
    }

    String getLink() {
        return link;
    }

    String getOwnerName() {
        return ownerName;
    }

    void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    Set<Integer> getImages() {
        return images;
    }

    void setImages(String[] input) {
        images = new LinkedHashSet<>();
        for (String s : input) {
            if(!s.equals("")){
                try{
                    images.add(Integer.parseInt(s));
                }catch (NumberFormatException e){
                    System.out.println("Images data is corrupt in directory " + id + ". Please delete and reload it!");
                }
            }
        }
    }

    String[] getContacts() {
        return contacts;
    }

    void setContacts(String[] contacts) {
        this.contacts = contacts;
    }

    String getModel() {
        return model;
    }

    String getDetectedDate() {
        return detectedDate;
    }

    String getDescription() {
        return description;
    }

    void setDescription(String description) {
        this.description = description;
    }

    boolean isSoldOut() {
        return isSoldOut;
    }

    void setSoldOut(boolean soldOut) {
        isSoldOut = soldOut;
    }

    boolean isFreshDetected() {
        return freshDetected;
    }

    static Car getCarFromHtml(String carHtml) {
        Car car = new Car();
        String link;
        Matcher m = linkToCarPage.matcher(carHtml);
        if (m.find()) {
            link = m.group();
            car.link = link;
            String[] name = link.split("/");
            car.brand = name[1];
            car.model = name[2];
        } else {
            return null;
        }
        Matcher m2 = idPattern.matcher(link);
        if (m2.find()) {
            String ids = m2.group();
            car.id = Integer.parseInt(ids.substring(0, ids.length() - 5));
        }
        car.isSoldOut = carHtml.contains("Уже ПРОДАНО");
        car.freshDetected = carHtml.contains("rst-ocb-i-s-fresh");
        car.exchange = carHtml.contains("ocb-i-exchange");
        Matcher m3 = pricePattern.matcher(carHtml);
        if (m3.find()) {
            char[] priceArray = m3.group(1).toCharArray();
            StringBuilder sb = new StringBuilder();
            for (char c : priceArray) {
                if (c > 47 && c < 58) {
                    sb.append(c);
                }
            }
            car.price = Integer.parseInt(sb.toString());
        }
        Matcher m4 = buildYearPattern.matcher(carHtml);
        if (m4.find()) {
            car.buildYear = Integer.parseInt(m4.group(1));
        }
        Matcher m5 = descriptionPattern.matcher(carHtml);
        if (m5.find()) {
            String desc = m5.group(1);
            if (desc.length() == 120) {
                desc = "big";
            }
            car.description = desc;
        }
        Matcher m6 = datePattern.matcher(carHtml);
        if (m6.find()) {
            if (m6.group().contains("сегодня")) {
                car.detectedDate = Explorer.CalendarHelper.getTodayDateString() + " " + m6.group(1);
            } else if (m6.group().contains("вчера")) {
                car.detectedDate = Explorer.CalendarHelper.getYesterdayDateString() + " " + m6.group(1);
            } else {
                car.detectedDate = m6.group(1) + " 00:00";
            }
        }
        Matcher m7 = regionPattern.matcher(carHtml);
        if (m7.find()) {
            car.region = m7.group(1);
        }
        Matcher m8 = enginePattern.matcher(carHtml);
        if (m8.find()) {
            car.engine = m8.group(1) + "-" + m8.group(2) + "-" + m8.group(3);
        }
        Matcher m9 = conditionPattern.matcher(carHtml);
        if (m9.find()) {
            car.condition = m9.group(1);
        }
        return car;
    }

    void addDetails() {
        try {
            String carHtml = HtmlGetter.getURLSource("http://m.rst.ua/" + link);
            if (description.equals("big")) {
                Matcher m = bigDescription.matcher(carHtml);
                if (m.find() && m.groupCount() > 0) {
                    description = m.group(1);
                }
            }
            Matcher m2 = townPattern.matcher(carHtml);
            if (m2.find()) {
                town = m2.group(2) == null ? m2.group(4) : m2.group(2);
            }
            Matcher m3 = contactsPattern.matcher(carHtml);
            if (m3.find()) {
                String contacts = m3.group();
                Matcher name = namePattern.matcher(contacts);
                if (name.find()) {
                    ownerName = name.group().substring(8, name.group().length() - 9);
                }
                StringBuilder tels = new StringBuilder();
                Matcher tel = telPattern.matcher(contacts);
                int counter = 0;
                while (tel.find()) {
                    tels.append(tel.group(1));
                    counter++;
                }
                String allTels = tels.toString();
                String[] phones = new String[counter];
                for (int i = 0; i < counter; i++) {
                    phones[i] = allTels.substring(10 * i, 10 * i + 10);
                }
                this.contacts = phones;
            }
            Matcher photo = photoPattern.matcher(carHtml);
            if (photo.find()) {
                String[] src = photo.group(1).split(", ");
                setImages(src);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int compareTo(Car o) {
        return Integer.compare(this.id, o.id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Car car = (Car) o;

        return id == car.id;
    }

    @Override
    public int hashCode() {
        return id;
    }
}
