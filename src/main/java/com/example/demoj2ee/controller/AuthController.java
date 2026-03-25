package com.example.demoj2ee.controller;

import com.example.demoj2ee.model.User;
import com.example.demoj2ee.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Base64;

@Controller
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/register")
    public String showRegisterForm() {
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@RequestParam String username, @RequestParam String password,
                             @RequestParam String email, @RequestParam String fullName,
                             @RequestParam String phone, Model model) {
        if (userRepository.findByUsername(username) != null || userRepository.findByEmail(email) != null) {
            model.addAttribute("error", "Ten dang nhap hoac Email da duoc su dung!");
            return "register";
        }

        User newUser = new User();
        newUser.setUsername(username);
        newUser.setPassword(password);
        newUser.setEmail(email);
        newUser.setFullName(fullName);
        newUser.setPhone(phone);
        newUser.setRole("USER");
        userRepository.save(newUser);

        return "redirect:/login?success=true";
    }

    @GetMapping("/login")
    public String showLoginForm() {
        return "login";
    }

    @PostMapping("/login")
    public String loginUser(@RequestParam String username, @RequestParam String password,
                            HttpSession session, Model model) {
        User user = userRepository.findByUsername(username);

        if (user != null && user.getPassword().equals(password)) {
            session.setAttribute("loggedInUser", user);
            return "redirect:/";
        }

        model.addAttribute("error", "Sai ten dang nhap hoac mat khau!");
        return "login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session, RedirectAttributes ra) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user != null && !user.isDatingProfileComplete()) {
            ra.addFlashAttribute("errorMessage", "Hay hoan thien ho so dating truoc khi dang xuat!");
            return "redirect:/settings/dating-profile";
        }
        session.invalidate();
        return "redirect:/";
    }

    // ========== DATING PROFILE SETUP ==========

    @GetMapping("/settings/dating-profile")
    public String showDatingProfile(HttpSession session, Model model) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) return "redirect:/login";
        model.addAttribute("user", user);
        return "dating-profile";
    }

    @PostMapping("/settings/dating-profile")
    public String saveDatingProfile(
            @RequestParam(value = "datingAvatarFile", required = false) MultipartFile datingAvatarFile,
            @RequestParam(value = "photo1", required = false) MultipartFile photo1,
            @RequestParam(value = "photo2", required = false) MultipartFile photo2,
            @RequestParam(value = "photo3", required = false) MultipartFile photo3,
            @RequestParam(value = "fullName", required = false) String fullName,
            @RequestParam(value = "ageStr", required = false) String ageStr,
            @RequestParam(value = "heightStr", required = false) String heightStr,
            @RequestParam(value = "hometown", required = false) String hometown,
            @RequestParam(value = "relationshipStatus", required = false) String relationshipStatus,
            @RequestParam(value = "datingBio", required = false) String datingBio,
            HttpSession session,
            RedirectAttributes ra) {

        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) return "redirect:/login";

        // Validation
        if (datingAvatarFile == null || datingAvatarFile.isEmpty()) {
            ra.addFlashAttribute("errorMessage", "Ban bat buoc phai dat anh dai dien!");
            return "redirect:/settings/dating-profile";
        }
        if (fullName == null || fullName.trim().length() < 2) {
            ra.addFlashAttribute("errorMessage", "Ten phai it nhat 2 ky tu!");
            return "redirect:/settings/dating-profile";
        }
        int age = 0;
        try {
            age = Integer.parseInt(ageStr);
            if (age < 18 || age > 99) {
                ra.addFlashAttribute("errorMessage", "Tuoi phai nam trong khoang 18 - 99!");
                return "redirect:/settings/dating-profile";
            }
        } catch (NumberFormatException e) {
            ra.addFlashAttribute("errorMessage", "Tuoi khong hop le!");
            return "redirect:/settings/dating-profile";
        }
        double height = 0;
        try {
            height = Double.parseDouble(heightStr);
            if (height < 100 || height > 250) {
                ra.addFlashAttribute("errorMessage", "Chieu cao phai nam trong khoang 100 - 250 cm!");
                return "redirect:/settings/dating-profile";
            }
        } catch (NumberFormatException e) {
            ra.addFlashAttribute("errorMessage", "Chieu cao khong hop le!");
            return "redirect:/settings/dating-profile";
        }
        if (hometown == null || hometown.trim().length() < 2) {
            ra.addFlashAttribute("errorMessage", "Que quan khong duoc de trong!");
            return "redirect:/settings/dating-profile";
        }
        if (relationshipStatus == null || relationshipStatus.isBlank()) {
            ra.addFlashAttribute("errorMessage", "Vui long chon tinh trang hanh phuc!");
            return "redirect:/settings/dating-profile";
        }
        if (datingBio == null || datingBio.trim().length() < 10) {
            ra.addFlashAttribute("errorMessage", "Gioi thieu ban than it nhat 10 ky tu!");
            return "redirect:/settings/dating-profile";
        }

        try {
            // Set avatar
            String avatarBase64 = Base64.getEncoder().encodeToString(datingAvatarFile.getBytes());
            user.setDatingAvatar("data:" + datingAvatarFile.getContentType() + ";base64," + avatarBase64);
            user.setAvatar(user.getDatingAvatar()); // also update main avatar

            // Set other fields
            user.setFullName(fullName.trim());
            user.setAge(age);
            user.setHeight(height);
            user.setHometown(hometown.trim());
            user.setRelationshipStatus(relationshipStatus);
            user.setDatingBio(datingBio.trim());

            // Save photos (optional)
            if (photo1 != null && !photo1.isEmpty()) {
                String p1 = Base64.getEncoder().encodeToString(photo1.getBytes());
                user.setDatingPhoto1("data:" + photo1.getContentType() + ";base64," + p1);
            }
            if (photo2 != null && !photo2.isEmpty()) {
                String p2 = Base64.getEncoder().encodeToString(photo2.getBytes());
                user.setDatingPhoto2("data:" + photo2.getContentType() + ";base64," + p2);
            }
            if (photo3 != null && !photo3.isEmpty()) {
                String p3 = Base64.getEncoder().encodeToString(photo3.getBytes());
                user.setDatingPhoto3("data:" + photo3.getContentType() + ";base64," + p3);
            }

            user.setDatingProfileComplete(true);
            userRepository.save(user);
            session.setAttribute("loggedInUser", user);

            ra.addFlashAttribute("successMessage", "Ho so dating da duoc hoan thien! Ban co the dang bai ngay.");
            return "redirect:/pele-dating";

        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Loi luu ho so: " + e.getMessage());
            return "redirect:/settings/dating-profile";
        }
    }
}
