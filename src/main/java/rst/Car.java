package rst;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

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

    Car(){
        comments = new LinkedList<>();
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

    void setComments(List<String> comments) {
        this.comments = comments;
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

    void setImages(Set<Integer> images) {
        this.images = images;
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
