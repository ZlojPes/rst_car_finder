package rst;

import java.util.*;
import javax.mail.*;
import javax.mail.internet.*;
import javax.mail.internet.MimeMessage.RecipientType;
import javax.naming.*;
import javax.naming.directory.*;

public class DirectMail {
    public static void sendMessageByEmail(Car car) {
        System.out.println("Sending E-Mail...");
        String subject = "Обнаружено новое авто!";
        String message = "Марка: " + car.getBrand() + "\nМодель: " + car.getModel() + "\nЦена: " + car.getPrice() + "\nГод: " +
                car.getBuildYear() + "Свежее:" + (car.freshDetected() ? "да" : "нет") + "\nДвигатель: " + car.getEngine() +
                "\nРегион: " + car.getRegion() + "\nГород: " + car.getTown() + "\nописание: " + car.getDescription() +
                "\nhttp://rst.ua/" + car.getLink();
        try {
            sendEmail(subject, message);
            System.out.println("Sent successful!");
        } catch (MessagingException | NamingException e) {
            System.out.println("E-Mail sending failed!");
            e.printStackTrace();
        }
    }

    public static void sendMessageByEmail(String message) {
        System.out.println("Sending E-Mail...");
        String subject = "Ошибка открытия URL!";
        try {
            sendEmail(subject, message);
            System.out.println("Sent successful!");
        } catch (MessagingException | NamingException e) {
            System.out.println("E-Mail sending failed!");
            e.printStackTrace();
        }
    }

    public synchronized static void sendEmail(String subject, String message) throws MessagingException, NamingException {
        String[] mx = getMX("gmail.com");
//        for (String mxx : mx) {
//            System.out.println("MX: " + mxx);
//        }
        Properties props = new Properties();
        props.setProperty("mail.smtp.host", mx[0]);
        props.setProperty("mail.debug", "true");
        Session session = Session.getInstance(props);
        MimeMessage mimeMessage = new MimeMessage(session);
        mimeMessage.setFrom(new InternetAddress("asp4ever@gmail.com"));
        mimeMessage.addRecipient(RecipientType.TO, new InternetAddress("asp4rever@gmail.com"));
        mimeMessage.setSubject(subject);
        mimeMessage.setText(message);
        Transport.send(mimeMessage);
    }

    private static String[] getMX(String domainName) throws NamingException {
        Hashtable<String, Object> env = new Hashtable<>();

        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.dns.DnsContextFactory");
        env.put(Context.PROVIDER_URL, "dns:");

        DirContext ctx = new InitialDirContext(env);
        Attributes attribute = ctx.getAttributes(domainName, new String[]{"MX"});
        Attribute attributeMX = attribute.get("MX");
        // if there are no MX RRs then default to domainName (see: RFC 974)
        if (attributeMX == null) {
            return (new String[]{domainName});
        }

        // split MX RRs into Preference Values(pvhn[0]) and Host Names(pvhn[1])
        String[][] pvhn = new String[attributeMX.size()][2];
        for (int i = 0; i < attributeMX.size(); i++) {
            pvhn[i] = ("" + attributeMX.get(i)).split("\\s+");
        }

        // sort the MX RRs by RR value (lower is preferred)
        Arrays.sort(pvhn, Comparator.comparingInt(o -> Integer.parseInt(o[0])));

        String[] sortedHostNames = new String[pvhn.length];
        for (int i = 0; i < pvhn.length; i++) {
            sortedHostNames[i] = pvhn[i][1].endsWith(".") ?
                    pvhn[i][1].substring(0, pvhn[i][1].length() - 1) : pvhn[i][1];
        }
        return sortedHostNames;
    }
}