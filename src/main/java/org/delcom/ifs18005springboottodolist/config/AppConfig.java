package org.delcom.ifs18005springboottodolist.config;

import org.delcom.ifs18005springboottodolist.adapter.presenter.TodoPresenter;
import org.delcom.ifs18005springboottodolist.adapter.repository.JdbcTodoRepository;
import org.delcom.ifs18005springboottodolist.adapter.repository.JdbcUserRepository;
import org.delcom.ifs18005springboottodolist.adapter.repository.SchemaInitializer;
import org.delcom.ifs18005springboottodolist.adapter.storage.FileStorage;
import org.delcom.ifs18005springboottodolist.domain.repository.ITodoRepository;
import org.delcom.ifs18005springboottodolist.domain.repository.IUserRepository;
import org.delcom.ifs18005springboottodolist.usecase.AuthUseCase;
import org.delcom.ifs18005springboottodolist.usecase.TodoUseCase;
import org.delcom.ifs18005springboottodolist.usecase.UserUseCase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.sql.DataSource;

/**
 * Composition root Spring: merakit dependency antar-layer.
 */
@Configuration
public class AppConfig {

    @Bean
    public IUserRepository userRepository(DataSource dataSource) {
        SchemaInitializer.initialize(dataSource);
        return new JdbcUserRepository(dataSource);
    }

    @Bean
    public ITodoRepository todoRepository(DataSource dataSource) {
        return new JdbcTodoRepository(dataSource);
    }

    @Bean
    public TodoUseCase todoUseCase(ITodoRepository todoRepository) {
        return new TodoUseCase(todoRepository);
    }

    @Bean
    public AuthUseCase authUseCase(IUserRepository userRepository) {
        return new AuthUseCase(userRepository);
    }

    @Bean
    public UserUseCase userUseCase(IUserRepository userRepository) {
        return new UserUseCase(userRepository);
    }

    @Bean
    public TodoPresenter todoPresenter() {
        return new TodoPresenter();
    }

    @Bean
    public FileStorage fileStorage(@Value("${app.upload-dir:uploads}") String uploadDir) {
        return new FileStorage(uploadDir);
    }

    @Bean
    public ApplicationRunner seedRootAdmin(
            AuthUseCase authUseCase,
            @Value("${app.root-username:admin}") String rootUsername,
            @Value("${app.root-password:admin}") String rootPassword
    ) {
        return args -> authUseCase.ensureRootAdmin(rootUsername, rootPassword);
    }

    @Bean
    public WebMvcConfigurer uploadResourceConfigurer(FileStorage fileStorage) {
        return new WebMvcConfigurer() {
            @Override
            public void addResourceHandlers(ResourceHandlerRegistry registry) {
                String location = fileStorage.root().toUri().toString();
                if (!location.endsWith("/")) {
                    location = location + "/";
                }
                registry.addResourceHandler("/uploads/**").addResourceLocations(location);
            }
        };
    }
}
