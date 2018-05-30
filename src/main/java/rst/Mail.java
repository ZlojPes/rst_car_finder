package rst;

import java.util.Date;
import java.util.Properties;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class Mail {
    private static Properties prop = Explorer.getProp();

    static void sendCar(Car car) {
        sendCar(null, car, null);
    }

    static void sendCar(String subject, Car car, String message) {
        if (subject == null) {
            subject = "Обнаружено новое авто!";
        }
        StringBuilder body = new StringBuilder().append("Марка: ").append(car.getBrand()).append("\nМодель: ").append(car.getModel())
                .append("\nЦена: ").append(car.getPrice()).append("$\nГод: ").append(car.getBuildYear()).append("\nСвежее: ")
                .append(car.isFreshDetected() ? "да" : "нет").append("\nДвигатель: ").append(car.getEngine()).append("\nРегион: ")
                .append(car.getRegion()).append("\nГород: ").append(car.getTown()).append("\nОбмен: ").append(car.isExchange() ? "да" : "нет")
                .append("\nописание: ").append(car.getDescription()).append("\nИмя:").append(car.getOwnerName())
                .append(" - ").append(car.getContacts() != null ? car.getContacts()[0] : "").append("\nhttp://rst.ua/")
                .append(car.getLink()).append("\nКомментарии:");
        for (String comment : car.getComments()) {
            body.append("\n").append(comment);
        }
        body.append("\n").append(message);
        alarmByEmail(subject, body.toString());
    }

    static void alarmByEmail(String subject, String message) {
        final String fromEmail = prop.getProperty("from_email");
        final String password = prop.getProperty("password");
        final String toEmail = prop.getProperty("to_email");

        System.out.println("SSLEmail Start");
        Properties props = new Properties();
        props.put("mail.smtp.host", prop.getProperty("smtp_host")); //SMTP Host
        props.put("mail.smtp.socketFactory.port", prop.getProperty("ssl_port")); //SSL Port
        props.put("mail.smtp.socketFactory.class",
                "javax.net.ssl.SSLSocketFactory"); //SSL Factory Class
        props.put("mail.smtp.auth", "true"); //Enabling SMTP Authentication
        props.put("mail.smtp.port", prop.getProperty("smtp_port")); //SMTP Port

        Authenticator auth = new Authenticator() {
            //override the getPasswordAuthentication method
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(fromEmail, password);
            }
        };

        Session session = Session.getDefaultInstance(props, auth);
        System.out.println("Session created");
        sendEmail(session, toEmail, subject, message);
    }

    private static void sendEmail(Session session, String toEmail, String subject, String body) {
        try {
            MimeMessage msg = new MimeMessage(session);
            msg.addHeader("Content-type", "text/HTML; charset=UTF-8");
            msg.addHeader("format", "flowed");
            msg.addHeader("Content-Transfer-Encoding", "8bit");
            msg.setFrom(new InternetAddress("zloj_pes@ukr.net"));
            msg.setReplyTo(InternetAddress.parse("zloj_pes@ukr.net", false));
            msg.setSubject(subject, "UTF-8");
            msg.setText(body, "UTF-8");
            msg.setSentDate(new Date());
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail, false));
            System.out.println("Message is ready");
            Transport.send(msg);
            System.out.println("EMail Sent Successfully!!");
        } catch (Exception e) {
            System.out.println("EMail Sent failed!!");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        alarmByEmail("test", "Test sending message\nПроверка кириллицы");
    }
}
