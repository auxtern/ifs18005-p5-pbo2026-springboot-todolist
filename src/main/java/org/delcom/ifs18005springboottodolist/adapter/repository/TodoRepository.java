package org.delcom.ifs18005springboottodolist.adapter.repository;

import org.delcom.ifs18005springboottodolist.domain.entity.Todo;
import org.delcom.ifs18005springboottodolist.domain.repository.ITodoRepository;

import java.util.List;

/**
 * Implementasi repository todo menggunakan basis generic {@link InMemoryRepository}.
 * Dipakai pada unit test; produksi memakai {@link JdbcTodoRepository}.
 */
public class TodoRepository extends InMemoryRepository<Todo, Integer> implements ITodoRepository {
    /** Penghitung ID otomatis, bertambah setiap kali todo baru disimpan. */
    private int idCounter = 0;

    public TodoRepository() {
        super(Todo::getId);
    }

    @Override
    public Todo save(int userId, String title, String description) {
        Todo todo = new Todo(nextId(), userId, title, description);
        return save(todo);
    }

    @Override
    public List<Todo> findByUserId(int userId) {
        return findBy(todo -> todo.getUserId() == userId);
    }

    /** Menghasilkan ID unik berikutnya. */
    private int nextId() {
        return ++idCounter;
    }
}
