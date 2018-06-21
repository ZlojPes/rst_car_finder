package rst;

import java.util.*;

class Seller {
    private static List<Seller> sellersBase;
    private Set<String> phones;
    private Set<String> names;
    private Set<String> links;

    static {
        sellersBase = new ArrayList<>();
        DiscManager.readSellersBase();
    }

    Seller() {
        phones = new HashSet<>();
        names = new HashSet<>();
        links = new HashSet<>();
    }

    static void addNewSeller(Seller seller) {
        addSellerToBase(seller.phones, seller.names, seller.links);
    }

    static void addNewSeller(Car car) {
        Set<String> phones = new HashSet<>(Arrays.asList(car.getPhonesArray()));
        Set<String> names = new HashSet<>();
        names.add(car.getOwnerName());
        Set<String> links = new HashSet<>();
        links.add("http://rst.ua/" + car.getLink());
        addSellerToBase(phones, names, links);
    }

    private static void addSellerToBase(Set<String> phones, Set<String> names, Set<String> links) {
        Seller outputSeller = findSeller(phones);
        if (outputSeller == null) {
            outputSeller = new Seller();
            sellersBase.add(outputSeller);
        }
        outputSeller.phones.addAll(phones);
        outputSeller.names.addAll(names);
        outputSeller.links.addAll(links);
    }

    static int getBaseHash() {
        int hash = sellersBase.size();
        for (Seller seller : sellersBase) {
            hash += seller.names.size();
            hash += seller.phones.size();
            hash += seller.links.size();
        }
        return hash;
    }

    static boolean isUniqueSeller(Set<String> phones) {
        Seller seller = findSeller(phones);
        return seller != null && seller.links.size() == 1;
    }

    private static Seller findSeller(Set<String> phones) {
        for (Seller seller : sellersBase) {
            for (String phone : phones) {
                if (seller.phones.contains(phone)) {
                    return seller;
                }
            }
        }
        return null;
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

    static List<Seller> getSellersBase() {
        return sellersBase;
    }
}
