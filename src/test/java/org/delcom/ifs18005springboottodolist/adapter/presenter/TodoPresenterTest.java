package org.delcom.ifs18005springboottodolist.adapter.presenter;

import org.delcom.ifs18005springboottodolist.domain.entity.Todo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TodoPresenterTest {

    private TodoPresenter presenter;

    @BeforeEach
    void setUp() {
        presenter = new TodoPresenter();
    }

    @Test
    void formatFinishedAndUnfinishedIncludesDates() {
        LocalDateTime created = LocalDateTime.of(2026, 11, 1, 8, 0);
        Todo unfinished = new Todo(1, 1, "A", "", null, false, created, null);
        assertTrue(presenter.format(unfinished).contains("[Belum Selesai]"));
        assertTrue(presenter.format(unfinished).contains("Dibuat: 01/11/2026 08:00"));
        assertTrue(presenter.format(unfinished).contains("Selesai: -"));
        assertTrue(presenter.format(unfinished).contains("Durasi: -"));

        LocalDateTime finishedAt = LocalDateTime.of(2026, 11, 1, 10, 15);
        Todo finished = new Todo(2, 1, "B", "", null, true, created, finishedAt);
        assertTrue(presenter.format(finished).contains("[Selesai]"));
        assertTrue(presenter.format(finished).contains("Selesai: 01/11/2026 10:15"));
        assertTrue(presenter.format(finished).contains("Durasi: 2 jam 15 menit"));
    }

    @Test
    void formatDurationHandlesDaysHoursAndMinutes() {
        LocalDateTime created = LocalDateTime.of(2026, 11, 1, 8, 0);
        Todo days = new Todo(1, 1, "A", "", null, true, created, created.plusDays(1).plusHours(2).plusMinutes(5));
        assertEquals("1 hari 2 jam 5 menit", presenter.formatDuration(days));

        Todo hoursOnly = new Todo(2, 1, "B", "", null, true, created, created.plusHours(3));
        assertEquals("3 jam", presenter.formatDuration(hoursOnly));

        Todo minutesOnly = new Todo(3, 1, "C", "", null, true, created, created.plusMinutes(7));
        assertEquals("7 menit", presenter.formatDuration(minutesOnly));

        Todo zero = new Todo(4, 1, "D", "", null, true, created, created);
        assertEquals("< 1 menit", presenter.formatDuration(zero));
    }

    @Test
    void formatListEmptyAndFilled() {
        String empty = presenter.formatList(List.of(), "Header:", "- kosong");
        assertEquals("Header:\n- kosong", empty);

        Todo todo = new Todo(1, 1, "A", "", null, false, LocalDateTime.of(2026, 11, 1, 8, 0), null);
        String filled = presenter.formatList(List.of(todo), "Header:", "- kosong");
        assertTrue(filled.contains("[1] A | [Belum Selesai]"));
    }

    @Test
    void showMessages() {
        Todo todo = new Todo(1, 1, "Belajar", "", null, false, LocalDateTime.of(2026, 11, 1, 8, 0), null);
        assertTrue(presenter.showTodos(List.of()).contains("belum tersedia"));
        assertTrue(presenter.showTodos(List.of(todo)).contains("Belajar"));
        assertTrue(presenter.showSearchResults(List.of(), "x").contains("tidak ditemukan"));
        assertTrue(presenter.showSearchResults(List.of(todo), "Belajar").contains("Hasil Pencarian"));
        assertTrue(presenter.showSortedTodos(List.of(todo)).contains("Terurut"));
        assertTrue(presenter.showAddSuccess(todo).contains("Berhasil menambah"));
        assertEquals("Berhasil menghapus todo.", presenter.showRemoveSuccess());
        assertEquals("[!] Gagal menghapus todo dengan ID: 9.", presenter.showRemoveFailed(9));
        assertEquals("Berhasil mengubah todo.", presenter.showUpdateSuccess());
        assertEquals("[!] Gagal mengubah todo dengan ID: 8.", presenter.showUpdateFailed(8));
        assertEquals("[!] Pilihan tidak dimengerti.", presenter.showInvalidChoice());
        assertEquals("[!] ID tidak valid!", presenter.showInvalidId());
        assertEquals("[!] Pilihan tidak valid!", presenter.showInvalidSortOption());
        assertTrue(presenter.showInvalidFinishedStatus().contains("y/n"));
        assertEquals("[!] judul kosong", presenter.showValidationError("judul kosong"));
    }
}
