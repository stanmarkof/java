package main.model;

import jakarta.persistence.*;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import java.time.LocalDateTime;

@Entity
@Table(name = "notes")
public class Note {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Expose
    private Long id;

    @Column(nullable = false)
    @Expose
    private String title;

    @Column(columnDefinition = "TEXT")
    @Expose
    private String content;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @Expose(serialize = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "folder_id")
    @Expose(serialize = false)
    private NoteFolder folder;

    @Column(name = "folder_id", insertable = false, updatable = false)
    @Expose
    @SerializedName("folderId")
    private Long folderId;

    @Column(name = "created_at", nullable = false)
    @Expose
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @Expose
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Note() {
    }

    public Note(String title, String content, User user) {
        this.title = title;
        this.content = content;
        this.user = user;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public NoteFolder getFolder() {
        return folder;
    }

    public void setFolder(NoteFolder folder) {
        this.folder = folder;
        this.folderId = folder != null ? folder.getId() : null;
    }

    public Long getFolderId() {
        return folderId;
    }

    public void setFolderId(Long folderId) {
        this.folderId = folderId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
