package pk.edu.nu.isb.bms.controllers;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import pk.edu.nu.isb.bms.models.DuplicateUserFieldException;
import pk.edu.nu.isb.bms.models.MyUserService;
import pk.edu.nu.isb.bms.models.RegistrationRequest;
import pk.edu.nu.isb.bms.models.WeakPasswordException;

@Controller
public class ContentController {

    private final MyUserService userService;

    public ContentController(MyUserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/signup")
    public String signup(Model model) {
        if (!model.containsAttribute("registrationRequest")) {
            model.addAttribute("registrationRequest", new RegistrationRequest());
        }
        return "signup";
    }

    @PostMapping("/signup")
    public String register(@Valid @ModelAttribute("registrationRequest") RegistrationRequest request,
                           BindingResult bindingResult,
                           Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("signupError", bindingResult.getFieldError().getDefaultMessage());
            return "signup";
        }

        try {
            userService.registerUser(request);
            model.addAttribute("signupSuccess", "Account created successfully. You can now sign in.");
            model.addAttribute("registrationRequest", new RegistrationRequest());
            return "signup";
        } catch (DuplicateUserFieldException | WeakPasswordException ex) {
            model.addAttribute("signupError", ex.getMessage());
            return "signup";
        }
    }
}
