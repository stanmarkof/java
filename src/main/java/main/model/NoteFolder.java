package main.model;

import jakarta.persistence.*;
import com.google.gson.annotations.Expose;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "note_folders")
public class NoteFolder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Expose
    private Long id;

    @Column(nullable = false)
    @Expose
    private String name;

    @Column
    @Expose
    private String description;

    @Column(name = "created_at", nullable = false)
    @Expose
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @Expose
    private User user;

    @OneToMany(mappedBy = "folder", cascade = CascadeType.ALL, orphanRemoval = true)
    @Expose(serialize = false)
    private List<Note> notes = new ArrayList<>();

    // Конструкторы
    public NoteFolder() {
        this.createdAt = LocalDateTime.now();
    }

    public NoteFolder(String name, String description, User user) {
        this.name = name;
        this.description = description;
        this.user = user;
        this.createdAt = LocalDateTime.now();
    }

    // Геттеры и сеттеры
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<Note> getNotes() {
        return notes;
    }

    public void setNotes(List<Note> notes) {
        this.notes = notes;
    }

    // Методы для работы с заметками
    public void addNote(Note note) {
        notes.add(note);
        note.setFolder(this);
    }

    public void removeNote(Note note) {
        notes.remove(note);
        note.setFolder(null);
    }
} 