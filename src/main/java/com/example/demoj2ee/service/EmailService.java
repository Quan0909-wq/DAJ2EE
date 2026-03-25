package com.example.demoj2ee.service;

import com.example.demoj2ee.model.Booking;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendTicketEmail(Booking booking) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(booking.getCustomerEmail());
            helper.setSubject("Xac nhan dat ve thanh cong - PELE Cinema");

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm - dd/MM/yyyy");
            String time = booking.getShowtime().getStartTime().format(formatter);

            String qrData = "PELE-TICKET-" + booking.getId() + "-" + booking.getCustomerPhone();
            String qrUrl = "https://quickchart.io/qr?text=" + qrData + "&size=200";

            String htmlMsg = "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: auto; border: 1px solid #ddd; padding: 20px; border-radius: 10px;'>"
                    + "<h2 style='color: #E50914; text-align: center;'>PELE CINEMA E-TICKET</h2>"
                    + "<p>Chao <b>" + booking.getCustomerName() + "</b>,</p>"
                    + "<p>Cam on cua da dat ve. Duoi day la thong tin ve dien tu cua cua:</p>"
                    + "<div style='background: #f9f9f9; padding: 15px; border-radius: 5px; margin: 15px 0;'>"
                    + "<p>Phim: " + booking.getShowtime().getMovie().getTitle() + "</p>"
                    + "<p>Suat chieu: " + time + " (" + booking.getShowtime().getRoom().getName() + ")</p>"
                    + "<p>Ghe: <span style='color: #E50914; font-weight: bold;'>" + booking.getSeatNumbers() + "</span></p>"
                    + "<p>Tong tien: " + String.format("%,.0f", booking.getTotalAmount()) + " dong</p>"
                    + "<p>Ma Don Hang: #" + booking.getId() + "</p>"
                    + "</div>"
                    + "<div style='text-align: center; margin: 25px 0;'>"
                    + "<p style='font-weight: bold; color: #333; margin-bottom: 10px;'>MA QR SOAT VE</p>"
                    + "<img src='" + qrUrl + "' alt='QR Code' style='border: 2px solid #E50914; border-radius: 10px; padding: 5px; background: white;'/>"
                    + "</div>"
                    + "<p style='color: #555; font-size: 13px; text-align: center;'><i>Vui long dua ma QR nay cho nhan vien tai quay de nhan ve cung va bap nuoc.</i></p>"
                    + "</div>";

            helper.setText(htmlMsg, true);
            mailSender.send(message);
            System.out.println("Da gui email ve thanh cong toi: " + booking.getCustomerEmail());

        } catch (Exception e) {
            System.err.println("Gui email that bai: " + e.getMessage());
        }
    }
}
