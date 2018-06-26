package rst;

import java.io.IOException;
import java.text.ParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.compile;

class Car {
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
    private String description;

    private Set<String> phones;
    private Set<Integer> images;
    private boolean isSoldOut;
    private boolean freshDetected;
    private boolean exchange;
    private String condition;
    private List<String> comments;
    private Date firstTimeDetected;
    private int mileage;

    private static final Pattern DATE_PATTERN;
    private static final Pattern ID_PATTERN;
    private static final Pattern PRICE_PATTERN;
    private static final Pattern BUILD_YEAR_PATTERN;
    private static final Pattern DESCRIPTION_PATTERN;
    private static final Pattern LINK_TO_CAR_PAGE;
    private static final Pattern REGION_PATTERN;
    private static final Pattern REGION_DETAIL_PATTERN;
    private static final Pattern BIG_DESCRIPTION;
    private static final Pattern TOWN_PATTERN;
    private static final Pattern CONTACTS_PATTERN;
    private static final Pattern NAME_PATTERN;
    private static final Pattern TEL_PATTERN;
    private static final Pattern PHOTO_PATTERN;
    private static final Pattern ENGINE_PATTERN;
    private static final Pattern CONDITION_PATTERN;
    private static final Pattern MILEAGE_PATTERN;

    static {
        DATE_PATTERN = compile("(?:размещено|обновлено).+?((?:\\d{2}\\.){0,2}\\d{2}:?\\d{2})</div>");
        ID_PATTERN = compile("(\\d{7,})\\.html$");
        PRICE_PATTERN = compile("данные НБУ\">\\$(\\d{0,3}'?\\d{3})</span>");
        BUILD_YEAR_PATTERN = compile("d-l-i-s\">(\\d{4})");
        DESCRIPTION_PATTERN = compile("-d-d\">(.*?)</div>");
        LINK_TO_CAR_PAGE = compile("oldcars/.+\\d+\\.html");
        REGION_PATTERN = compile("Область:.+?>([А-Яа-я]+?)</span>");
        REGION_DETAIL_PATTERN = compile(">(?:([А-Яа-я]+)</a></span>Область)|(?:>Область<.+?>([А-Яа-я]+)</a>)");
        BIG_DESCRIPTION = compile("desc rst-uix-block-more\">(?:\\s*<p><strong>Возможен обмен.</strong> </p>)?\\s*(.+?)\\s*</div>");
        TOWN_PATTERN = compile("(\">(\\D+?)</a></span>Город<)|(Город</td>.+?title=\".*?авто.*?\">(\\D+?)</a>)");
        CONTACTS_PATTERN = compile("<h3>Контакты:</h3.+?</div></div>");
        NAME_PATTERN = compile("<strong>(.+)</strong>");
        TEL_PATTERN = compile("тел\\.: <a href=\"tel:(\\d{10})");
        PHOTO_PATTERN = compile("var photos = \\[((?:\\d\\d?(?:, )?)+?)];");
        ENGINE_PATTERN = compile("Двиг\\.:.{32}>(\\d\\.\\d)</span>\\s(.{6,10})\\s\\(.{30}\">(.{7,12})</.{6}</li>");
        CONDITION_PATTERN = compile("Состояние:\\s<span class=\"rst-ocb-i-d-l-i-s\">(.+?)</span>");
        MILEAGE_PATTERN = compile(">\\d{4}</span>,\\s\\((\\d+)\\s-\\sпробег\\)");
    }

