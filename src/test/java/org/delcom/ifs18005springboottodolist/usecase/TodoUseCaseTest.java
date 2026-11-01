package org.delcom.ifs18005springboottodolist.usecase;

import org.delcom.ifs18005springboottodolist.adapter.repository.TodoRepository;
import org.delcom.ifs18005springboottodolist.domain.entity.SortOption;
import org.delcom.ifs18005springboottodolist.domain.entity.Todo;
import org.delcom.ifs18005springboottodolist.domain.exception.EntityNotFoundException;
import org.delcom.ifs18005springboottodolist.domain.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TodoUseCaseTest {

    private static final int USER_A = 1;
    private static final int USER_B = 2;

    private TodoUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new TodoUseCase(new TodoRepository());
    }

    @Test
    void addAndGetTodosByUser() throws ValidationException {
        Todo todo = useCase.addTodo(USER_A, "Belajar Generic", "Desc");
        assertEquals(1, todo.getId());
        assertEquals(USER_A, todo.getUserId());
        assertEquals(1, useCase.getTodosByUser(USER_A).size());
        assertTrue(useCase.getTodosByUser(USER_B).isEmpty());
        assertEquals(1, useCase.getAllTodos().size());
    }

    @Test
    void addTodoRejectsBlankTitle() throws Exception {
        assertThrows(ValidationException.class, () -> useCase.addTodo(USER_A, " ", "x"));
        assertThrows(ValidationException.class, () -> useCase.addTodo(USER_A, null, null));
        Todo withNullDescription = useCase.addTodo(USER_A, "Tanpa deskripsi", null);
        assertEquals("", withNullDescription.getDescription());
    }

    @Test
    void removeTodoSuccessAndNotFound() throws Exception {
        Todo todo = useCase.addTodo(USER_A, "Hapus", "");
        useCase.removeTodoForUser(USER_A, todo.getId());
        assertTrue(useCase.getTodosByUser(USER_A).isEmpty());

        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class, () -> useCase.removeTodo(99));
        assertEquals(99, ex.getId());
    }

    @Test
    void removeTodoRejectsOtherOwner() throws Exception {
        Todo todo = useCase.addTodo(USER_A, "Milik A", "");
        assertThrows(ValidationException.class, () -> useCase.removeTodoForUser(USER_B, todo.getId()));
    }

    @Test
    void updateTodoPartialAndValidation() throws Exception {
        Todo todo = useCase.addTodo(USER_A, "Lama", "Awal");

        useCase.updateTodoForUser(USER_A, todo.getId(), "Baru", "Desc baru", true, "covers/a.png", false);
        Todo updated = useCase.getTodosByUser(USER_A).get(0);
        assertEquals("Baru", updated.getTitle());
        assertEquals("Desc baru", updated.getDescription());
        assertEquals("covers/a.png", updated.getCoverPath());
        assertTrue(updated.isFinished());

        useCase.updateTodoForUser(USER_A, todo.getId(), null, null, false, null, true);
        assertFalse(useCase.getTodosByUser(USER_A).get(0).isFinished());
        assertNull(useCase.getTodosByUser(USER_A).get(0).getCoverPath());

        useCase.updateTodo(todo.getId(), "Hanya Judul", null, null, null, false);
        assertEquals("Hanya Judul", useCase.getTodosByUser(USER_A).get(0).getTitle());

        useCase.updateTodo(todo.getId(), null, null, null, null, false);
        assertEquals("Hanya Judul", useCase.getTodosByUser(USER_A).get(0).getTitle());

        assertThrows(ValidationException.class, () -> useCase.updateTodo(todo.getId(), "  ", null, null, null, false));
        assertThrows(EntityNotFoundException.class, () -> useCase.updateTodo(999, "X", null, null, null, false));
        assertThrows(ValidationException.class, () ->
                useCase.updateTodoForUser(USER_B, todo.getId(), "X", null, null, null, false));
        assertThrows(EntityNotFoundException.class, () ->
                useCase.updateTodoForUser(USER_A, 999, "X", null, null, null, false));
        assertThrows(EntityNotFoundException.class, () ->
                useCase.removeTodoForUser(USER_A, 999));
    }

    @Test
    void searchAndSortTodos() throws Exception {
        useCase.addTodo(USER_A, "Alpha Task", "");
        Todo beta = useCase.addTodo(USER_A, "Beta Task", "");
        useCase.updateTodoForUser(USER_A, beta.getId(), null, null, true, null, false);

        List<Todo> search = useCase.searchTodos(USER_A, "alpha");
        assertEquals(1, search.size());
        assertEquals("Alpha Task", search.get(0).getTitle());

        List<Todo> titleAsc = useCase.sortTodos(USER_A, SortOption.TITLE_ASC);
        assertEquals("Alpha Task", titleAsc.get(0).getTitle());

        List<Todo> finishedFirst = useCase.sortTodos(USER_A, SortOption.FINISHED_FIRST);
        assertTrue(finishedFirst.get(0).isFinished());
    }
}
