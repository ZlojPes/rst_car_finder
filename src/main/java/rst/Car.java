package rst;

import java.io.IOException;
import java.util.*;
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
    private Set<String> phones;
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
    private static final Pattern regionPattern;
    private static final Pattern regionDetailPattern;
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
        regionPattern = compile("Область:.+?>([А-Яа-я]+?)</span>");
        regionDetailPattern = compile(">(?:([А-Яа-я]+)</a></span>Область)|(?:>Область<.+?>([А-Яа-я]+)</a>)");
        bigDescription = compile("desc rst-uix-block-more\">(?:\\s*<p><strong>Возможен обмен.</strong> </p>)?\\s*(.+?)\\s*</div>");
        townPattern = compile("(\">(\\D+?)</a></span>Город<)|(Город</td>.+?title=\".*?авто.*?\">(\\D+?)</a>)");
        contactsPattern = compile("<h3>Контакты:</h3.+?</div></div>");
        namePattern = compile("<strong>(.+)</strong>");
        telPattern = compile("тел\\.: <a href=\"tel:(\\d{10})");
        photoPattern = compile("var photos = \\[((?:\\d\\d?(?:, )?)+?)];");
        enginePattern = compile("Двиг\\.:.{32}>(\\d\\.\\d)</span>\\s(.{6,10})\\s\\(.{30}\">(.{7,12})</.{6}</li>");
        conditionPattern = compile("Состояние:\\s<span class=\"rst-ocb-i-d-l-i-s\">(.+?)</span>");
    }

    Car() {
        comments = new LinkedList<>();
        phones = new HashSet<>();
        images = new LinkedHashSet<>();
    }

    //Старое описание: <p><strong>Возможен обмен.</strong> </p>Требуется покраски машина люкс 31.05.2018 15:26:37 - 8314631

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
            if (!s.equals("")) {
                try {
                    images.add(Integer.parseInt(s));
                } catch (NumberFormatException e) {
                    System.out.println("Images data is corrupt in directory " + id + ". Please delete and reload it!");
                }
            }
        }
    }

    String[] getPhones() {
        Iterator<String> iterator = phones.iterator();
        String[] out = new String[phones.size()];
        int counter = 0;
        while (iterator.hasNext()) {
            out[counter] = iterator.next();
        }
        return out;
    }

    void setPhones(String[] ph) {
        phones.addAll(Arrays.asList(ph));
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

    void setSoldOut() {
        isSoldOut = true;
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
                car.detectedDate = CalendarUtil.getTodayDateString() + " " + m6.group(1);
            } else if (m6.group().contains("вчера")) {
                car.detectedDate = CalendarUtil.getYesterdayDateString() + " " + m6.group(1);
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
            addDetails(HtmlGetter.getURLSource("http://m.rst.ua/" + link));
        } catch (IOException e) {
            System.out.println("Cannot add detail to car " + id + ". Error:" + e.getMessage());
        }
    }

    private boolean addDetails(String src) {
        boolean carWasChanged = false;
        Matcher m = bigDescription.matcher(src);
        if (m.find() && !m.group(1).equals(description)) {
            description = m.group(1);
            carWasChanged = true;
        }
        Matcher m2 = townPattern.matcher(src);
        if (m2.find()) {
            town = m2.group(2) == null ? m2.group(4) : m2.group(2);
            carWasChanged = true;
        }
        if (region == null || region.equals("null")) {
            Matcher m22 = regionDetailPattern.matcher(src);
            if (m22.find()) {
                region = m22.group(1) == null ? m22.group(2) : m22.group(1);
            }
        }
        Matcher m3 = contactsPattern.matcher(src);
        if (m3.find()) {
            String contacts = m3.group();
            Matcher name = namePattern.matcher(contacts);
            if (name.find() && !name.group(1).equals(ownerName)) {
                ownerName = name.group(1);
                carWasChanged = true;
            }
            Matcher tel = telPattern.matcher(contacts);
            while (tel.find()) {
                if(phones.add(tel.group(1))) {
                    carWasChanged = true;
                }
            }
        }
        Matcher photo = photoPattern.matcher(src);
        if (photo.find()) {
            String[] ar = photo.group(1).split(", ");
            if (ar.length > 0) {
                ArrayList<Integer> list = new ArrayList<>();
                for (String s : ar) {
                    int i = Integer.parseInt(s);
                    if (!images.contains(i)) {
                        list.add(i);
                        images.add(i);
                    }
                }
                if (list.size() > 0) {
                    new ImageGetter().downloadImages(this, list);
                    carWasChanged = true;
                }
            }
        }
        return carWasChanged;
    }

    static void deepCheck(Map<Integer, Car> base, DiscManager discManager) {
        Set<Map.Entry<Integer, Car>> entrySet = base.entrySet();
        Iterator<Map.Entry<Integer, Car>> iterator = entrySet.iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer, Car> entry = iterator.next();
            Car car = entry.getValue();
            boolean carWasChanged;
            try {
                String src = HtmlGetter.getURLSource("http://m.rst.ua/" + car.getLink());
                if (!src.contains(String.valueOf(car.getId())) && src.contains("<p>При использовании материалов, ссылка на RST обязательна.</p>")) {
                    car.setSoldOut();
                    String comment = "Объявление удалено! Время отметки: " + CalendarUtil.getTimeStamp();
                    car.getComments().add(comment);
                    System.out.println("\n(" + car.getId() + ")Объявление удалено!");
                    iterator.remove();
                    carWasChanged = true;
                } else {
                    carWasChanged = car.addDetails(src);
                }
                if (carWasChanged) {
                    discManager.writeCarOnDisc(car, false);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
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
