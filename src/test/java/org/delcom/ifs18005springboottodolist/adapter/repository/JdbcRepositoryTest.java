package org.delcom.ifs18005springboottodolist.adapter.repository;

import org.delcom.ifs18005springboottodolist.domain.entity.Role;
import org.delcom.ifs18005springboottodolist.domain.entity.Todo;
import org.delcom.ifs18005springboottodolist.domain.entity.User;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JdbcRepositoryTest {

    private JdbcUserRepository userRepository;
    private JdbcTodoRepository todoRepository;

    @BeforeEach
    void setUp() {
        DataSource dataSource = createDataSource();
        SchemaInitializer.initialize(dataSource);
        userRepository = new JdbcUserRepository(dataSource);
        todoRepository = new JdbcTodoRepository(dataSource);
    }

    private DataSource createDataSource() {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:jdbc_repo_" + System.nanoTime() + ";MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE");
        dataSource.setUser("sa");
        dataSource.setPassword("");
        return dataSource;
    }

    @Test
    void userCrudAndFinders() {
        User saved = userRepository.saveNew("andi", "hash", "Andi", Role.USER);
        assertTrue(saved.getId() > 0);

        assertTrue(userRepository.findByUsername("ANDI").isPresent());
        assertEquals(1, userRepository.findAll().size());
        assertTrue(userRepository.findById(saved.getId()).isPresent());
        assertTrue(userRepository.findBy(user -> user.getUsername().equals("andi")).size() == 1);

        saved.changeFullName("Andi Baru");
        saved.changePhotoPath("profiles/a.png");
        userRepository.update(saved);
        assertEquals("Andi Baru", userRepository.findById(saved.getId()).orElseThrow().getFullName());

        User viaSave = userRepository.save(new User(0, "budi", "h", "Budi", Role.USER, null));
        assertTrue(viaSave.getId() > 0);

        assertTrue(userRepository.deleteById(viaSave.getId()));
        assertFalse(userRepository.deleteById(viaSave.getId()));
        assertTrue(userRepository.findByUsername("tidakada").isEmpty());
        assertTrue(userRepository.findById(999).isEmpty());
    }

    @Test
    void todoCrudAndFinders() {
        User owner = userRepository.saveNew("owner", "hash", "Owner", Role.USER);
        Todo created = todoRepository.save(owner.getId(), "Judul", "Deskripsi");
        assertEquals("Deskripsi", created.getDescription());
        Todo nullDesc = todoRepository.save(owner.getId(), "NullDesc", null);
        assertEquals("", nullDesc.getDescription());

        Todo viaEntity = todoRepository.save(new Todo(0, owner.getId(), "Lain", "D"));
        assertTrue(viaEntity.getId() > 0);

        assertEquals(3, todoRepository.findAll().size());
        assertEquals(3, todoRepository.findByUserId(owner.getId()).size());
        assertTrue(todoRepository.findById(created.getId()).isPresent());
        assertTrue(todoRepository.findById(999).isEmpty());
        assertEquals(1, todoRepository.findBy(todo -> todo.getTitle().equals("Judul")).size());

        created.changeTitle("Judul Baru");
        created.changeDescription("Desc");
        created.changeCoverPath("covers/x.png");
        created.changeFinished(true);
        todoRepository.update(created);
        Todo updated = todoRepository.findById(created.getId()).orElseThrow();
        assertEquals("Judul Baru", updated.getTitle());
        assertEquals("covers/x.png", updated.getCoverPath());
        assertTrue(updated.isFinished());

        updated.changeFinished(false);
        todoRepository.update(updated);
        assertFalse(todoRepository.findById(created.getId()).orElseThrow().isFinished());

        assertTrue(todoRepository.deleteById(created.getId()));
        assertFalse(todoRepository.deleteById(created.getId()));
        List<Todo> remaining = todoRepository.findByUserId(owner.getId());
        assertEquals(2, remaining.size());
    }
}
