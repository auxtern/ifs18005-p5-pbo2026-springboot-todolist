package org.delcom.ifs18005springboottodolist.adapter.repository;

import org.delcom.ifs18005springboottodolist.domain.entity.Role;
import org.delcom.ifs18005springboottodolist.domain.entity.Todo;
import org.delcom.ifs18005springboottodolist.domain.entity.User;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JdbcFailureTest {

    @Test
    void schemaInitializerFailsWhenConnectionBroken() throws Exception {
        DataSource dataSource = mock(DataSource.class);
        when(dataSource.getConnection()).thenThrow(new SQLException("down"));
        assertThrows(IllegalStateException.class, () -> SchemaInitializer.initialize(dataSource));
    }

    @Test
    void userRepositoryPropagatesSqlFailures() throws Exception {
        DataSource dataSource = failingDataSource();
        JdbcUserRepository repository = new JdbcUserRepository(dataSource);

        assertThrows(IllegalStateException.class, repository::findAll);
        assertThrows(IllegalStateException.class, () -> repository.findById(1));
        assertThrows(IllegalStateException.class, () -> repository.findByUsername("x"));
        assertThrows(IllegalStateException.class, () -> repository.saveNew("u", "h", "n", Role.USER));
        assertThrows(IllegalStateException.class, () -> repository.deleteById(1));
        assertThrows(IllegalStateException.class, () ->
                repository.update(new User(1, "u", "h", "n", Role.USER, null)));
    }

    @Test
    void userSaveNewFailsWhenNoGeneratedKey() throws Exception {
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        PreparedStatement statement = mock(PreparedStatement.class);
        ResultSet keys = mock(ResultSet.class);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS))).thenReturn(statement);
        when(statement.executeUpdate()).thenReturn(1);
        when(statement.getGeneratedKeys()).thenReturn(keys);
        when(keys.next()).thenReturn(false);

        JdbcUserRepository repository = new JdbcUserRepository(dataSource);
        assertThrows(IllegalStateException.class, () -> repository.saveNew("u", "h", "n", Role.USER));
    }

    @Test
    void todoRepositoryPropagatesSqlFailures() throws Exception {
        DataSource dataSource = failingDataSource();
        JdbcTodoRepository repository = new JdbcTodoRepository(dataSource);
        Todo todo = new Todo(1, 1, "t", "d", null, false, LocalDateTime.now(), null);

        assertThrows(IllegalStateException.class, repository::findAll);
        assertThrows(IllegalStateException.class, () -> repository.findByUserId(1));
        assertThrows(IllegalStateException.class, () -> repository.findById(1));
        assertThrows(IllegalStateException.class, () -> repository.save(1, "t", "d"));
        assertThrows(IllegalStateException.class, () -> repository.deleteById(1));
        assertThrows(IllegalStateException.class, () -> repository.update(todo));
    }

    @Test
    void todoSaveFailsWhenNoGeneratedKey() throws Exception {
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        PreparedStatement statement = mock(PreparedStatement.class);
        ResultSet keys = mock(ResultSet.class);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS))).thenReturn(statement);
        when(statement.executeUpdate()).thenReturn(1);
        when(statement.getGeneratedKeys()).thenReturn(keys);
        when(keys.next()).thenReturn(false);

        JdbcTodoRepository repository = new JdbcTodoRepository(dataSource);
        assertThrows(IllegalStateException.class, () -> repository.save(1, "t", "d"));
    }

    @Test
    void todoUpdateWithFinishedAtSucceedsPathAlreadyCoveredSeparately() throws Exception {
        // ensure update catch is hit even when finishedAt is non-null before SQL fails
        DataSource dataSource = failingDataSource();
        JdbcTodoRepository repository = new JdbcTodoRepository(dataSource);
        Todo todo = new Todo(1, 1, "t", "d", "c.png", true, LocalDateTime.now(), LocalDateTime.now());
        assertThrows(IllegalStateException.class, () -> repository.update(todo));
    }

    @Test
    void userFindByIdSwallowsCloseFailures() throws Exception {
        JdbcUserRepository repository = new JdbcUserRepository(queryWithCloseFailure(false));
        assertTrue(repository.findById(1).isEmpty());
    }

    @Test
    void userFindByUsernameSwallowsCloseFailures() throws Exception {
        JdbcUserRepository repository = new JdbcUserRepository(queryWithCloseFailure(false));
        assertTrue(repository.findByUsername("andi").isEmpty());
    }

    @Test
    void userFindByIdPropagatesBodyFailureDespiteCloseFailures() throws Exception {
        JdbcUserRepository repository = new JdbcUserRepository(queryWithCloseFailure(true));
        assertThrows(IllegalStateException.class, () -> repository.findById(1));
    }

    @Test
    void todoFindByIdSwallowsCloseFailures() throws Exception {
        JdbcTodoRepository repository = new JdbcTodoRepository(queryWithCloseFailure(false));
        assertTrue(repository.findById(1).isEmpty());
    }

    @Test
    void todoFindByIdPropagatesBodyFailureDespiteCloseFailures() throws Exception {
        JdbcTodoRepository repository = new JdbcTodoRepository(queryWithCloseFailure(true));
        assertThrows(IllegalStateException.class, () -> repository.findById(1));
    }

    private DataSource failingDataSource() throws SQLException {
        DataSource dataSource = mock(DataSource.class);
        when(dataSource.getConnection()).thenThrow(new SQLException("down"));
        return dataSource;
    }

    /**
     * Nested try-with-resources generates synthetic close branches. Force {@code close()}
     * failures (and optionally a body failure) so JaCoCo can see those paths.
     */
    private DataSource queryWithCloseFailure(boolean failBody) throws SQLException {
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        PreparedStatement statement = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(statement);
        when(statement.executeQuery()).thenReturn(rs);
        if (failBody) {
            when(rs.next()).thenThrow(new SQLException("body"));
        } else {
            when(rs.next()).thenReturn(false);
        }
        doThrow(new SQLException("rs-close")).when(rs).close();
        doThrow(new SQLException("ps-close")).when(statement).close();
        doThrow(new SQLException("conn-close")).when(connection).close();
        return dataSource;
    }
}
