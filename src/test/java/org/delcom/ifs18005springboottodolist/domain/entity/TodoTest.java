package org.delcom.ifs18005springboottodolist.domain.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TodoTest {

    @Test
    void constructorDefaultsFinishedToFalse() {
        Todo todo = new Todo(1, 10, "Belajar Spring Boot");

        assertEquals(1, todo.getId());
        assertEquals(10, todo.getUserId());
        assertEquals("Belajar Spring Boot", todo.getTitle());
        assertEquals("", todo.getDescription());
        assertNull(todo.getCoverPath());
        assertFalse(todo.isFinished());
        assertNotNull(todo.getCreatedAt());
        assertNull(todo.getFinishedAt());
        assertTrue(todo.duration().isEmpty());
    }

    @Test
    void constructorWithDescriptionAndFinishedFlag() {
        Todo withDesc = new Todo(2, 10, "Judul", "Deskripsi");
        assertEquals("Deskripsi", withDesc.getDescription());

        LocalDateTime created = LocalDateTime.of(2026, 11, 1, 8, 0);
        Todo nullDescription = new Todo(5, 10, "Judul", null, null, false, created, null);
        assertEquals("", nullDescription.getDescription());

        Todo todo = new Todo(3, 10, "Selesai", "Desc", "covers/a.png", true);
        assertTrue(todo.isFinished());
        assertEquals("covers/a.png", todo.getCoverPath());
        assertNotNull(todo.getFinishedAt());
        assertTrue(todo.duration().isPresent());
    }

    @Test
    void changeTitleDescriptionCoverAndFinished() {
        LocalDateTime created = LocalDateTime.of(2026, 11, 1, 8, 0);
        Todo todo = new Todo(3, 10, "Lama", "Awal", null, false, created, null);

        todo.changeTitle("Baru");
        todo.changeDescription("Baru deskripsi");
        todo.changeCoverPath("covers/x.png");
        todo.changeFinished(true);

        assertEquals("Baru", todo.getTitle());
        assertEquals("Baru deskripsi", todo.getDescription());
        assertEquals("covers/x.png", todo.getCoverPath());
        assertTrue(todo.isFinished());
        assertNotNull(todo.getFinishedAt());
        assertTrue(todo.duration().isPresent());

        LocalDateTime firstFinishedAt = todo.getFinishedAt();
        todo.changeFinished(true);
        assertEquals(firstFinishedAt, todo.getFinishedAt());

        todo.changeDescription(null);
        assertEquals("", todo.getDescription());

        todo.changeFinished(false);
        assertFalse(todo.isFinished());
        assertNull(todo.getFinishedAt());
        assertTrue(todo.duration().isEmpty());
    }

    @Test
    void durationUsesCreatedAndFinishedAt() {
        LocalDateTime created = LocalDateTime.of(2026, 11, 1, 8, 0);
        LocalDateTime finished = LocalDateTime.of(2026, 11, 1, 10, 30);
        Todo todo = new Todo(4, 10, "Durasi", "", null, true, created, finished);

        assertEquals(150, todo.duration().orElseThrow().toMinutes());
    }
}
