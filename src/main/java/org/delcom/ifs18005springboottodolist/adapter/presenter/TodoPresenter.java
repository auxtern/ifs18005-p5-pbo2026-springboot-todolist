package org.delcom.ifs18005springboottodolist.adapter.presenter;

import org.delcom.ifs18005springboottodolist.domain.entity.Todo;

import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Presenter yang memformat data dari use case menjadi teks siap tampil.
 * Memisahkan logika tampilan dari entity, use case, dan view sehingga
 * format output bisa diubah tanpa menyentuh domain.
 */
public class TodoPresenter {

    private static final DateTimeFormatter DATE_TIME_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /** Memformat satu todo menjadi baris teks untuk ditampilkan. */
    public String format(Todo todo) {
        String status = todo.isFinished() ? "[Selesai]" : "[Belum Selesai]";
        return String.format(
                "[%d] %s | %s | Dibuat: %s | Selesai: %s | Durasi: %s",
                todo.getId(),
                todo.getTitle(),
                status,
                formatCreatedAt(todo),
                formatFinishedAt(todo),
                formatDuration(todo)
        );
    }

    /** Memformat tanggal dibuat. */
    public String formatCreatedAt(Todo todo) {
        return todo.getCreatedAt().format(DATE_TIME_FORMAT);
    }

    /** Memformat tanggal selesai; "-" jika belum selesai. */
    public String formatFinishedAt(Todo todo) {
        if (todo.getFinishedAt() == null) {
            return "-";
        }
        return todo.getFinishedAt().format(DATE_TIME_FORMAT);
    }

    /** Memformat rentang waktu dibuat → selesai. */
    public String formatDuration(Todo todo) {
        return todo.duration()
                .map(this::formatDurationValue)
                .orElse("-");
    }

    private String formatDurationValue(Duration duration) {
        long totalMinutes = Math.max(duration.toMinutes(), 0);
        if (totalMinutes < 1) {
            return "< 1 menit";
        }

        long days = totalMinutes / (24 * 60);
        long hours = (totalMinutes % (24 * 60)) / 60;
        long minutes = totalMinutes % 60;

        StringBuilder builder = new StringBuilder();
        if (days > 0) {
            builder.append(days).append(" hari");
        }
        if (hours > 0) {
            if (!builder.isEmpty()) {
                builder.append(' ');
            }
            builder.append(hours).append(" jam");
        }
        if (minutes > 0) {
            if (!builder.isEmpty()) {
                builder.append(' ');
            }
            builder.append(minutes).append(" menit");
        }
        return builder.toString();
    }

    /**
     * Memformat daftar todo menjadi teks multi-baris.
     * Mengembalikan pesan kosong jika list tidak berisi data.
     */
    public String formatList(List<Todo> todos, String header, String emptyMessage) {
        StringBuilder builder = new StringBuilder(header);
        if (todos.isEmpty()) {
            builder.append('\n').append(emptyMessage);
            return builder.toString();
        }
        String body = todos.stream()
                .map(this::format)
                .collect(Collectors.joining("\n"));
        builder.append('\n').append(body);
        return builder.toString();
    }

    /** Memformat daftar semua todo. */
    public String showTodos(List<Todo> todos) {
        return formatList(todos, "Daftar Todo:", "- Data todo belum tersedia!");
    }

    /** Memformat hasil pencarian berdasarkan kata kunci. */
    public String showSearchResults(List<Todo> todos, String keyword) {
        return formatList(todos, "Hasil Pencarian: \"" + keyword + "\"", "- Todo tidak ditemukan!");
    }

    /** Memformat daftar todo yang sudah diurutkan. */
    public String showSortedTodos(List<Todo> todos) {
        return formatList(todos, "Daftar Todo (Terurut):", "- Data todo belum tersedia!");
    }

    /** Pesan sukses setelah menambah todo. */
    public String showAddSuccess(Todo todo) {
        return "Berhasil menambah todo: " + format(todo);
    }

    /** Pesan sukses setelah menghapus todo. */
    public String showRemoveSuccess() {
        return "Berhasil menghapus todo.";
    }

    /** Pesan gagal saat menghapus todo. */
    public String showRemoveFailed(int id) {
        return String.format("[!] Gagal menghapus todo dengan ID: %d.", id);
    }

    /** Pesan sukses setelah mengubah todo. */
    public String showUpdateSuccess() {
        return "Berhasil mengubah todo.";
    }

    /** Pesan gagal saat mengubah todo. */
    public String showUpdateFailed(int id) {
        return String.format("[!] Gagal mengubah todo dengan ID: %d.", id);
    }

    /** Pesan saat pilihan tidak dikenali. */
    public String showInvalidChoice() {
        return "[!] Pilihan tidak dimengerti.";
    }

    /** Pesan saat ID yang dimasukkan tidak valid. */
    public String showInvalidId() {
        return "[!] ID tidak valid!";
    }

    /** Pesan saat opsi pengurutan tidak valid. */
    public String showInvalidSortOption() {
        return "[!] Pilihan tidak valid!";
    }

    /** Pesan saat input status selesai tidak valid. */
    public String showInvalidFinishedStatus() {
        return "[!] Pilihan status selesai tidak valid (gunakan y/n)!";
    }

    /** Pesan validasi umum dari domain. */
    public String showValidationError(String message) {
        return "[!] " + message;
    }
}
