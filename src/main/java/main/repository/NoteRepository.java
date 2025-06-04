package main.repository;

import main.model.Note;
import main.model.User;
import main.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;
import java.util.Optional;

public class NoteRepository {
    
    public List<Note> findAllByUser(User user) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Note> query = session.createQuery("FROM Note WHERE user = :user", Note.class);
            query.setParameter("user", user);
            return query.list();
        }
    }

    public Optional<Note> findById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Note note = session.get(Note.class, id);
            return Optional.ofNullable(note);
        }
    }

    public List<Note> findByUserId(Long userId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Note n WHERE n.user.id = :userId", Note.class)
                    .setParameter("userId", userId)
                    .list();
        }
    }

    public void save(Note note) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            session.persist(note);
            transaction.commit();
        }
    }

    public void update(Note note) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            session.merge(note);
            transaction.commit();
        }
    }

    public void delete(Note note) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            session.remove(note);
            transaction.commit();
        }
    }

    public List<Note> findByTitleContaining(String title, Long userId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Note n WHERE n.title LIKE :title AND n.user.id = :userId", Note.class)
                    .setParameter("title", "%" + title + "%")
                    .setParameter("userId", userId)
                    .list();
        }
    }
}

