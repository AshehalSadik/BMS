package pk.edu.nu.isb.bms.controllers;

import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import pk.edu.nu.isb.bms.models.*;

@Controller
public class ContentController {

    private final MyUserService userService;
    private final FacultyService facultyService;
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;

    public ContentController(MyUserService userService, FacultyService facultyService, ReviewRepository reviewRepository, UserRepository userRepository) {
        this.userService = userService;
        this.facultyService = facultyService;
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/")
    public String home(@RequestParam(value = "q", required = false) String q,
                       @RequestParam(value = "dept", required = false, defaultValue = "All") String dept,
                       Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean authenticated = auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName());
        if (!authenticated) {
            return "redirect:/login";
        }

        model.addAttribute("q", q == null ? "" : q);
        model.addAttribute("dept", dept == null ? "All" : dept);
        model.addAttribute("faculty", facultyService.search(q, dept));

        userRepository.findByUsername(auth.getName()).ifPresentOrElse(
                u -> model.addAttribute("currentUser", u),
                () -> model.addAttribute("currentUser", null)
        );

        return "home";
    }

    @GetMapping("/faculty/{id}")
    public String facultyDetail(@PathVariable Long id, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean authenticated = auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName());
        if (!authenticated) {
            return "redirect:/login";
        }

        var maybe = facultyService.findById(id);
        if (maybe.isEmpty()) {
            model.addAttribute("missingId", id);
            return "faculty-not-found";
        }
        Faculty f = maybe.get();
        model.addAttribute("faculty", f);
        model.addAttribute("reviews", reviewRepository.findByFaculty_Id(id));
        userRepository.findByUsername(auth.getName()).ifPresentOrElse(
                u -> model.addAttribute("currentUser", u),
                () -> model.addAttribute("currentUser", null)
        );
        return "faculty";
    }

    @PostMapping("/faculty/{id}/reviews")
    public String addReview(@PathVariable Long id,
                            @RequestParam int rating,
                            @RequestParam String comment) {
        // Enforce anonymity: do not record reviewer user id
        Review r = new Review();
        r.setFaculty(new FacultyEntity() {{ setId(id); }});
        r.setRating(rating);
        r.setComment(comment);
        // Enforce anonymity: always store 'Anonymous'
        r.setStudentName("Anonymous");
        reviewRepository.save(r);
        return "redirect:/faculty/" + id;
    }

    @GetMapping("/login")
    public String login(@RequestParam(value = "invalid", required = false) String invalid,
                        @RequestParam(value = "locked", required = false) String locked,
                        @RequestParam(value = "disabled", required = false) String disabled,
                        @RequestParam(value = "error", required = false) String error,
                        @RequestParam(value = "logout", required = false) String logout,
                        @RequestParam(value = "registered", required = false) String registered,
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
        if (registered != null) {
            model.addAttribute("loginSuccess", "Registration successful. Please log in.");
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
            model.addAttribute("signupError", bindingResult.getFieldError() != null ? bindingResult.getFieldError().getDefaultMessage() : "Invalid signup data");
            return "signup";
        }

        userService.registerUser(request);
        // After successful registration redirect to login page so user can sign in
        return "redirect:/login?registered=true";
    }
}
