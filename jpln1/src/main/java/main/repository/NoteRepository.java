package main.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import main.model.Note;

public class NoteRepository {
    private final Connection connection;

    public NoteRepository(Connection connection) {
        this.connection = connection;
        createTableIfNotExists();
    }

    private void createTableIfNotExists() {
        String sql = """
            CREATE TABLE IF NOT EXISTS notes (
                id INT AUTO_INCREMENT PRIMARY KEY,
                title VARCHAR(255) NOT NULL,
                content TEXT NOT NULL,
                user_id INT NOT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                FOREIGN KEY (user_id) REFERENCES users(id)
            )
        """;
        try (Statement stmt = connection.createStatement()) {
            System.out.println("Попытка создания таблицы notes...");
            stmt.execute(sql);
            System.out.println("Таблица notes успешно создана или уже существует");
        } catch (SQLException e) {
            System.err.println("Ошибка при создании таблицы notes: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void addNote(Note note) throws SQLException {
        // Создаём таблицу, если её нет
        createTableIfNotExists();
        
        String sql = "INSERT INTO notes (title, content, user_id) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            System.out.println("Попытка добавления заметки:");
            System.out.println("Title: " + note.getTitle());
            System.out.println("Content: " + note.getContent());
            System.out.println("User ID: " + note.getUserId());
            
            stmt.setString(1, note.getTitle());
            stmt.setString(2, note.getContent());
            stmt.setLong(3, note.getUserId());
            int result = stmt.executeUpdate();
            System.out.println("Заметка добавлена успешно. Затронуто строк: " + result);
        } catch (SQLException e) {
            System.err.println("Ошибка при добавлении заметки: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    public void updateNote(Note note) throws SQLException {
        String sql = "UPDATE notes SET title = ?, content = ? WHERE id = ? AND user_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, note.getTitle());
            stmt.setString(2, note.getContent());
            stmt.setInt(3, note.getId());
            stmt.setLong(4, note.getUserId());
            stmt.executeUpdate();
        }
    }

    public void deleteNoteById(int id) throws SQLException {
        String sql = "DELETE FROM notes WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    public List<Note> getNotesByUserId(Long userId) throws SQLException {
        List<Note> notes = new ArrayList<>();
        String sql = "SELECT * FROM notes WHERE user_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Note note = new Note();
                note.setId(rs.getInt("id"));
                note.setTitle(rs.getString("title"));
                note.setContent(rs.getString("content"));
                note.setUserId(rs.getLong("user_id"));
                notes.add(note);
            }
        }
        return notes;
    }
}

