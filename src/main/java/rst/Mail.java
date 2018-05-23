package rst;

import java.util.Date;
import java.util.Properties;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class Mail {
    static void sendCar(Car car) {
        String subject = "Обнаружено новое авто!";
        String message = "Марка: " + car.getBrand() + "\nМодель: " + car.getModel() + "\nЦена: " + car.getPrice() + "$\nГод: " +
                car.getBuildYear() + "\nСвежее: " + (car.freshDetected() ? "да" : "нет") + "\nДвигатель: " + car.getEngine() +
                "\nРегион: " + car.getRegion() + "\nГород: " + car.getTown() + "\nОбмен: " + car.isExchange() +
                "\nописание: " + car.getDescription() + "\nhttp://rst.ua/" + car.getLink();
        alarmByEmail(subject, message);
    }

    static void alarmByEmail(String subject, String message) {
        final String fromEmail = "zloj_pes@ukr.net";
        final String password = "qaz45asd";
        final String toEmail = "asp4rever@gmail.com";

        System.out.println("SSLEmail Start");
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.ukr.net"); //SMTP Host
        props.put("mail.smtp.socketFactory.port", "465"); //SSL Port
        props.put("mail.smtp.socketFactory.class",
                "javax.net.ssl.SSLSocketFactory"); //SSL Factory Class
        props.put("mail.smtp.auth", "true"); //Enabling SMTP Authentication
        props.put("mail.smtp.port", "465"); //SMTP Port

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
        alarmByEmail("test", "Test sending message\nПроверка русской раскладки");
    }
}
