package pk.edu.nu.isb.bms.controllers;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import pk.edu.nu.isb.bms.models.*;
import pk.edu.nu.isb.bms.services.AdminService;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final AdminService adminService;
    private final UserRepository userRepository;

    public AdminController(AdminService adminService, UserRepository userRepository) {
        this.adminService = adminService;
        this.userRepository = userRepository;
    }

    private Long actorId(Authentication authentication) {
        if (authentication == null) {
            throw new IllegalStateException("Missing authentication");
        }
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found: " + username))
                .getId();
    }

    @GetMapping("")
    public String dashboard(Model model) {
        model.addAttribute("users", adminService.listUsers());
        model.addAttribute("reportedReviews", adminService.listReportedReviews());
        model.addAttribute("faculties", adminService.listFaculties());
        model.addAttribute("departments", adminService.listDepartments());
        model.addAttribute("auditLogs", adminService.listAuditLogs());
        return "admin";
    }

    @PostMapping("/users/{id}/role")
    public String setRole(@PathVariable Long id, @RequestParam String role, Authentication authentication) {
        adminService.setUserRole(id, role, actorId(authentication));
        return "redirect:/admin";
    }

    @PostMapping("/users/{id}/enable")
    public String setEnabled(@PathVariable Long id, @RequestParam boolean enabled, Authentication authentication) {
        adminService.setUserEnabled(id, enabled, actorId(authentication));
        return "redirect:/admin";
    }

    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable Long id, Authentication authentication) {
        adminService.deleteUser(id, actorId(authentication));
        return "redirect:/admin";
    }

    @PostMapping("/reviews/{id}/delete")
    public String deleteReview(@PathVariable Long id, Authentication authentication) {
        adminService.deleteReview(id, actorId(authentication));
        return "redirect:/admin";
    }

    @PostMapping("/faculties/add")
    public String addFaculty(@ModelAttribute FacultyEntity faculty, Authentication authentication) {
        adminService.addFaculty(faculty, actorId(authentication));
        return "redirect:/admin";
    }

    @PostMapping("/departments/add")
    public String addDepartment(@RequestParam String name, Authentication authentication) {
        adminService.addDepartment(name, actorId(authentication));
        return "redirect:/admin";
    }
}
