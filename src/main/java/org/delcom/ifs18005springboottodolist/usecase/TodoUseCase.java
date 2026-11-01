package org.delcom.ifs18005springboottodolist.usecase;

import org.delcom.ifs18005springboottodolist.domain.entity.SortOption;
import org.delcom.ifs18005springboottodolist.domain.entity.Todo;
import org.delcom.ifs18005springboottodolist.domain.exception.EntityNotFoundException;
import org.delcom.ifs18005springboottodolist.domain.exception.ValidationException;
import org.delcom.ifs18005springboottodolist.domain.repository.ITodoRepository;

import java.util.List;
import java.util.Optional;

/**
 * Use case yang menangani logika bisnis aplikasi todo.
 * Tidak melakukan I/O — hanya memproses data dan melempar checked exception
 * domain jika operasi tidak valid atau data tidak ditemukan.
 */
public class TodoUseCase {
    /** Port repository yang di-inject dari luar (Dependency Inversion). */
    private final ITodoRepository todoRepository;

    public TodoUseCase(ITodoRepository todoRepository) {
        this.todoRepository = todoRepository;
    }

    /** Mengambil semua todo milik satu pengguna. */
    public List<Todo> getTodosByUser(int userId) {
        return todoRepository.findByUserId(userId);
    }

    /** Mengambil semua todo yang tersedia (admin). */
    public List<Todo> getAllTodos() {
        return todoRepository.findAll();
    }

    /** Menambahkan todo baru setelah validasi. */
    public Todo addTodo(int userId, String title, String description) throws ValidationException {
        if (title == null || title.trim().isEmpty()) {
            throw new ValidationException("Judul todo tidak boleh kosong");
        }
        String safeDescription = description == null ? "" : description.trim();
        return todoRepository.save(userId, title.trim(), safeDescription);
    }

    /** Menghapus todo berdasarkan ID; melempar exception jika tidak ditemukan. */
    public void removeTodo(int id) throws EntityNotFoundException {
        boolean deleted = todoRepository.deleteById(id);
        if (!deleted) {
            throw new EntityNotFoundException(id);
        }
    }

    /**
     * Menghapus todo milik pengguna tertentu.
     */
    public void removeTodoForUser(int userId, int id) throws EntityNotFoundException, ValidationException {
        Todo todo = requireOwnedTodo(userId, id);
        removeTodo(todo.getId());
    }

    /**
     * Mengubah judul, deskripsi, cover, dan/atau status selesai todo.
     * Parameter {@code null} berarti field tersebut tidak diubah
     * (kecuali {@code clearCover=true} untuk menghapus cover).
     */
    public void updateTodo(
            int id,
            String title,
            String description,
            Boolean finished,
            String coverPath,
            boolean clearCover
    ) throws EntityNotFoundException, ValidationException {
        Optional<Todo> found = todoRepository.findById(id);
        if (found.isEmpty()) {
            throw new EntityNotFoundException(id);
        }

        Todo todo = found.get();
        applyUpdates(todo, title, description, finished, coverPath, clearCover);
        todoRepository.update(todo);
    }

    public void updateTodoForUser(
            int userId,
            int id,
            String title,
            String description,
            Boolean finished,
            String coverPath,
            boolean clearCover
    ) throws EntityNotFoundException, ValidationException {
        Todo todo = requireOwnedTodo(userId, id);
        applyUpdates(todo, title, description, finished, coverPath, clearCover);
        todoRepository.update(todo);
    }

    /** Mencari todo pengguna yang judulnya mengandung kata kunci (case-insensitive). */
    public List<Todo> searchTodos(int userId, String keyword) {
        String lowerKeyword = keyword.toLowerCase();
        return todoRepository.findByUserId(userId).stream()
                .filter(todo -> todo.getTitle().toLowerCase().contains(lowerKeyword))
                .toList();
    }

    /** Mengurutkan todo pengguna sesuai kriteria {@link SortOption} yang dipilih. */
    public List<Todo> sortTodos(int userId, SortOption option) {
        return todoRepository.findByUserId(userId).stream()
                .sorted(option.comparator())
                .toList();
    }

    private Todo requireOwnedTodo(int userId, int id) throws EntityNotFoundException, ValidationException {
        Optional<Todo> found = todoRepository.findById(id);
        if (found.isEmpty()) {
            throw new EntityNotFoundException(id);
        }
        Todo todo = found.get();
        if (todo.getUserId() != userId) {
            throw new ValidationException("Todo tidak milik pengguna ini");
        }
        return todo;
    }

    private void applyUpdates(
            Todo todo,
            String title,
            String description,
            Boolean finished,
            String coverPath,
            boolean clearCover
    ) throws ValidationException {
        if (title != null) {
            if (title.trim().isEmpty()) {
                throw new ValidationException("Judul todo tidak boleh kosong");
            }
            todo.changeTitle(title.trim());
        }
        if (description != null) {
            todo.changeDescription(description.trim());
        }
        if (finished != null) {
            todo.changeFinished(finished);
        }
        if (clearCover) {
            todo.changeCoverPath(null);
        } else if (coverPath != null) {
            todo.changeCoverPath(coverPath);
        }
    }
}
