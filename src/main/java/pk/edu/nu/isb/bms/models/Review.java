package pk.edu.nu.isb.bms.models;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "reviews")
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reviewer_user_id", nullable = false)
    private Long reviewerUserId;

    @ManyToOne
    @JoinColumn(name = "faculty_id", nullable = false)
    private FacultyEntity faculty;

    private int rating;

    @Column(columnDefinition = "text")
    private String comment;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = OffsetDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getReviewerUserId() { return reviewerUserId; }
    public void setReviewerUserId(Long reviewerUserId) { this.reviewerUserId = reviewerUserId; }
    public FacultyEntity getFaculty() { return faculty; }
    public void setFaculty(FacultyEntity faculty) { this.faculty = faculty; }
    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}

