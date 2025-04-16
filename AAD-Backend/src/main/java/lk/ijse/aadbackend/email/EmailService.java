package lk.ijse.aadbackend.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendWelcomeEmail(String toEmail, String userName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            
            helper.setTo(toEmail);
            helper.setSubject("Welcome to vikunuwa.lk 🎉");

            String emailContent = "<div style='font-family: Arial, sans-serif; color: #333; max-width: 600px; margin: auto; padding: 20px; border: 1px solid #ddd; border-radius: 10px;'>"
                    + "<h2 style='color: #007bff; text-align: center;'>🎉 Welcome to Vikunuwa, " + userName + "! 🎉</h2>"
                    + "<p style='font-size: 16px;'>We are thrilled to have you on board! Vikunuwa is Sri Lanka’s leading classified ads platform, helping you **buy, sell, and discover** great deals in your community.</p>"
                    + "<p style='font-size: 16px;'>Here's what you can do on Vikunuwa:</p>"
                    + "<ul style='font-size: 16px; line-height: 1.6;'>"
                    + "<li>📢 Post ads to sell your products and services</li>"
                    + "<li>🔍 Explore thousands of listings from trusted users</li>"
                    + "<li>💬 Connect with buyers and sellers instantly</li>"
                    + "</ul>"
                    + "<p style='font-size: 16px;'>Start exploring now and make the most of your experience!</p>"
                    + "<div style='text-align: center; margin-top: 20px;'>"
                    + "<a href='https://vikunuwa.lk' style='background-color: #007bff; color: white; padding: 12px 20px; text-decoration: none; border-radius: 5px; font-size: 16px;'>Visit Vikunuwa</a>"
                    + "</div>"
                    + "<p style='font-size: 14px; color: #555; text-align: center; margin-top: 20px;'>If you have any questions, feel free to reach out to our support team.</p>"
                    + "<p style='font-size: 14px; color: #555; text-align: center;'>Best Regards, <br><b>The Vikunuwa Team</b></p>"
                    + "</div>";


            helper.setText(emailContent, true);  // Enable HTML content
            
            mailSender.send(message);
            System.out.println("✅ Welcome email sent successfully to " + toEmail);

        } catch (MessagingException e) {
            e.printStackTrace();
            System.out.println("❌ Failed to send email to " + toEmail);
        }
    }


    public void sendAdDeletionEmail(String toEmail, String userName, String adTitle) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(toEmail);
            helper.setSubject("Your Ad Has Been Deleted - Vikunuwa.lk");
            String emailContent = "<div style='font-family: Arial, sans-serif; color: #333; max-width: 600px; margin: auto; padding: 20px; border: 1px solid #ddd; border-radius: 10px;'>"
                    + "<h2 style='color: #007bff; text-align: center;'>Ad Deletion Confirmation</h2>"
                    + "<p style='font-size: 16px;'>Hello " + userName + ",</p>"
                    + "<p style='font-size: 16px;'>Your advertisement <b>\"" + adTitle + "\"</b> has been successfully deleted from Vikunuwa.lk.</p>"
                    + "<p style='font-size: 16px;'>If you didn't request this deletion or have any concerns, please contact our support team immediately.</p>"
                    + "<p style='font-size: 16px;'>Remember, you can always post new ads anytime:</p>"
                    + "<ul style='font-size: 16px; line-height: 1.6;'>"
                    + "<li>📢 Create a new advertisement</li>"
                    + "<li>🔍 Browse other listings</li>"
                    + "<li>💬 Connect with more buyers and sellers</li>"
                    + "</ul>"
                    + "<div style='text-align: center; margin-top: 20px;'>"
                    + "<a href='https://vikunuwa.lk/post-ad' style='background-color: #007bff; color: white; padding: 12px 20px; text-decoration: none; border-radius: 5px; font-size: 16px;'>Post a New Ad</a>"
                    + "</div>"
                    + "<p style='font-size: 14px; color: #555; text-align: center; margin-top: 20px;'>Thank you for using Vikunuwa.lk - Sri Lanka's leading classified ads platform.</p>"
                    + "<p style='font-size: 14px; color: #555; text-align: center;'>Best Regards, <br><b>The Vikunuwa Team</b></p>"
                    + "</div>";
            helper.setText(emailContent, true);  // Enable HTML content

            mailSender.send(message);
            System.out.println("✅ Ad deletion email sent successfully to " + toEmail);
        } catch (MessagingException e) {
            e.printStackTrace();
            System.out.println("❌ Failed to send ad deletion email to " + toEmail);
        }
    }
}
