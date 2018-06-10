package rst;

import java.util.*;

class Seller {
    private static List<Seller> sellersBase;
    private Set<String> phones;
    private Set<String> names;
    private Set<String> links;

    static {
        sellersBase = new ArrayList<>();
        DiscManager.readSellersBase(sellersBase);
    }

    Seller() {
        phones = new HashSet<>();
        names = new HashSet<>();
        links = new HashSet<>();
    }

    static boolean isUniqueSeller(Car car) {
        boolean out = true;
        Seller outputSeller = null;
        exit:
        for (Seller seller : sellersBase) {
            for (String phone : car.getPhones()) {
                if (seller.phones.contains(phone)) {
                    outputSeller = seller;
                    out = false;
                    break exit;
                }
            }
        }
        if (outputSeller == null) {
            outputSeller = new Seller();
            sellersBase.add(outputSeller);
        }
        outputSeller.phones.addAll(Arrays.asList(car.getPhones()));
        outputSeller.names.add(car.getOwnerName());
        outputSeller.links.add("http://rst.ua/" + car.getLink());
        return out;
    }

    void setPhones(String[] phones) {
        this.phones.addAll(Arrays.asList(phones));
    }

    String[] getPhones() {
        return getArray(phones);
    }

    void setNames(String[] names) {
        this.names.addAll(Arrays.asList(names));
    }

    String[] getNames() {
        return getArray(names);
    }

    private String[] getArray(Set<String> set) {
        String[] out = new String[set.size()];
        Iterator<String> iterator = set.iterator();
        int counter = 0;
        while (iterator.hasNext()) {
            out[counter] = iterator.next();
            counter++;
        }
        return out;
    }

    void setLink(String link) {
        links.add(link);
    }

    Set<String> getLinks() {
        return links;
    }

    public static List<Seller> getSellersBase() {
        return sellersBase;
    }
}
