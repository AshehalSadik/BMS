package pk.edu.nu.isb.bms.controllers;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import pk.edu.nu.isb.bms.models.FacultyService;
import pk.edu.nu.isb.bms.models.MyUserService;
import pk.edu.nu.isb.bms.models.RegistrationRequest;

@Controller
public class ContentController {

    private final MyUserService userService;
    private final FacultyService facultyService;

    public ContentController(MyUserService userService, FacultyService facultyService) {
        this.userService = userService;
        this.facultyService = facultyService;
    }

    @GetMapping("/")
    public String home(@RequestParam(value = "q", required = false) String q,
                       @RequestParam(value = "dept", required = false, defaultValue = "All") String dept,
                       Model model) {
        model.addAttribute("q", q == null ? "" : q);
        model.addAttribute("dept", dept == null ? "All" : dept);
        model.addAttribute("faculty", facultyService.search(q, dept));
        return "home";
    }

    @GetMapping("/login")
    public String login(@RequestParam(value = "invalid", required = false) String invalid,
                        @RequestParam(value = "locked", required = false) String locked,
                        @RequestParam(value = "disabled", required = false) String disabled,
                        @RequestParam(value = "error", required = false) String error,
                        @RequestParam(value = "logout", required = false) String logout,
                        Model model) {
        if (invalid != null) {
            model.addAttribute("loginError", "Invalid credentials. Please check your username and password.");
        } else if (locked != null) {
            model.addAttribute("loginError", "Your account is locked. Please contact support.");
        } else if (disabled != null) {
            model.addAttribute("loginError", "Your account is disabled. Please contact support.");
        } else if (error != null) {
            model.addAttribute("loginError", "Login failed. Please try again.");
        }

        if (logout != null) {
            model.addAttribute("loginSuccess", "You have been logged out successfully.");
        }

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

        userService.registerUser(request);
        model.addAttribute("signupSuccess", "Account created successfully. You can now sign in.");
        model.addAttribute("registrationRequest", new RegistrationRequest());
        return "signup";
    }
}
