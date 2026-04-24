package pk.edu.nu.isb.bms.controllers;

import jakarta.persistence.EntityManager;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import pk.edu.nu.isb.bms.models.*;

import java.util.List;

@Controller
public class ContentController {

    private final MyUserService userService;
    private final FacultyService facultyService;
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final EntityManager entityManager;

    public ContentController(MyUserService userService,
                             FacultyService facultyService,
                             ReviewRepository reviewRepository,
                             UserRepository userRepository,
                             EntityManager entityManager) {
        this.userService = userService;
        this.facultyService = facultyService;
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
        this.entityManager = entityManager;
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
    public String facultyDetail(@PathVariable Long id,
                                @RequestParam(value = "reviewSubmitted", required = false) String reviewSubmitted,
                                Model model) {
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
        List<Review> reviews = reviewRepository.findByFaculty_Id(id);

        model.addAttribute("faculty", f);
        model.addAttribute("reviews", reviews);
        model.addAttribute("courses", findCoursesForFaculty(id));
        model.addAttribute("reviewSubmitted", reviewSubmitted != null);

        model.addAttribute("criteriaLabels", List.of(
                "Subject Matter Knowledge",
                "Teaching Methods",
                "Student Engagement",
                "Collaboration & Teamwork",
                "Behavior Management",
                "Classroom Environment",
                "Professional Ethics",
                "Communication Skills"
        ));

        model.addAttribute("criteriaAverages", List.of(
                averageScore(reviews, "smk"),
                averageScore(reviews, "tm"),
                averageScore(reviews, "se"),
                averageScore(reviews, "ct"),
                averageScore(reviews, "bm"),
                averageScore(reviews, "ce"),
                averageScore(reviews, "pe"),
                averageScore(reviews, "cs")
        ));

        userRepository.findByUsername(auth.getName()).ifPresentOrElse(
                u -> model.addAttribute("currentUser", u),
                () -> model.addAttribute("currentUser", null)
        );
        return "faculty";
    }

    @PostMapping("/faculty/{id}/reviews")
    public String addReview(@PathVariable Long id,
                        @RequestParam(required = false) Integer rating,
                        @RequestParam String comment,
                        @RequestParam(required = false) Long courseId,
                        @RequestParam(required = false) Integer subjectMatterKnowledge,
                        @RequestParam(required = false) Integer teachingMethods,
                        @RequestParam(required = false) Integer studentEngagement,
                        @RequestParam(required = false) Integer collaborationTeamwork,
                        @RequestParam(required = false) Integer behaviorManagement,
                        @RequestParam(required = false) Integer classroomEnvironment,
                        @RequestParam(required = false) Integer professionalEthics,
                        @RequestParam(required = false) Integer communicationSkills) {
        int baseRating = normalizeRating(rating == null ? 5 : rating);

        short smk = (short) normalizeRating(subjectMatterKnowledge == null ? baseRating : subjectMatterKnowledge);
        short tm = (short) normalizeRating(teachingMethods == null ? baseRating : teachingMethods);
        short se = (short) normalizeRating(studentEngagement == null ? baseRating : studentEngagement);
        short ct = (short) normalizeRating(collaborationTeamwork == null ? baseRating : collaborationTeamwork);
        short bm = (short) normalizeRating(behaviorManagement == null ? baseRating : behaviorManagement);
        short ce = (short) normalizeRating(classroomEnvironment == null ? baseRating : classroomEnvironment);
        short pe = (short) normalizeRating(professionalEthics == null ? baseRating : professionalEthics);
        short cs = (short) normalizeRating(communicationSkills == null ? baseRating : communicationSkills);

        Review r = new Review();
        FacultyEntity facultyRef = new FacultyEntity();
        facultyRef.setId(id);
        r.setFaculty(facultyRef);

        if (courseId != null && isCourseAssignedToFaculty(courseId, id)) {
            r.setCourse(entityManager.getReference(Course.class, courseId));
        }

        r.setSubjectMatterKnowledge(smk);
        r.setTeachingMethods(tm);
        r.setStudentEngagement(se);
        r.setCollaborationTeamwork(ct);
        r.setBehaviorManagement(bm);
        r.setClassroomEnvironment(ce);
        r.setProfessionalEthics(pe);
        r.setCommunicationSkills(cs);

        int avg = Math.round((smk + tm + se + ct + bm + ce + pe + cs) / 8.0f);
        r.setRating(normalizeRating(avg));
        r.setComment(comment == null ? "" : comment.trim());

        // Enforce anonymity: always store 'Anonymous'
        r.setStudentName("Anonymous");
        reviewRepository.save(r);
        return "redirect:/faculty/" + id + "?reviewSubmitted=1";
    }

    private int normalizeRating(int rating) {
        if (rating < 1) return 1;
        return Math.min(rating, 5);
    }

    @SuppressWarnings("unchecked")
    private List<Course> findCoursesForFaculty(Long facultyId) {
        return entityManager.createNativeQuery(
                        "SELECT c.* FROM courses c INNER JOIN faculty_courses fc ON fc.course_id = c.id WHERE fc.faculty_id = :facultyId ORDER BY c.code",
                        Course.class)
                .setParameter("facultyId", facultyId)
                .getResultList();
    }

    private boolean isCourseAssignedToFaculty(Long courseId, Long facultyId) {
        Object value = entityManager.createNativeQuery(
                        "SELECT EXISTS (SELECT 1 FROM faculty_courses WHERE faculty_id = :facultyId AND course_id = :courseId)")
                .setParameter("facultyId", facultyId)
                .setParameter("courseId", courseId)
                .getSingleResult();
        return value instanceof Boolean b ? b : "t".equalsIgnoreCase(String.valueOf(value));
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

    private double averageScore(List<Review> reviews, String criterion) {
        if (reviews == null || reviews.isEmpty()) {
            return 0.0;
        }

        double total = 0.0;
        for (Review r : reviews) {
            total += switch (criterion) {
                case "smk" -> scoreOrRating(r.getSubjectMatterKnowledge(), r.getRating());
                case "tm" -> scoreOrRating(r.getTeachingMethods(), r.getRating());
                case "se" -> scoreOrRating(r.getStudentEngagement(), r.getRating());
                case "ct" -> scoreOrRating(r.getCollaborationTeamwork(), r.getRating());
                case "bm" -> scoreOrRating(r.getBehaviorManagement(), r.getRating());
                case "ce" -> scoreOrRating(r.getClassroomEnvironment(), r.getRating());
                case "pe" -> scoreOrRating(r.getProfessionalEthics(), r.getRating());
                case "cs" -> scoreOrRating(r.getCommunicationSkills(), r.getRating());
                default -> r.getRating();
            };
        }

        double avg = total / reviews.size();
        return Math.round(avg * 10.0) / 10.0;
    }

    private int scoreOrRating(short criterionScore, int rating) {
        return criterionScore > 0 ? criterionScore : rating;
    }
}
