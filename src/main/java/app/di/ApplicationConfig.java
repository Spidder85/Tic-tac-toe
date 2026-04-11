package app.di;

import app.datasource.mapper.CurrentGameDataMapper;
import app.datasource.model.CurrentGameStorage;
import app.datasource.repository.CurrentGameRepository;
import app.datasource.repository.InMemoryCurrentGameRepository;
import app.domain.service.CurrentGameService;
import app.domain.service.CurrentGameServiceImpl;
import app.web.mapper.CurrentGameWebMapper;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfig {
    @Bean
    public CurrentGameStorage currentGameStorage() {
        return new CurrentGameStorage();
    }

    @Bean
    public CurrentGameDataMapper currentGameDataMapper() {
        return new CurrentGameDataMapper();
    }

    @Bean
    public CurrentGameRepository currentGameRepository(CurrentGameStorage storage, CurrentGameDataMapper mapper) {
        return new InMemoryCurrentGameRepository(storage, mapper);
    }

    @Bean
    public CurrentGameService currentGameService(CurrentGameRepository repository) {
        return new CurrentGameServiceImpl(repository);
    }

    @Bean
    public CurrentGameWebMapper currentGameWebMapper() {
        return new CurrentGameWebMapper();
    }
}
