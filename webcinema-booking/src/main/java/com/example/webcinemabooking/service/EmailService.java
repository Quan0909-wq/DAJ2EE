package com.example.webcinemabooking.service;

import com.example.webcinemabooking.model.Booking;
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
            helper.setSubject("🎟️ Xác nhận đặt vé thành công - PELE Cinema");

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm - dd/MM/yyyy");
            String time = booking.getShowtime().getStartTime().format(formatter);

            // MỚI THÊM: Tạo dữ liệu cho QR Code (Chứa Mã vé và SĐT để nhân viên quét)
            String qrData = "PELE-TICKET-" + booking.getId() + "-" + booking.getCustomerPhone();
            // Dùng API QuickChart tạo mã QR tự động từ dữ liệu trên
            String qrUrl = "https://quickchart.io/qr?text=" + qrData + "&size=200";

            // Nâng cấp giao diện Email, chèn thêm ảnh QR Code
            String htmlMsg = "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: auto; border: 1px solid #ddd; padding: 20px; border-radius: 10px;'>"
                    + "<h2 style='color: #E50914; text-align: center;'>PELE CINEMA E-TICKET</h2>"
                    + "<p>Chào <b>" + booking.getCustomerName() + "</b>,</p>"
                    + "<p>Cảm ơn cậu đã đặt vé. Dưới đây là thông tin vé điện tử của cậu:</p>"
                    + "<div style='background: #f9f9f9; padding: 15px; border-radius: 5px; margin: 15px 0;'>"
                    + "<p>🎬 <b>Phim:</b> " + booking.getShowtime().getMovie().getTitle() + "</p>"
                    + "<p>⏰ <b>Suất chiếu:</b> " + time + " (" + booking.getShowtime().getRoom().getName() + ")</p>"
                    + "<p>💺 <b>Ghế:</b> <span style='color: #E50914; font-weight: bold;'>" + booking.getSeatNumbers() + "</span></p>"
                    + "<p>💰 <b>Tổng tiền:</b> " + String.format("%,.0f", booking.getTotalAmount()) + " đ</p>"
                    + "<p>🎫 <b>Mã Đơn Hàng:</b> #" + booking.getId() + "</p>"
                    + "</div>"
                    // CHÈN HÌNH ẢNH QR VÀO ĐÂY
                    + "<div style='text-align: center; margin: 25px 0;'>"
                    + "<p style='font-weight: bold; color: #333; margin-bottom: 10px;'>MÃ QR SOÁT VÉ</p>"
                    + "<img src='" + qrUrl + "' alt='QR Code' style='border: 2px solid #E50914; border-radius: 10px; padding: 5px; background: white;'/>"
                    + "</div>"
                    + "<p style='color: #555; font-size: 13px; text-align: center;'><i>Vui lòng đưa mã QR này cho nhân viên tại quầy để nhận vé cứng và bắp nước.</i></p>"
                    + "</div>";

            helper.setText(htmlMsg, true);
            mailSender.send(message);
            System.out.println("✅ Đã gửi email vé (Kèm QR Code) thành công tới: " + booking.getCustomerEmail());

        } catch (Exception e) {
            System.err.println("❌ Gửi email thất bại: " + e.getMessage());
        }
    }
}