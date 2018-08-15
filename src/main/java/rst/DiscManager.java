package rst;

import javax.swing.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.compile;

class DiscManager {
    private static final String mainPath;
    private static File mainDir;
    private static Pattern prefixPattern;

    static {
        String def = new JFileChooser().getFileSystemView().getDefaultDirectory().toString() + "/rst";
        mainPath = Explorer.getProp().getProperty("work_directory", def);
        mainDir = new File(mainPath);
        prefixPattern = Pattern.compile("([A-Za-z]{4,15})=");
    }

    static boolean initBaseFromDisc(Map<Integer, Car> base) {
        if (mainDir.exists()) {
            System.out.print("Reading base from disc (" + mainPath + ")");
        } else {
            System.out.print("Creating work directory...");
            if (!mainDir.mkdir()) {
                System.out.println("Error happens during creating work directory!");
                System.exit(1);
            }
            return false;
        }
        Pattern idFromFolder = compile("^\\d{7,}");
        String[] folders = mainDir.list();
        if (folders != null) {
            for (String folder : folders) {
                Car car = new Car();
                Matcher m = idFromFolder.matcher(folder);
                if (m.find()) {
                    car.setId(Integer.parseInt(m.group()));
                } else {
                    continue;
                }
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(
                        mainPath + "/" + folder + "/data.txt"), StandardCharsets.UTF_8))) {
                    String line, value, prefix = "";
                    while ((line = reader.readLine()) != null) {
                        if ((value = getValue(line)) == null) {
                            continue;
                        }
                        Matcher pref = prefixPattern.matcher(line);
                        if (pref.find()) {
                            prefix = pref.group(1);
                        }
                        switch (prefix) {
                            case ("isSoldOut"):
                                if (Boolean.valueOf(value)) {
                                    car.setSoldOut();
                                }
                                break;
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
                            case ("mileage"):
                                try {
                                    car.setMileage(Integer.parseInt(value));
                                } catch (NumberFormatException e) {
                                    car.setMileage(0);
                                }
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
                                car.setPhones(value.split(", "));
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
                                car.setImages(sub);
                                break;
                            case ("link"):
                                car.setLink(value);
                                break;
                            case ("comment"):
                                car.addComment(value);
                                break;
                        }
                    }
                    System.out.print(".");
                    Seller.addNewSeller(car);
                    if (!car.isSoldOut()) {
                        base.put(car.getId(), car);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.println("Error happens during base initialisation!");
                    System.exit(1);
                }
            }
        }
        return true;
    }

    static void writeCarOnDisc(Car car, boolean createFolder) {
        String path = mainPath + "/" + car.getId() + "_" + car.getBrand() + "_" + car.getModel();
        if (!createFolder || new File(path).mkdir()) {
            try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(path + "/data.txt"), StandardCharsets.UTF_8))) {
                writer.println("isSoldOut=\"" + car.isSoldOut() + "\"");
                writer.println("brand=\"" + car.getBrand() + "\"");
                writer.println("model=\"" + car.getModel() + "\"");
                writer.println("condition=\"" + car.getCondition() + "\"");
                writer.println("engine=\"" + car.getEngine() + "\"");
                writer.println("buildYear=\"" + car.getBuildYear() + "\"");
                writer.println("price=\"" + car.getPrice() + "\"");
                writer.println("mileage=\"" + car.getMileage() + "\"");
                writer.println("exchange=\"" + car.isExchange() + "\"");
                writer.println("region=\"" + car.getRegion() + "\"");
                writer.println("town=\"" + car.getTown() + "\"");
                writer.println("name=\"" + car.getOwnerName() + "\"");
                writer.println("contacts=\"" + String.join(", ", car.getPhonesArray()) + "\"");
                writer.println("description=\"" + car.getDescription() + "\"");
                writer.println("isFreshDetected=\"" + car.isFreshDetected() + "\"");
                writer.println("date=\"" + car.getDetectedDate() + "\"");
                writer.println("images=\"" + (car.getImages() == null ? "null" : Arrays.deepToString(car.getImages().toArray())) + "\"");
                writer.print("link=\"" + car.getLink() + "\"");
                for (String comment : car.getComments()) {
                    writer.println();
                    writer.print("comment=\"" + comment + "\"");
                }
                writer.flush();
                System.out.print("'w'");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    static void writeSellersBase() {
        List<Seller> sellersBase = Seller.getSellersBase();
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(mainPath + "/sellers.txt"), StandardCharsets.UTF_8))) {
            for (Seller seller : sellersBase) {
                if (seller.getPhones().length == 0) {
                    continue;
                }
                writer.println("phones=\"" + String.join(", ", seller.getPhones()) + "\"");
                writer.println("names=\"" + String.join(", ", seller.getNames()) + "\"");
                for (String link : seller.getLinks()) {
                    writer.println("link=\"" + link + "\"");
                }
                writer.println("********************");
            }
            writer.flush();
            System.out.print("'ws'");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void readSellersBase() {
        if (!new File(mainDir + "/sellers.txt").exists()) {
            System.out.println("There's no sellers base!");
            return;
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(
                mainPath + "/sellers.txt"), StandardCharsets.UTF_8))) {
            String line, value, prefix = "";
            Seller seller = new Seller();
            while ((line = reader.readLine()) != null) {
                value = getValue(line);
//                if ((value = getValue(line)) == null) {
//                    return;
//                }
                if (line.equals("********************")) {
                    Seller.addNewSeller(seller);
                    seller = new Seller();
                }
                if (value == null) {
                    continue;
                }
                Matcher pref = prefixPattern.matcher(line);
                if (pref.find()) {
                    prefix = pref.group(1);
                }
                switch (prefix) {
                    case ("phones"):
                        seller.setPhones(value.split(", "));
                        break;
                    case ("names"):
                        seller.setNames(value.split(", "));
                        break;
                    case ("link"):
                        seller.setLink(value);
                        break;
                }
            }
        } catch (IOException e) {
            System.out.println("Error happens during sellers base initialisation!");
        }
    }

    private static String getValue(String line) {
        Pattern value = compile("\"(.+)\"$");
        Matcher m = value.matcher(line);
        if (m.find()) {
            return m.group(1);
        }
        return null;
    }

    static String getMainPath() {
        return mainPath;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    static void discardCarFolder(Car car) {
        String path = mainPath + "/" + car.getId() + "_" + car.getBrand() + "_" + car.getModel();
        File file = new File(path);
        if (file.exists()) {
            new Thread(() -> {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                file.renameTo(new File(path + "_x"));
            }).start();
        }
    }
}
