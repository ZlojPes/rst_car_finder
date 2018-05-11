package rst;

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
    private int[] images;
    private boolean isSoldOut;
    private boolean freshDetected;
    private boolean exchange;

    Car(int id, String brand, String model, String engine, String region, String link, int price, boolean exchange, int buildYear, String detectedDate, String description, boolean isSoldOut, boolean freshDetected) {
        this.id = id;
        this.brand = brand;
        this.model = model;
        this.region = region;
        this.link = link;
        this.price = price;
        this.buildYear = buildYear;
        this.detectedDate = detectedDate;
        this.description = description;
        this.isSoldOut = isSoldOut;
        this.freshDetected = freshDetected;
        this.exchange = exchange;
        this.engine = engine;
    }

    Car(){}


    int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    int getBuildYear() {
        return buildYear;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public void setEngine(String engine) {
        this.engine = engine;
    }

    public void setBuildYear(int buildYear) {
        this.buildYear = buildYear;
    }

    public void setDetectedDate(String detectedDate) {
        this.detectedDate = detectedDate;
    }

    public void setFreshDetected(boolean freshDetected) {
        this.freshDetected = freshDetected;
    }

    public void setExchange(boolean exchange) {
        this.exchange = exchange;
    }

    int getId() {
        return id;
    }

    public String getEngine() {
        return engine;
    }

    public boolean isExchange() {
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

    int[] getImages() {
        return images;
    }

    void setImages(int[] images) {
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

    public void setSoldOut(boolean soldOut) {
        isSoldOut = soldOut;
    }

    boolean freshDetected() {
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
