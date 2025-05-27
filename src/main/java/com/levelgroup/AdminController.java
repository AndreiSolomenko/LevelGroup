package com.levelgroup;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final ConfigService configService;
    private final String ADMIN_PASSWORD = "admin123"; // краще винести в application.properties

    public AdminController(ConfigService configService) {
        this.configService = configService;
    }

    @GetMapping("/login")
    public String showLogin() {
        return "admin/login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String password, HttpSession session, Model model) {
        if (ADMIN_PASSWORD.equals(password)) {
            session.setAttribute("isAdmin", true);
            return "redirect:/admin/config";
        } else {
            model.addAttribute("error", "Wrong password");
            return "admin/login";
        }
    }

    @GetMapping("/config")
    public String showConfig(Model model, HttpSession session) {
        if (!Boolean.TRUE.equals(session.getAttribute("isAdmin"))) {
            return "redirect:/admin/login";
        }
        model.addAttribute("trialCalls", configService.getTrialCalls());
        return "admin/config";
    }

    @PostMapping("/config")
    public String updateConfig(@RequestParam int trialCalls, HttpSession session) {
        if (!Boolean.TRUE.equals(session.getAttribute("isAdmin"))) {
            return "redirect:/admin/login";
        }
        configService.setTrialCalls(trialCalls);
        return "redirect:/admin/config";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/admin/login";
    }




}
