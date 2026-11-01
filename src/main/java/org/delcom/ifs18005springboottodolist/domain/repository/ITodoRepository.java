package org.delcom.ifs18005springboottodolist.domain.repository;

import org.delcom.ifs18005springboottodolist.domain.entity.Todo;

import java.util.List;

/**
 * Port spesifik Todo yang memperluas generic {@link IRepository}.
 * Menambahkan method pembuatan Todo baru dengan penomoran ID otomatis.
 */
public interface ITodoRepository extends IRepository<Todo, Integer> {
    /**
     * Menyimpan todo baru untuk pengguna tertentu.
     */
    Todo save(int userId, String title, String description);

    /** Mengambil semua todo milik satu pengguna. */
    List<Todo> findByUserId(int userId);
}
