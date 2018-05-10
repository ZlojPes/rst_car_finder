package rst;

public class Car implements Comparable<Car> {
    private int id;
    private String brand;
    private String model;
    private String link;
    private String ownerName;
    private String region;
    private String town;
    private int price;
    private int buildYear;
    private String detectedDate;
    private String description;
    private String[] contacts;
    private int[] images;
    private boolean isSoldOut;
    private boolean wasFreshAdded;

    Car(int id, String brand, String model, String region, String link, int price, int buildYear, String detectedDate, String description, boolean isSoldOut, boolean wasFreshAdded) {
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
        this.wasFreshAdded = wasFreshAdded;
    }


    int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    int getBuildYear() {
        return buildYear;
    }

    int getId() {
        return id;
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

    boolean isWasFreshAdded() {
        return wasFreshAdded;
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
