package rst;

import javax.swing.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.compile;

class DiscManager {
    private static final String mainPath;

    static {
        String path = Explorer.getProp().getProperty("work_directory");
        if (!path.equals("")) {
            mainPath = path;
        } else {
            System.out.println("Work directory has been set to C:\\%user%\\Documents\\rst");
            mainPath = new JFileChooser().getFileSystemView().getDefaultDirectory().toString() + "\\rst";
        }
    }

    private File mainDir = new File(mainPath);
    private Pattern prefixPattern = Pattern.compile("^\\D{4,15}=");


    boolean initBaseFromDisc(Map<Integer, Car> base) {
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
                        mainPath + "\\" + folder + "\\" + "data.txt"), "UTF-8"))) {
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
                                car.getComments().add(value);
                                break;
                        }
                    }
                    System.out.print(".");
                    base.put(car.getId(), car);
                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.println("Error happens during base initialisation!");
                    System.exit(1);
                }
            }
        }
        return true;
    }

    void writeCarOnDisc(Car car, boolean createFolder) {
        String path = mainPath + "\\" + car.getId() + "_" + car.getBrand() + "_" + car.getModel();
        if (!createFolder || new File(path).mkdir()) {
            try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(path + "\\data.txt"), StandardCharsets.UTF_8))) {
                writer.println("isSoldOut=\"" + car.isSoldOut() + "\"");
                writer.println("brand=\"" + car.getBrand() + "\"");
                writer.println("model=\"" + car.getModel() + "\"");
                writer.println("condition=\"" + car.getCondition() + "\"");
                writer.println("engine=\"" + car.getEngine() + "\"");
                writer.println("buildYear=\"" + car.getBuildYear() + "\"");
                writer.println("price=\"" + car.getPrice() + "\"");
                writer.println("exchange=\"" + car.isExchange() + "\"");
                writer.println("region=\"" + car.getRegion() + "\"");
                writer.println("town=\"" + car.getTown() + "\"");
                writer.println("name=\"" + car.getOwnerName() + "\"");
                writer.println("contacts=\"" + String.join(", ", car.getPhones()) + "\"");
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
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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

    static String getMainPath() {
        return mainPath;
    }
}
