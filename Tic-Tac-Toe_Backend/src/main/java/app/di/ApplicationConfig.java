package app.di;

import app.datasource.mapper.CurrentGameDataMapper;
import app.datasource.mapper.UserDataMapper;
import app.datasource.repository.CurrentGameRepository;
import app.datasource.repository.CurrentGameJpaRepository;
import app.datasource.repository.JpaCurrentGameRepository;
import app.datasource.repository.JpaUserRepository;
import app.datasource.repository.UserJpaRepository;
import app.datasource.repository.UserRepository;
import app.domain.service.AuthService;
import app.domain.service.AuthServiceImpl;
import app.domain.service.CurrentGameService;
import app.domain.service.CurrentGameServiceImpl;
import app.domain.service.UserService;
import app.domain.service.UserServiceImpl;
import app.web.mapper.CurrentGameWebMapper;
import app.web.mapper.UserWebMapper;
import app.web.filter.AuthFilter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfig {
    @Bean
    public CurrentGameDataMapper currentGameDataMapper() {
        return new CurrentGameDataMapper();
    }

    @Bean
    public CurrentGameRepository currentGameRepository(CurrentGameJpaRepository repository, CurrentGameDataMapper mapper) {
        return new JpaCurrentGameRepository(repository, mapper);
    }

    @Bean
    public CurrentGameService currentGameService(CurrentGameRepository repository) {
        return new CurrentGameServiceImpl(repository);
    }

    @Bean
    public UserDataMapper userDataMapper() {
        return new UserDataMapper();
    }

    @Bean
    public UserRepository userRepository(UserJpaRepository repository, UserDataMapper mapper) {
        return new JpaUserRepository(repository, mapper);
    }

    @Bean
    public UserService userService(UserRepository repository) {
        return new UserServiceImpl(repository);
    }

    @Bean
    public AuthService authService(UserService userService) {
        return new AuthServiceImpl(userService);
    }

    @Bean
    public AuthFilter authFilter(AuthService authService) {
        return new AuthFilter(authService);
    }

    @Bean
    public CurrentGameWebMapper currentGameWebMapper() {
        return new CurrentGameWebMapper();
    }

    @Bean
    public UserWebMapper userWebMapper() {
        return new UserWebMapper();
    }
}
