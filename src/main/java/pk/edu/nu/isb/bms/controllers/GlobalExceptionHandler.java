package pk.edu.nu.isb.bms.controllers;

import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import pk.edu.nu.isb.bms.models.DuplicateUserFieldException;
import pk.edu.nu.isb.bms.models.RegistrationRequest;
import pk.edu.nu.isb.bms.models.WeakPasswordException;

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
}