    Car() {
        comments = new LinkedList<>();
        phones = new HashSet<>();
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

    int getMileage() {
        return mileage;
    }

    Set<String> getPhones() {
        return phones;
    }

    void setMileage(int mileage) {
        this.mileage = mileage;
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
        try {
            firstTimeDetected = CalendarUtil.fullDateFormat.parse(detectedDate);
        } catch (ParseException e) {
            System.out.println("Could not recognize the date");
        }
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

    void addComment(String comment) {
        comments.add(comment);
    }

    String[] getPhonesArray() {
        Iterator<String> iterator = phones.iterator();
        String[] out = new String[phones.size()];
        int counter = 0;
        while (iterator.hasNext()) {
            out[counter] = iterator.next();
            counter++;
        }
        return out;
    }

    void setPhones(String[] ph) {
        phones.addAll(Arrays.asList(ph));
    }

    String getModel() {
        return model;
    }

    boolean isCarAlive() {
        return new Date().getTime() - firstTimeDetected.getTime() < 86400000L * Integer.parseInt(Explorer.getProp().getProperty("car_alive_days"));
    }

    String getDetectedDate() {
        return CalendarUtil.fullDateFormat.format(firstTimeDetected);
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
        DiscManager.discardCarFolder(this);
    }

    boolean isFreshDetected() {
        return freshDetected;
    }

    static Car getCarFromHtml(String carHtml) {
        Car car = new Car();
        String link;
        Matcher m = LINK_TO_CAR_PAGE.matcher(carHtml);
        if (m.find()) {
            link = m.group();
            car.link = link;
            String[] name = link.split("/");
            car.brand = name[1];
            car.model = name[2];
        } else {
            return null;
        }
        Matcher m2 = ID_PATTERN.matcher(link);
        if (m2.find()) {
            car.id = Integer.parseInt(m2.group(1));
        }
        car.isSoldOut = carHtml.contains("Уже ПРОДАНО");
        car.freshDetected = carHtml.contains("rst-ocb-i-s-fresh");
        car.exchange = carHtml.contains("ocb-i-exchange");
        Matcher m3 = PRICE_PATTERN.matcher(carHtml);
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
        Matcher m4 = BUILD_YEAR_PATTERN.matcher(carHtml);
        if (m4.find()) {
            car.buildYear = Integer.parseInt(m4.group(1));
        }
        Matcher m5 = DESCRIPTION_PATTERN.matcher(carHtml);
        if (m5.find()) {
            String desc = m5.group(1);
            if (desc.length() == 120) {
                desc = "big";
            }
            car.description = desc;
        }
        Matcher m6 = DATE_PATTERN.matcher(carHtml);
        if (m6.find()) {
            String out;
            if (m6.group().contains("сегодня")) {
                out = CalendarUtil.getTodayDateString() + " " + m6.group(1);
            } else if (m6.group().contains("вчера")) {
                out = CalendarUtil.getYesterdayDateString() + " " + m6.group(1);
            } else {
                out = m6.group(1) + " 00:00";
            }
            car.setDetectedDate(out);
        }
        Matcher m7 = REGION_PATTERN.matcher(carHtml);
        if (m7.find()) {
            car.region = m7.group(1);
        }
        Matcher m8 = ENGINE_PATTERN.matcher(carHtml);
        if (m8.find()) {
            car.engine = m8.group(1) + "-" + m8.group(2) + "-" + m8.group(3);
        }
        Matcher m9 = CONDITION_PATTERN.matcher(carHtml);
        if (m9.find()) {
            car.condition = m9.group(1);
        }
        Matcher m10 = MILEAGE_PATTERN.matcher(carHtml);
        if (m10.find()) {
            try {
                car.setMileage(Integer.parseInt(m10.group(1)));
            } catch (NumberFormatException e) {
                System.out.println("ME");// Mileage Error
            }
        }
        return car;
    }

    void addDetails() {
        try {
            addDetails(HtmlGetter.getURLSource("http://m.rst.ua/" + link), false);
        } catch (IOException e) {
            System.out.println("Cannot add detail to car " + id + ". Error:" + e.getMessage());
        }
    }

    boolean addDetails(String src, boolean writeCommentIfChanged) {
        boolean carWasChanged = false;
        Matcher m = BIG_DESCRIPTION.matcher(src);
        if (m.find() && !m.group(1).equals(description)) {
            if (writeCommentIfChanged) {
                addComment("Старое описание: " + description + " " + CalendarUtil.getTimeStamp());
            }
            description = m.group(1);
            carWasChanged = true;
        }
        if (town == null || town.equals("null")) {
            Matcher m2 = TOWN_PATTERN.matcher(src);
            if (m2.find()) {
                town = m2.group(2) == null ? m2.group(4) : m2.group(2);
                carWasChanged = true;
            }
        }
        if (region == null || region.equals("null")) {
            Matcher m22 = REGION_DETAIL_PATTERN.matcher(src);
            if (m22.find()) {
                region = m22.group(1) == null ? m22.group(2) : m22.group(1);
            }
        }
        Matcher m3 = CONTACTS_PATTERN.matcher(src);
        if (m3.find()) {
            String contacts = m3.group();
            Matcher name = NAME_PATTERN.matcher(contacts);
            if (name.find() && !name.group(1).equals(ownerName)) {
                if (writeCommentIfChanged) {
                    addComment("Старое имя продавца: " + ownerName + " " + CalendarUtil.getTimeStamp());
                }
                ownerName = name.group(1);
                carWasChanged = true;
            }
            Matcher tel = TEL_PATTERN.matcher(contacts);
            while (tel.find()) {
                if (phones.add(tel.group(1))) {
                    carWasChanged = true;
                    if (writeCommentIfChanged) {
                        addComment("Добавлен телефон: " + tel.group(1) + " " + CalendarUtil.getTimeStamp());
                    }
                }
            }
        }
        Matcher photo = PHOTO_PATTERN.matcher(src);
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
                    if (writeCommentIfChanged) {
                        addComment("Добавлены фото: " + list + " " + CalendarUtil.getTimeStamp());
                    }
                }
            }
        }
        return carWasChanged;
    }
}
