package org.delcom.ifs18005springboottodolist.config;

import org.delcom.ifs18005springboottodolist.adapter.presenter.TodoPresenter;
import org.delcom.ifs18005springboottodolist.adapter.storage.FileStorage;
import org.delcom.ifs18005springboottodolist.domain.repository.ITodoRepository;
import org.delcom.ifs18005springboottodolist.domain.repository.IUserRepository;
import org.delcom.ifs18005springboottodolist.usecase.AuthUseCase;
import org.delcom.ifs18005springboottodolist.usecase.TodoUseCase;
import org.delcom.ifs18005springboottodolist.usecase.UserUseCase;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;

import javax.sql.DataSource;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AppConfigTest {

    @TempDir
    Path tempDir;

    @Test
    void wiresBeans() throws Exception {
        AppConfig config = new AppConfig();
        DataSource dataSource = createDataSource();

        IUserRepository userRepository = config.userRepository(dataSource);
        ITodoRepository todoRepository = config.todoRepository(dataSource);
        TodoUseCase todoUseCase = config.todoUseCase(todoRepository);
        AuthUseCase authUseCase = config.authUseCase(userRepository);
        UserUseCase userUseCase = config.userUseCase(userRepository);
        TodoPresenter presenter = config.todoPresenter();
        FileStorage fileStorage = config.fileStorage(tempDir.toString());

        assertNotNull(userRepository);
        assertNotNull(todoRepository);
        assertNotNull(todoUseCase);
        assertNotNull(authUseCase);
        assertNotNull(userUseCase);
        assertNotNull(presenter);
        assertNotNull(fileStorage);
        assertInstanceOf(ITodoRepository.class, todoRepository);

        config.seedRootAdmin(authUseCase, "admin", "admin")
                .run(new DefaultApplicationArguments());

        ResourceHandlerRegistry registry = mock(ResourceHandlerRegistry.class);
        var registration = mock(org.springframework.web.servlet.config.annotation.ResourceHandlerRegistration.class);
        when(registry.addResourceHandler(anyString())).thenReturn(registration);
        when(registration.addResourceLocations(anyString())).thenReturn(registration);
        config.uploadResourceConfigurer(fileStorage).addResourceHandlers(registry);
        verify(registry).addResourceHandler("/uploads/**");
        verify(registration).addResourceLocations(anyString());
    }

    private DataSource createDataSource() {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:appconfig_" + System.nanoTime() + ";MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE");
        dataSource.setUser("sa");
        dataSource.setPassword("");
        return dataSource;
    }
}
