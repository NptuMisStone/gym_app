package com.NPTUMisStone.gym_app.Main.Identify;

import android.content.Context;

import java.util.Properties;
import java.util.concurrent.Executors;

import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

//透過Java Mail API 發送郵件：https://stackoverflow.com/questions/49786771/sending-email-android-using-javamail-api
//使用Java Mail API發送郵件：https://www.cnblogs.com/guanxinjing/p/9952055.html
//AsyncTask運作方式：https://aiur3908.blogspot.com/2015/06/android-asynctask.html
//AsyncTask替代方式：https://stackoverflow.com/questions/58767733/the-asynctask-api-is-deprecated-in-android-11-what-are-the-alternatives
public class JavaMailAPI {
    Context context;
    String email;
    String subject;
    String message;

    public interface EmailSendResultCallback {  //用Interface實作Call Back機制：https://andy02172001.blogspot.com/2017/10/androidinterfacecall-back.html
        void onSuccess();                       //Java拋出例外方式：https://stevenjhu.com/2019/10/08/java-%E7%A8%8B%E5%BC%8F%E8%A8%AD%E8%A8%88%E5%9F%BA%E7%A4%8E-throw/
        void onFailure(Exception e);            //throws介紹：https://caterpillar.gitbooks.io/javase6tutorial/content/c10_3.html
    }

    public JavaMailAPI(Context context, String email, String subject, String message) { //JavaMailAPI 發送郵件失敗：https://stackoverflow.com/questions/59542638/java-mail-api-send-email-failed
        this.context = context;
        this.email = email;
        this.subject = subject;
        this.message = message;
    }

    public void sendMail(EmailSendResultCallback callback) {
        Executors.newSingleThreadExecutor().execute(() -> {
            Session session = Session.getDefaultInstance(getProperties(), new javax.mail.Authenticator() { //取得GOOGLE APP 密碼：https://stackoverflow.com/questions/19973456/getting-error-in-java-mail-api-passwordauthentication-method
                protected PasswordAuthentication getPasswordAuthentication() {           //取得GOOGLE APP 密碼：https://support.google.com/accounts/answer/185833?hl=en#zippy=%2Cwhy-you-may-need-an-app-password%2Cyou-still-cant-sign-in
                    return new PasswordAuthentication("NptuMisStone@gmail.com", "lgtb rhoq irjc flyi"); //新增英文用詞：https://stackoverflow.com/questions/16709263/how-to-add-a-dictionary-for-spell-check-in-android-studio-intellij-idea
                }
            });
            try {
                MimeMessage mimeMessage = new MimeMessage(session);
                mimeMessage.setFrom(new InternetAddress("NptuMisStone@gmail.com"));
                mimeMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(email));
                mimeMessage.setSubject(subject);
                mimeMessage.setText(message);
                Transport.send(mimeMessage);
                callback.onSuccess();
            }catch (Exception e) {
                callback.onFailure(e);
            }
        });
    }

    private static Properties getProperties() {
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "465");
        return props;
    }
}
