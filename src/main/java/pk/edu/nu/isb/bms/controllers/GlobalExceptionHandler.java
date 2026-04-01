package pk.edu.nu.isb.bms.controllers;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pk.edu.nu.isb.bms.models.DuplicateUserFieldException;
import pk.edu.nu.isb.bms.models.RegistrationRequest;
import pk.edu.nu.isb.bms.models.WeakPasswordException;
import jakarta.validation.ConstraintViolationException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DuplicateUserFieldException.class)
    public String handleDuplicateUserField(DuplicateUserFieldException ex, Model model) {
        model.addAttribute("signupError", ex.getMessage());
        if (!model.containsAttribute("registrationRequest")) {
            model.addAttribute("registrationRequest", new RegistrationRequest());
        }
        return "signup";
    }

    @ExceptionHandler(WeakPasswordException.class)
    public String handleWeakPassword(WeakPasswordException ex, Model model) {
        model.addAttribute("signupError", ex.getMessage());
        if (!model.containsAttribute("registrationRequest")) {
            model.addAttribute("registrationRequest", new RegistrationRequest());
        }
        return "signup";
    }

    @ExceptionHandler({DataIntegrityViolationException.class, ConstraintViolationException.class})
    public String handleDatabaseConstraint(Exception ex, Model model) {
        model.addAttribute("signupError", "Unable to process request due to data constraints. Please verify your input.");
        if (!model.containsAttribute("registrationRequest")) {
            model.addAttribute("registrationRequest", new RegistrationRequest());
        }
        return "signup";
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public String handleIllegalArgument(IllegalArgumentException ex, RedirectAttributes redirectAttributes, HttpServletRequest request) {
        // Put the error message into flash attributes so the admin page can display it
        redirectAttributes.addFlashAttribute("adminError", ex.getMessage());

        // If the request came from admin pages, redirect to /admin, otherwise back to root
        String referer = request.getHeader("Referer");
        if (referer != null && referer.contains("/admin")) {
            return "redirect:/admin";
        }
        return "redirect:/";
    }
}
