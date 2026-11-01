package org.delcom.ifs18005springboottodolist.domain.entity;

import java.util.Comparator;

/**
 * Kriteria pengurutan todo.
 * Setiap opsi membawa comparator-nya sendiri sehingga logika pengurutan
 * terpusat di domain, bukan tersebar sebagai angka ajaib di view/use case.
 */
public enum SortOption {
    /** Urutkan judul dari A ke Z (case-insensitive). */
    TITLE_ASC(Comparator.comparing(Todo::getTitle, String.CASE_INSENSITIVE_ORDER)),

    /** Urutkan judul dari Z ke A (case-insensitive). */
    TITLE_DESC(Comparator.comparing(Todo::getTitle, String.CASE_INSENSITIVE_ORDER).reversed()),

    /** Tampilkan todo selesai terlebih dahulu. */
    FINISHED_FIRST(Comparator.comparing(Todo::isFinished).reversed()),

    /** Tampilkan todo belum selesai terlebih dahulu. */
    UNFINISHED_FIRST(Comparator.comparing(Todo::isFinished));

    /** Comparator yang digunakan untuk mengurutkan daftar todo. */
    private final Comparator<Todo> comparator;

    SortOption(Comparator<Todo> comparator) {
        this.comparator = comparator;
    }

    /** Mengembalikan comparator yang sesuai dengan opsi ini. */
    public Comparator<Todo> comparator() {
        return comparator;
    }
}
