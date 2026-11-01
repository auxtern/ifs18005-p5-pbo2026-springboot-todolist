package org.delcom.ifs18005springboottodolist.adapter.repository;

import org.delcom.ifs18005springboottodolist.domain.entity.Todo;
import org.delcom.ifs18005springboottodolist.domain.repository.ITodoRepository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Repository todo berbasis JDBC (PostgreSQL).
 */
public class JdbcTodoRepository implements ITodoRepository {
    private final DataSource dataSource;

    public JdbcTodoRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public List<Todo> findAll() {
        String sql = """
                SELECT id, user_id, title, description, cover_path, finished, created_at, finished_at
                FROM todos ORDER BY id
                """;
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            List<Todo> todos = new ArrayList<>();
            while (rs.next()) {
                todos.add(mapRow(rs));
            }
            return todos;
        } catch (SQLException ex) {
            throw new IllegalStateException("Gagal membaca daftar todo", ex);
        }
    }

    @Override
    public List<Todo> findByUserId(int userId) {
        String sql = """
                SELECT id, user_id, title, description, cover_path, finished, created_at, finished_at
                FROM todos WHERE user_id = ? ORDER BY id
                """;
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            try (ResultSet rs = statement.executeQuery()) {
                List<Todo> todos = new ArrayList<>();
                while (rs.next()) {
                    todos.add(mapRow(rs));
                }
                return todos;
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Gagal membaca todo pengguna", ex);
        }
    }

    @Override
    public Optional<Todo> findById(Integer id) {
        String sql = """
                SELECT id, user_id, title, description, cover_path, finished, created_at, finished_at
                FROM todos WHERE id = ?
                """;
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet rs = null;
        try {
            connection = dataSource.getConnection();
            statement = connection.prepareStatement(sql);
            statement.setInt(1, id);
            rs = statement.executeQuery();
            if (rs.next()) {
                return Optional.of(mapRow(rs));
            }
            return Optional.empty();
        } catch (SQLException ex) {
            throw new IllegalStateException("Gagal membaca todo", ex);
        } finally {
            closeQuietly(rs);
            closeQuietly(statement);
            closeQuietly(connection);
        }
    }

    @Override
    public Todo save(Todo entity) {
        return save(entity.getUserId(), entity.getTitle(), entity.getDescription());
    }

    @Override
    public Todo save(int userId, String title, String description) {
        String sql = """
                INSERT INTO todos (user_id, title, description, cover_path, finished, created_at, finished_at)
                VALUES (?, ?, ?, NULL, FALSE, ?, NULL)
                """;
        LocalDateTime createdAt = LocalDateTime.now();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, userId);
            statement.setString(2, title);
            statement.setString(3, description == null ? "" : description);
            statement.setTimestamp(4, Timestamp.valueOf(createdAt));
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new IllegalStateException("Gagal mendapatkan ID todo baru");
                }
                return new Todo(keys.getInt(1), userId, title, description, null, false, createdAt, null);
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Gagal menyimpan todo", ex);
        }
    }

    @Override
    public boolean deleteById(Integer id) {
        String sql = "DELETE FROM todos WHERE id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            return statement.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new IllegalStateException("Gagal menghapus todo", ex);
        }
    }

    @Override
    public void update(Todo entity) {
        String sql = """
                UPDATE todos
                SET title = ?, description = ?, cover_path = ?, finished = ?, finished_at = ?
                WHERE id = ?
                """;
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, entity.getTitle());
            statement.setString(2, entity.getDescription());
            statement.setString(3, entity.getCoverPath());
            statement.setBoolean(4, entity.isFinished());
            if (entity.getFinishedAt() == null) {
                statement.setNull(5, Types.TIMESTAMP);
            } else {
                statement.setTimestamp(5, Timestamp.valueOf(entity.getFinishedAt()));
            }
            statement.setInt(6, entity.getId());
            statement.executeUpdate();
        } catch (SQLException ex) {
            throw new IllegalStateException("Gagal mengubah todo", ex);
        }
    }

    @Override
    public List<Todo> findBy(Predicate<Todo> predicate) {
        return findAll().stream().filter(predicate).toList();
    }

    private static void closeQuietly(AutoCloseable closeable) {
        if (closeable == null) {
            return;
        }
        try {
            closeable.close();
        } catch (Exception ignored) {
            // best-effort close; primary SQLException already handled above
        }
    }

    private static Todo mapRow(ResultSet rs) throws SQLException {
        Timestamp finishedAt = rs.getTimestamp("finished_at");
        return new Todo(
                rs.getInt("id"),
                rs.getInt("user_id"),
                rs.getString("title"),
                rs.getString("description"),
                rs.getString("cover_path"),
                rs.getBoolean("finished"),
                rs.getTimestamp("created_at").toLocalDateTime(),
                finishedAt == null ? null : finishedAt.toLocalDateTime()
        );
    }
}
