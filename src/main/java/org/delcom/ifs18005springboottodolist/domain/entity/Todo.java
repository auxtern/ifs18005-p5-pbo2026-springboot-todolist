package org.delcom.ifs18005springboottodolist.domain.entity;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Entity inti yang merepresentasikan satu item todo.
 * Berada di layer domain — tidak bergantung pada layer lain dan bebas dari
 * urusan tampilan maupun penyimpanan.
 */
public class Todo {
    /** ID unik todo, tidak boleh diubah setelah dibuat. */
    private final int id;

    /** Pemilik todo. */
    private final int userId;

    /** Judul tugas. */
    private String title;

    /** Deskripsi tugas. */
    private String description;

    /** Path relatif cover todo (opsional). */
    private String coverPath;

    /** Status penyelesaian: true = selesai, false = belum selesai. */
    private boolean finished;

    /** Waktu todo dibuat. */
    private final LocalDateTime createdAt;

    /** Waktu todo ditandai selesai; null jika belum selesai. */
    private LocalDateTime finishedAt;

    /** Membuat todo baru dengan status belum selesai. */
    public Todo(int id, int userId, String title) {
        this(id, userId, title, "", null, false);
    }

    /** Membuat todo dengan deskripsi. */
    public Todo(int id, int userId, String title, String description) {
        this(id, userId, title, description, null, false);
    }

    /** Membuat todo dengan status selesai yang sudah ditentukan. */
    public Todo(int id, int userId, String title, String description, String coverPath, boolean finished) {
        this(
                id,
                userId,
                title,
                description,
                coverPath,
                finished,
                LocalDateTime.now(),
                finished ? LocalDateTime.now() : null
        );
    }

    /** Membuat todo dengan waktu dibuat dan selesai yang sudah ditentukan. */
    public Todo(
            int id,
            int userId,
            String title,
            String description,
            String coverPath,
            boolean finished,
            LocalDateTime createdAt,
            LocalDateTime finishedAt
    ) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.description = description == null ? "" : description;
        this.coverPath = coverPath;
        this.finished = finished;
        this.createdAt = createdAt;
        this.finishedAt = finishedAt;
    }

    public int getId() {
        return id;
    }

    public int getUserId() {
        return userId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getCoverPath() {
        return coverPath;
    }

    public boolean isFinished() {
        return finished;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getFinishedAt() {
        return finishedAt;
    }

    /**
     * Menghitung rentang waktu dari tanggal dibuat sampai tanggal selesai.
     * Kosong jika todo belum selesai.
     */
    public Optional<Duration> duration() {
        if (finishedAt == null) {
            return Optional.empty();
        }
        return Optional.of(Duration.between(createdAt, finishedAt));
    }

    /** Mengubah judul todo. */
    public void changeTitle(String title) {
        this.title = title;
    }

    /** Mengubah deskripsi todo. */
    public void changeDescription(String description) {
        this.description = description == null ? "" : description;
    }

    /** Mengubah path cover todo. */
    public void changeCoverPath(String coverPath) {
        this.coverPath = coverPath;
    }

    /** Mengubah status selesai todo dan memperbarui tanggal selesai. */
    public void changeFinished(boolean finished) {
        this.finished = finished;
        if (finished) {
            if (this.finishedAt == null) {
                this.finishedAt = LocalDateTime.now();
            }
        } else {
            this.finishedAt = null;
        }
    }
}
