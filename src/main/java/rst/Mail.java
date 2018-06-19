package rst;

import java.util.Date;
import java.util.Properties;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

class Mail {
    private static Properties prop = Explorer.getProp();

    static void sendCar(Car car) {
        sendCar("Обнаружено новое авто!", car, "Краткое описание:");
    }

    static void sendCar(String subject, Car car, String message) {
        StringBuilder body = new StringBuilder().append(message).append("\nМарка: ").append(car.getBrand()).append("\nМодель: ")
                .append(car.getModel()).append("\nСостояние: ").append(car.getCondition()).append("\nЦена: ").append(car.getPrice())
                .append("$\nГод: ").append(car.getBuildYear()).append("\nПробег: ").append(car.getMileage()).append("\nДобавлен:")
                .append(car.getDetectedDate()).append("\nДобавлен свежим: ").append(car.isFreshDetected() ? "да" : "нет")
                .append("\nДвигатель: ").append(car.getEngine()).append("\nРегион: ").append(car.getRegion()).append("\nГород: ")
                .append(car.getTown()).append("\nОбмен: ").append(car.isExchange() ? "да" : "нет").append("\nописание: ")
                .append(car.getDescription()).append("\nИмя:").append(car.getOwnerName()).append(" - ")
                .append(String.join(", ", car.getPhones())).append("\nhttp://rst.ua/").append(car.getLink())
                .append("\nИстория изменений:");
        for (String comment : car.getComments()) {
            body.append("\n").append(comment);
        }
        alarmByEmail(subject, body.toString());
    }

    static void alarmByEmail(String subject, String message) {
        final String fromEmail = prop.getProperty("from_email");
        final String password = prop.getProperty("password");
        final String toEmail = prop.getProperty("to_email");

        Properties props = new Properties();
        props.put("mail.smtp.host", prop.getProperty("smtp_host")); //SMTP Host
        props.put("mail.smtp.socketFactory.port", prop.getProperty("ssl_port")); //SSL Port
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory"); //SSL Factory Class
        props.put("mail.smtp.auth", "true"); //Enabling SMTP Authentication
        props.put("mail.smtp.port", prop.getProperty("smtp_port")); //SMTP Port

        Authenticator auth = new Authenticator() {
            //override the getPasswordAuthentication method
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(fromEmail, password);
            }
        };

        Session session = Session.getDefaultInstance(props, auth);
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
            Transport.send(msg);
            System.out.println("EMail Sent Successfully!!");
        } catch (Exception e) {
            System.out.println("EMail Sent failed!!");
            e.printStackTrace();
        }
    }
}
