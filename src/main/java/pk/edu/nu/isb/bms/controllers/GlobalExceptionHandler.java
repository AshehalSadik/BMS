package pk.edu.nu.isb.bms.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import pk.edu.nu.isb.bms.models.DuplicateUserFieldException;
import pk.edu.nu.isb.bms.models.RegistrationRequest;
import pk.edu.nu.isb.bms.models.WeakPasswordException;
import jakarta.validation.ConstraintViolationException;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

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
        logger.error("Database constraint exception while processing request", ex);
        model.addAttribute("signupError", "Unable to process request due to data constraints. Please verify your input.");
        if (!model.containsAttribute("registrationRequest")) {
            model.addAttribute("registrationRequest", new RegistrationRequest());
        }
        return "signup";
    }
}
