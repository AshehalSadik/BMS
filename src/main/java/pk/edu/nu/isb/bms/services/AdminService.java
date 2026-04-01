package pk.edu.nu.isb.bms.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pk.edu.nu.isb.bms.models.*;

import java.util.List;
import java.util.Optional;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final FacultyRepository facultyRepository;
    private final ReviewRepository reviewRepository;
    private final DepartmentRepository departmentRepository;
    private final AuditLogRepository auditLogRepository;

    public AdminService(UserRepository userRepository,
                        FacultyRepository facultyRepository,
                        ReviewRepository reviewRepository,
                        DepartmentRepository departmentRepository,
                        AuditLogRepository auditLogRepository) {
        this.userRepository = userRepository;
        this.facultyRepository = facultyRepository;
        this.reviewRepository = reviewRepository;
        this.departmentRepository = departmentRepository;
        this.auditLogRepository = auditLogRepository;
    }

    public List<MyUser> listUsers() { return userRepository.findAll(); }

    public Optional<MyUser> findUser(Long id) { return userRepository.findById(id); }

    @Transactional
    public void setUserRole(Long userId, String role, Long adminUserId) {
        var actor = userRepository.findById(adminUserId).orElseThrow();
        var target = userRepository.findById(userId).orElseThrow();

        if (target.getId().equals(actor.getId()) && !"ROLE_ADMIN".equalsIgnoreCase(role)) {
            logAction(adminUserId, "SET_ROLE_BLOCKED", "Blocked self-demotion attempt for user " + userId + " to " + role);
            throw new IllegalArgumentException("You cannot demote your own admin role.");
        }

        target.setRole(role);
        userRepository.save(target);
        logAction(adminUserId, "SET_ROLE", "Set role of user " + userId + " to " + role);
    }

    @Transactional
    public void setUserEnabled(Long userId, boolean enabled, Long adminUserId) {
        var actor = userRepository.findById(adminUserId).orElseThrow();
        var target = userRepository.findById(userId).orElseThrow();

        if (target.getId().equals(actor.getId())) {
            logAction(adminUserId, "SUSPEND_USER_BLOCKED", "Blocked self-enable/disable for user " + userId);
            throw new IllegalArgumentException("You cannot change your own enabled status.");
        }

        if ("ROLE_ADMIN".equalsIgnoreCase(target.getRole())) {
            logAction(adminUserId, "SUSPEND_USER_BLOCKED", "Blocked enable/disable of admin user " + userId);
            throw new IllegalArgumentException("Admin accounts cannot be enabled/disabled from admin panel.");
        }

        target.setEnabled(enabled);
        userRepository.save(target);
        logAction(adminUserId, enabled ? "UNSUSPEND_USER" : "SUSPEND_USER", (enabled ? "Enabled" : "Disabled") + " user " + userId);
    }

    @Transactional
    public void deleteUser(Long userId, Long adminUserId) {
        var actor = userRepository.findById(adminUserId).orElseThrow();
        var target = userRepository.findById(userId).orElseThrow();

        if (target.getId().equals(actor.getId())) {
            logAction(adminUserId, "DELETE_USER_BLOCKED", "Blocked self-delete for user " + userId);
            throw new IllegalArgumentException("You cannot delete your own account.");
        }

        if ("ROLE_ADMIN".equalsIgnoreCase(target.getRole())) {
            logAction(adminUserId, "DELETE_USER_BLOCKED", "Blocked delete of admin user " + userId);
            throw new IllegalArgumentException("Admin accounts cannot be deleted from admin panel.");
        }

        userRepository.deleteById(userId);
        logAction(adminUserId, "DELETE_USER", "Deleted user " + userId);
    }

    public List<Review> listReportedReviews() {
        return reviewRepository.findAll().stream().filter(Review::isReported).toList();
    }

    @Transactional
    public void deleteReview(Long reviewId, Long adminUserId) {
        reviewRepository.deleteById(reviewId);
        var log = new AuditLog();
        log.setActorUserId(adminUserId);
        log.setAction("DELETE_REVIEW");
        log.setDetails("Deleted review " + reviewId);
        auditLogRepository.save(log);
    }

    public List<FacultyEntity> listFaculties() { return facultyRepository.findAll(); }

    @Transactional
    public FacultyEntity addFaculty(FacultyEntity f, Long adminUserId) {
        FacultyEntity saved = facultyRepository.save(f);
        var log = new AuditLog();
        log.setActorUserId(adminUserId);
        log.setAction("ADD_FACULTY");
        log.setDetails("Added faculty " + saved.getId());
        auditLogRepository.save(log);
        return saved;
    }

    public List<Department> listDepartments() { return departmentRepository.findAll(); }

    @Transactional
    public Department addDepartment(String name, Long adminUserId) {
        Department existing = departmentRepository.findByNameIgnoreCase(name);
        if (existing != null) return existing;
        Department d = new Department();
        d.setName(name);
        Department saved = departmentRepository.save(d);
        var log = new AuditLog();
        log.setActorUserId(adminUserId);
        log.setAction("ADD_DEPARTMENT");
        log.setDetails("Added department " + name);
        auditLogRepository.save(log);
        return saved;
    }

    public List<AuditLog> listAuditLogs() { return auditLogRepository.findAll(); }

    private void logAction(Long actorUserId, String action, String details) {
        var log = new AuditLog();
        log.setActorUserId(actorUserId);
        log.setAction(action);
        log.setDetails(details);
        auditLogRepository.save(log);
    }
}
