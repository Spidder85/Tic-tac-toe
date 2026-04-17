# Реализация backend-ТЗ

Документ идет по порядку `tasks/backend_1.md`: что требовалось, что добавлялось, что менялось, что выводилось из работы, и какие классы/методы при этом появились. Порядок выбран так, чтобы при повторной реализации проект оставался компилируемым.

## 1. Добавление базы данных

### 1.1. Gradle: зависимости для БД и Security

**ТЗ:** `Задание 1. Добавление базы данных`, `Задание 2. Добавление авторизации`.

**Зачем:** без этих зависимостей не скомпилируются `@Entity`, `CrudRepository`, `SecurityFilterChain`, `GenericFilterBean`, PostgreSQL connection.

Файл: `build.gradle.kts`

Добавлено:

```kotlin
dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")
    runtimeOnly("org.postgresql:postgresql")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}
```

Заменено:

```kotlin
java {
    sourceCompatibility = JavaVersion.VERSION_18
    targetCompatibility = JavaVersion.VERSION_18
}
```

Добавлено в компиляцию:

```kotlin
tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.isFork = false
    options.release.set(18)
}
```

Что изменилось:

- `spring-boot-starter-data-jpa` дал Hibernate/JPA/`CrudRepository`.
- `spring-boot-starter-security` дал security chain и фильтры.
- `postgresql` дал JDBC-драйвер.
- Toolchain заменен на `options.release.set(18)`, чтобы сохранить Java 18 и избежать проблемы Gradle worker daemon в текущем окружении.

### 1.2. Настройки PostgreSQL

**ТЗ:** описать подключение к PostgreSQL в `application.properties`.

Файл создан: `src/main/resources/application.properties`

Код:

```properties
spring.datasource.url=${DB_URL:jdbc:postgresql://localhost:5432/tictactoe}
spring.datasource.username=${DB_USERNAME:postgres}
spring.datasource.password=${DB_PASSWORD:postgres}
spring.datasource.driver-class-name=org.postgresql.Driver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.open-in-view=false
```

Что это значит:

- База должна называться `tictactoe`.
- Базу создает разработчик вручную.
- Таблицы создает Hibernate при старте приложения.

Команда для БД:

```sql
CREATE DATABASE tictactoe WITH ENCODING 'UTF8';
```

### 1.3. JPA-модель поля игры

**ТЗ:** добавить специальные аннотации для классов, которые сохраняются в БД.

Файл изменен: `src/main/java/app/datasource/model/GameFieldData.java`

Было: обычный класс с `int[][] cells`.

Стало:

```java
@Embeddable
public class GameFieldData {
    @Column(name = "cells", nullable = false, length = 32)
    private String cells;

    protected GameFieldData() {
    }

    public GameFieldData(int[][] cells) {
        this.cells = serialize(cells);
    }

    public int[][] getCells() {
        return deserialize(cells);
    }

    private static String serialize(int[][] source) {
        StringBuilder builder = new StringBuilder(9);
        for (int[] row : source) {
            for (int cell : row) {
                builder.append(cell);
            }
        }
        return builder.toString();
    }

    private static int[][] deserialize(String source) {
        if (source == null || source.length() != 9) {
            return new int[3][3];
        }
        int[][] target = new int[3][3];
        for (int index = 0; index < source.length(); index++) {
            target[index / 3][index % 3] = Character.digit(source.charAt(index), 10);
        }
        return target;
    }
}
```

Зачем:

- JPA плохо хранит `int[][]` как простое поле.
- Поле 3x3 хранится строкой из 9 цифр: `000000000`.

### 1.4. JPA-модель текущей игры

**ТЗ:** сохранять текущую игру в БД, добавить состояния и игроков.

Файл изменен: `src/main/java/app/datasource/model/CurrentGameData.java`

Было: обычный класс с `gameId` и `gameField`.

Стало:

```java
@Entity
@Table(name = "current_games")
public class CurrentGameData {
    @Id
    private UUID gameId;

    @Embedded
    private GameFieldData gameField;

    private UUID firstPlayerId;
    private UUID secondPlayerId;
    private UUID currentTurnPlayerId;
    private UUID winnerPlayerId;

    @Enumerated(EnumType.STRING)
    private GameStatus status;

    private boolean computerOpponent;
}
```

Добавлены конструкторы: protected no-args для JPA, старый совместимый `(UUID, GameFieldData)`, и полный конструктор со всеми полями.

Добавлены getters:

```java
getGameId()
getGameField()
getFirstPlayerId()
getSecondPlayerId()
getCurrentTurnPlayerId()
getWinnerPlayerId()
getStatus()
isComputerOpponent()
```

### 1.5. Репозиторий игры через `CrudRepository`

**ТЗ:** использовать у репозиториев `CrudRepository`.

Файл добавлен: `src/main/java/app/datasource/repository/CurrentGameJpaRepository.java`

```java
public interface CurrentGameJpaRepository extends CrudRepository<CurrentGameData, UUID> {
    List<CurrentGameData> findByStatusAndComputerOpponent(GameStatus status, boolean computerOpponent);
}
```

Файл добавлен: `src/main/java/app/datasource/repository/JpaCurrentGameRepository.java`

```java
public class JpaCurrentGameRepository implements CurrentGameRepository {
    private final CurrentGameJpaRepository repository;
    private final CurrentGameDataMapper mapper;

    public JpaCurrentGameRepository(CurrentGameJpaRepository repository, CurrentGameDataMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public CurrentGame save(CurrentGame currentGame) {
        return mapper.toDomain(repository.save(mapper.toData(currentGame)));
    }

    @Override
    public CurrentGame findById(UUID gameId) {
        return repository.findById(gameId)
                .map(mapper::toDomain)
                .orElse(null);
    }

    @Override
    public List<CurrentGame> findWaitingGames() {
        return repository.findByStatusAndComputerOpponent(GameStatus.WAITING_FOR_PLAYERS, false).stream()
                .map(mapper::toDomain)
                .toList();
    }
}
```

Файл изменен: `src/main/java/app/datasource/repository/CurrentGameRepository.java`

Добавлен метод:

```java
List<CurrentGame> findWaitingGames();
```

Чтобы старый `InMemoryCurrentGameRepository` продолжал компилироваться, в него добавлен такой же метод:

```java
@Override
public List<CurrentGame> findWaitingGames() {
    return storage.findAll().stream()
            .filter(game -> game.getStatus() == GameStatus.WAITING_FOR_PLAYERS)
            .map(mapper::toDomain)
            .collect(Collectors.toList());
}
```

И в `CurrentGameStorage` добавлен метод:

```java
public Collection<CurrentGameData> findAll() {
    return games.values();
}
```

### 1.6. Вывод in-memory storage из активной работы

**ТЗ:** избавиться от класса-хранилища.

Файл изменен: `src/main/java/app/di/ApplicationConfig.java`

Было активное in-memory подключение:

```java
@Bean
public CurrentGameStorage currentGameStorage() {
    return new CurrentGameStorage();
}

@Bean
public CurrentGameRepository currentGameRepository(CurrentGameStorage storage, CurrentGameDataMapper mapper) {
    return new InMemoryCurrentGameRepository(storage, mapper);
}
```

Стало JPA-подключение:

```java
@Bean
public CurrentGameRepository currentGameRepository(CurrentGameJpaRepository repository, CurrentGameDataMapper mapper) {
    return new JpaCurrentGameRepository(repository, mapper);
}
```

Важно:

- `CurrentGameStorage` и `InMemoryCurrentGameRepository` физически остались, но больше не используются в DI.
- Активное хранение теперь идет через PostgreSQL.

## 2. Добавление авторизации

### 2.1. Domain-модель пользователя

**ТЗ:** добавить пользователей с UUID, логином, паролем.

Файл добавлен: `src/main/java/app/domain/model/User.java`

```java
public class User {
    private final UUID id;
    private final String login;
    private final String password;

    public User(UUID id, String login, String password) {
        this.id = id;
        this.login = login;
        this.password = password;
    }

    public UUID getId() {
        return id;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }
}
```

### 2.2. JPA-модель пользователя

Файл добавлен: `src/main/java/app/datasource/model/UserData.java`

```java
@Entity
@Table(name = "users")
public class UserData {
    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private String login;

    @Column(nullable = false)
    private String password;

    protected UserData() {
    }

    public UserData(UUID id, String login, String password) {
        this.id = id;
        this.login = login;
        this.password = password;
    }
}
```

Добавлены getters:

```java
getId()
getLogin()
getPassword()
```

### 2.3. Репозитории пользователя

**ТЗ:** реализовать поддержку пользователей на всех слоях.

Файл добавлен: `src/main/java/app/datasource/repository/UserRepository.java`

```java
public interface UserRepository {
    User save(User user);

    User findById(UUID id);

    User findByLogin(String login);

    boolean existsByLogin(String login);
}
```

Файл добавлен: `src/main/java/app/datasource/repository/UserJpaRepository.java`

```java
public interface UserJpaRepository extends CrudRepository<UserData, UUID> {
    Optional<UserData> findByLogin(String login);

    boolean existsByLogin(String login);
}
```

Файл добавлен: `src/main/java/app/datasource/repository/JpaUserRepository.java`

```java
public class JpaUserRepository implements UserRepository {
    private final UserJpaRepository repository;
    private final UserDataMapper mapper;

    public JpaUserRepository(UserJpaRepository repository, UserDataMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public User save(User user) {
        return mapper.toDomain(repository.save(mapper.toData(user)));
    }

    @Override
    public User findById(UUID id) {
        return repository.findById(id)
                .map(mapper::toDomain)
                .orElse(null);
    }

    @Override
    public User findByLogin(String login) {
        return repository.findByLogin(login)
                .map(mapper::toDomain)
                .orElse(null);
    }

    @Override
    public boolean existsByLogin(String login) {
        return repository.existsByLogin(login);
    }
}
```

### 2.4. Маппер пользователя

Файл добавлен: `src/main/java/app/datasource/mapper/UserDataMapper.java`

```java
public class UserDataMapper {
    public UserData toData(User user) {
        return new UserData(user.getId(), user.getLogin(), user.getPassword());
    }

    public User toDomain(UserData userData) {
        if (userData == null) {
            return null;
        }
        return new User(userData.getId(), userData.getLogin(), userData.getPassword());
    }
}
```

### 2.5. `SignUpRequest`

**ТЗ:** создать модель `SignUpRequest` с логином и паролем.

Файл добавлен: `src/main/java/app/web/model/SignUpRequest.java`

```java
public class SignUpRequest {
    private String login;
    private String password;

    public SignUpRequest() {
    }

    public SignUpRequest(String login, String password) {
        this.login = login;
        this.password = password;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
```

### 2.6. `UserService` и `UserServiceImpl`

Файл добавлен: `src/main/java/app/domain/service/UserService.java`

```java
public interface UserService {
    User register(String login, String password);

    User findById(UUID id);

    User findByLogin(String login);

    boolean hasCredentials(String login, String password);
}
```

Файл добавлен: `src/main/java/app/domain/service/UserServiceImpl.java`

Ключевые методы:

```java
@Override
public User register(String login, String password) {
    validateCredentials(login, password);
    if (repository.existsByLogin(login)) {
        throw new IllegalArgumentException("Login is already used");
    }
    return repository.save(new User(UUID.randomUUID(), login, password));
}

@Override
public boolean hasCredentials(String login, String password) {
    User user = repository.findByLogin(login);
    return user != null && user.getPassword().equals(password);
}

private void validateCredentials(String login, String password) {
    if (login == null || login.isBlank()) {
        throw new IllegalArgumentException("Login is required");
    }
    if (password == null || password.isBlank()) {
        throw new IllegalArgumentException("Password is required");
    }
}
```

### 2.7. `AuthService` и `AuthServiceImpl`

**ТЗ:** создать сервис авторизации, который использует `UserService`.

Файл добавлен: `src/main/java/app/domain/service/AuthService.java`

```java
public interface AuthService {
    boolean signUp(SignUpRequest request);

    UUID signIn(String authorizationHeader);

    UUID authenticate(String authorizationHeader);
}
```

Файл добавлен: `src/main/java/app/domain/service/AuthServiceImpl.java`

Ключевые методы:

```java
@Override
public boolean signUp(SignUpRequest request) {
    if (request == null) {
        throw new IllegalArgumentException("Sign up request is required");
    }
    userService.register(request.getLogin(), request.getPassword());
    return true;
}

@Override
public UUID signIn(String authorizationHeader) {
    return authenticate(authorizationHeader);
}

@Override
public UUID authenticate(String authorizationHeader) {
    Credentials credentials = parseAuthorizationHeader(authorizationHeader);
    User user = userService.findByLogin(credentials.login());
    if (user == null || !user.getPassword().equals(credentials.password())) {
        throw new IllegalArgumentException("Invalid login or password");
    }
    return user.getId();
}
```

Разбор Basic Auth:

```java
private Credentials parseAuthorizationHeader(String authorizationHeader) {
    if (authorizationHeader == null || !authorizationHeader.startsWith(BASIC_PREFIX)) {
        throw new IllegalArgumentException("Basic authorization header is required");
    }
    String encodedCredentials = authorizationHeader.substring(BASIC_PREFIX.length());
    String decodedCredentials = new String(Base64.getDecoder().decode(encodedCredentials), StandardCharsets.UTF_8);
    int delimiterIndex = decodedCredentials.indexOf(':');
    if (delimiterIndex <= 0 || delimiterIndex == decodedCredentials.length() - 1) {
        throw new IllegalArgumentException("Authorization header must contain login and password");
    }
    return new Credentials(
            decodedCredentials.substring(0, delimiterIndex),
            decodedCredentials.substring(delimiterIndex + 1)
    );
}
```

### 2.8. `AuthController`

**ТЗ:** создать контроллер авторизации с endpoint'ами регистрации и авторизации.

Файл добавлен: `src/main/java/app/web/controller/AuthController.java`

```java
@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/signup")
    public boolean signUp(@RequestBody SignUpRequest request) {
        try {
            return authService.signUp(request);
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage(), exception);
        }
    }

    @PostMapping("/signin")
    public UUID signIn(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader) {
        try {
            return authService.signIn(authorizationHeader);
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, exception.getMessage(), exception);
        }
    }
}
```

Endpoint'ы:

```text
POST /auth/signup
POST /auth/signin
```

### 2.9. `AuthFilter`

**ТЗ:** создать `AuthFilter`, наследоваться от `GenericFilterBean`, реализовать `doFilter`.

Файл добавлен: `src/main/java/app/web/filter/AuthFilter.java`

```java
public class AuthFilter extends GenericFilterBean {
    public static final String USER_ID_ATTRIBUTE = "authenticatedUserId";

    private final AuthService authService;

    public AuthFilter(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        if (isPublicEndpoint(httpRequest)) {
            chain.doFilter(request, response);
            return;
        }

        try {
            UUID userId = authService.authenticate(httpRequest.getHeader(HttpHeaders.AUTHORIZATION));
            httpRequest.setAttribute(USER_ID_ATTRIBUTE, userId);
            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(userId, null, List.of())
            );
            chain.doFilter(request, response);
        } catch (IllegalArgumentException exception) {
            SecurityContextHolder.clearContext();
            httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, exception.getMessage());
        }
    }

    private boolean isPublicEndpoint(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.equals("/auth/signup")
                || path.equals("/auth/signin");
    }
}
```

### 2.10. `SecurityConfig`

**ТЗ:** создать Spring Configuration с `SecurityFilterChain`.

Файл добавлен: `src/main/java/app/di/SecurityConfig.java`

```java
@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity, AuthFilter authFilter) throws Exception {
        return httpSecurity
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(requests -> requests
                        .requestMatchers("/auth/signup", "/auth/signin").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(authFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
```

## 3. Логика игры между двумя игроками

### 3.1. Статусы игры

**ТЗ:** добавить состояния текущей игры.

Файл добавлен: `src/main/java/app/domain/model/GameStatus.java`

```java
public enum GameStatus {
    WAITING_FOR_PLAYERS,
    TURN,
    DRAW,
    WIN
}
```

### 3.2. Символы игроков

**ТЗ:** добавить информацию о значках, которыми ходят пользователи.

Файл: `src/main/java/app/domain/service/CurrentGameServiceImpl.java`

Используются константы:

```java
private static final int EMPTY = 0;
private static final int USER = 1;
private static final int COMPUTER = 2;
```

Правило:

- первый игрок - `1`;
- второй игрок - `2`;
- компьютер - `2`;
- пустая клетка - `0`.

Файл изменен: `src/main/java/app/domain/model/GameField.java`

Было:

```java
this.cells[i][j] = -1;
```

Стало:

```java
this.cells[i][j] = 0;
```

### 3.3. Расширение domain-модели `CurrentGame`

Файл изменен: `src/main/java/app/domain/model/CurrentGame.java`

Добавлены поля:

```java
private final UUID firstPlayerId;
private final UUID secondPlayerId;
private final UUID currentTurnPlayerId;
private final UUID winnerPlayerId;
private final GameStatus status;
private final boolean computerOpponent;
```

Добавлен полный конструктор:

```java
public CurrentGame(
        UUID id,
        GameField gameField,
        UUID firstPlayerId,
        UUID secondPlayerId,
        UUID currentTurnPlayerId,
        UUID winnerPlayerId,
        GameStatus status,
        boolean computerOpponent
) {
    this.id = id;
    this.gameField = gameField;
    this.firstPlayerId = firstPlayerId;
    this.secondPlayerId = secondPlayerId;
    this.currentTurnPlayerId = currentTurnPlayerId;
    this.winnerPlayerId = winnerPlayerId;
    this.status = status;
    this.computerOpponent = computerOpponent;
}
```

Старый конструктор сохранен:

```java
public CurrentGame(UUID id, GameField gameField) {
    this(id, gameField, null, null, null, null, GameStatus.WAITING_FOR_PLAYERS, true);
}
```

Добавлены getters:

```java
getFirstPlayerId()
getSecondPlayerId()
getCurrentTurnPlayerId()
getWinnerPlayerId()
getStatus()
isComputerOpponent()
```

### 3.4. Расширение `CurrentGameService`

Файл изменен: `src/main/java/app/domain/service/CurrentGameService.java`

Добавлены методы:

```java
CurrentGame getNextMove(CurrentGame currentGame, UUID playerId);

CurrentGame createGame(UUID ownerId, boolean computerOpponent);

List<CurrentGame> findAvailableGames();

CurrentGame joinGame(UUID gameId, UUID playerId);

CurrentGame findById(UUID gameId);
```

### 3.5. Создание игры

Файл изменен: `src/main/java/app/domain/service/CurrentGameServiceImpl.java`

Добавлен метод:

```java
@Override
public CurrentGame createGame(UUID ownerId, boolean computerOpponent) {
    GameStatus status = computerOpponent ? GameStatus.TURN : GameStatus.WAITING_FOR_PLAYERS;
    UUID currentTurnPlayerId = computerOpponent ? ownerId : null;
    return repository.save(new CurrentGame(
            UUID.randomUUID(),
            new GameField(),
            ownerId,
            null,
            currentTurnPlayerId,
            null,
            status,
            computerOpponent
    ));
}
```

### 3.6. Доступные игры

Добавлен метод:

```java
@Override
public List<CurrentGame> findAvailableGames() {
    return repository.findWaitingGames();
}
```

### 3.7. Присоединение к игре

Добавлен метод:

```java
@Override
public CurrentGame joinGame(UUID gameId, UUID playerId) {
    CurrentGame game = repository.findById(gameId);
    if (game == null) {
        throw new IllegalArgumentException("Game not found");
    }
    if (game.isComputerOpponent()) {
        throw new IllegalArgumentException("Computer game cannot be joined");
    }
    if (game.getStatus() != GameStatus.WAITING_FOR_PLAYERS || game.getSecondPlayerId() != null) {
        throw new IllegalArgumentException("Game is not available");
    }
    if (playerId.equals(game.getFirstPlayerId())) {
        throw new IllegalArgumentException("Game owner cannot join the same game");
    }
    return repository.save(new CurrentGame(
            game.getId(),
            game.getGameField(),
            game.getFirstPlayerId(),
            playerId,
            game.getFirstPlayerId(),
            null,
            GameStatus.TURN,
            false
    ));
}
```

### 3.8. Определение конца игры

Добавлен метод:

```java
private GameStatus resolveStatus(int[][] field) {
    if (hasWinner(field, USER) || hasWinner(field, COMPUTER)) {
        return GameStatus.WIN;
    }
    if (isBoardFull(field)) {
        return GameStatus.DRAW;
    }
    return GameStatus.TURN;
}
```

### 3.9. Проверка хода конкретного игрока

Добавлен метод:

```java
private int getPlayerSymbol(CurrentGame currentGame, UUID playerId) {
    if (playerId == null) {
        throw new IllegalArgumentException("Player is required");
    }
    if (playerId.equals(currentGame.getFirstPlayerId())) {
        return USER;
    }
    if (!currentGame.isComputerOpponent() && playerId.equals(currentGame.getSecondPlayerId())) {
        return COMPUTER;
    }
    throw new IllegalArgumentException("Player is not a participant of this game");
}
```

Изменен метод хода: добавлен вариант с `UUID playerId`.

```java
@Override
public CurrentGame getNextMove(CurrentGame currentGame, UUID playerId) {
    CurrentGame savedGame = repository.findById(currentGame.getId());
    if (savedGame == null) {
        throw new IllegalArgumentException("Game not found");
    }
    if (savedGame.getStatus() != GameStatus.TURN) {
        throw new IllegalArgumentException("Game is not active");
    }
    if (!playerId.equals(savedGame.getCurrentTurnPlayerId())) {
        throw new IllegalArgumentException("It is not this player's turn");
    }

    CurrentGame gameForValidation = new CurrentGame(
            currentGame.getId(),
            currentGame.getGameField(),
            savedGame.getFirstPlayerId(),
            savedGame.getSecondPlayerId(),
            savedGame.getCurrentTurnPlayerId(),
            savedGame.getWinnerPlayerId(),
            savedGame.getStatus(),
            savedGame.isComputerOpponent()
    );
    validateGameField(gameForValidation, savedGame, playerId);

    int[][] field = currentGame.getGameField().getCells();
    int playerSymbol = getPlayerSymbol(savedGame, playerId);
    GameStatus status = resolveStatus(field);
    UUID winnerPlayerId = status == GameStatus.WIN ? playerId : null;
    UUID nextTurnPlayerId = null;

    if (status == GameStatus.TURN && savedGame.isComputerOpponent()) {
        int[] bestMove = findBestMove(field);
        if (bestMove[0] != -1) {
            field[bestMove[0]][bestMove[1]] = COMPUTER;
        }
        status = resolveStatus(field);
        winnerPlayerId = status == GameStatus.WIN ? null : winnerPlayerId;
        nextTurnPlayerId = status == GameStatus.TURN ? savedGame.getFirstPlayerId() : null;
    } else if (status == GameStatus.TURN) {
        nextTurnPlayerId = playerSymbol == USER ? savedGame.getSecondPlayerId() : savedGame.getFirstPlayerId();
    }

    return repository.save(new CurrentGame(
            savedGame.getId(),
            new GameField(field),
            savedGame.getFirstPlayerId(),
            savedGame.getSecondPlayerId(),
            nextTurnPlayerId,
            winnerPlayerId,
            status,
            savedGame.isComputerOpponent()
    ));
}
```

### 3.10. Web-модель создания игры

Файл добавлен: `src/main/java/app/web/model/CreateGameRequest.java`

```java
public class CreateGameRequest {
    private boolean computerOpponent;

    public CreateGameRequest() {
    }

    public boolean isComputerOpponent() {
        return computerOpponent;
    }

    public void setComputerOpponent(boolean computerOpponent) {
        this.computerOpponent = computerOpponent;
    }
}
```

### 3.11. Расширение `CurrentGameDto`

Файл изменен: `src/main/java/app/web/model/CurrentGameDto.java`

Добавлены поля:

```java
private UUID firstPlayerId;
private UUID secondPlayerId;
private UUID currentTurnPlayerId;
private UUID winnerPlayerId;
private String status;
private boolean computerOpponent;
```

Также добавлены constructor, getters, setters для этих полей.

### 3.12. Изменение `CurrentGameDataMapper`

Файл изменен: `src/main/java/app/datasource/mapper/CurrentGameDataMapper.java`

Было:

```java
return new CurrentGameData(
        currentGame.getId(),
        new GameFieldData(currentGame.getGameField().getCells())
);
```

Стало:

```java
return new CurrentGameData(
        currentGame.getId(),
        new GameFieldData(currentGame.getGameField().getCells()),
        currentGame.getFirstPlayerId(),
        currentGame.getSecondPlayerId(),
        currentGame.getCurrentTurnPlayerId(),
        currentGame.getWinnerPlayerId(),
        currentGame.getStatus(),
        currentGame.isComputerOpponent()
);
```

Обратный маппинг стал:

```java
return new CurrentGame(
        currentGameData.getGameId(),
        new GameField(currentGameData.getGameField().getCells()),
        currentGameData.getFirstPlayerId(),
        currentGameData.getSecondPlayerId(),
        currentGameData.getCurrentTurnPlayerId(),
        currentGameData.getWinnerPlayerId(),
        currentGameData.getStatus(),
        currentGameData.isComputerOpponent()
);
```

### 3.13. Изменение `CurrentGameWebMapper`

Файл изменен: `src/main/java/app/web/mapper/CurrentGameWebMapper.java`

В `toDomain` добавлено:

```java
return new CurrentGame(
        currentGameDto.getId(),
        new GameField(currentGameDto.getGameField().getCells()),
        currentGameDto.getFirstPlayerId(),
        currentGameDto.getSecondPlayerId(),
        currentGameDto.getCurrentTurnPlayerId(),
        currentGameDto.getWinnerPlayerId(),
        toStatus(currentGameDto.getStatus()),
        currentGameDto.isComputerOpponent()
);
```

В `toDto` добавлено:

```java
return new CurrentGameDto(
        currentGame.getId(),
        new GameFieldDto(currentGame.getGameField().getCells()),
        currentGame.getFirstPlayerId(),
        currentGame.getSecondPlayerId(),
        currentGame.getCurrentTurnPlayerId(),
        currentGame.getWinnerPlayerId(),
        currentGame.getStatus().name(),
        currentGame.isComputerOpponent()
);
```

Добавлен helper:

```java
private GameStatus toStatus(String status) {
    if (status == null || status.isBlank()) {
        return null;
    }
    return GameStatus.valueOf(status);
}
```

### 3.14. Endpoint'ы игры

Файл изменен: `src/main/java/app/web/controller/GameController.java`

Добавлен endpoint создания:

```java
@PostMapping
public CurrentGameDto createGame(@RequestBody(required = false) CreateGameRequest request, HttpServletRequest servletRequest) {
    UUID userId = authenticatedUserId(servletRequest);
    boolean computerOpponent = request == null || request.isComputerOpponent();
    return mapper.toDto(currentGameService.createGame(userId, computerOpponent));
}
```

Добавлен endpoint доступных игр:

```java
@GetMapping("/available")
public List<CurrentGameDto> getAvailableGames() {
    return currentGameService.findAvailableGames().stream()
            .map(mapper::toDto)
            .toList();
}
```

Добавлен endpoint присоединения:

```java
@PostMapping("/{gameId}/join")
public CurrentGameDto joinGame(@PathVariable UUID gameId, HttpServletRequest servletRequest) {
    try {
        return mapper.toDto(currentGameService.joinGame(gameId, authenticatedUserId(servletRequest)));
    } catch (IllegalArgumentException exception) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage(), exception);
    }
}
```

Добавлен endpoint получения игры:

```java
@GetMapping("/{gameId}")
public CurrentGameDto getGame(@PathVariable UUID gameId) {
    CurrentGame game = currentGameService.findById(gameId);
    if (game == null) {
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found");
    }
    return mapper.toDto(game);
}
```

Изменен endpoint хода.

Было:

```java
CurrentGame updatedGame = currentGameService.getNextMove(mapper.toDomain(currentGameDto));
```

Стало:

```java
CurrentGame updatedGame = currentGameService.getNextMove(
        mapper.toDomain(currentGameDto),
        authenticatedUserId(servletRequest)
);
```

Добавлен helper:

```java
private UUID authenticatedUserId(HttpServletRequest request) {
    return (UUID) request.getAttribute(AuthFilter.USER_ID_ATTRIBUTE);
}
```

## 4. Endpoint получения пользователя

### Пункт ТЗ

`Задание 3. Добавить endpoint для получения информации о пользователе по UUID`.

### 4.1. `UserDto`

Файл добавлен: `src/main/java/app/web/model/UserDto.java`

```java
public class UserDto {
    private UUID id;
    private String login;
}
```

Также добавлены constructor, getters, setters.

### 4.2. `UserWebMapper`

Файл добавлен: `src/main/java/app/web/mapper/UserWebMapper.java`

```java
public class UserWebMapper {
    public UserDto toDto(User user) {
        return new UserDto(user.getId(), user.getLogin());
    }
}
```

### 4.3. `UserController`

Файл добавлен: `src/main/java/app/web/controller/UserController.java`

```java
@RestController
@RequestMapping("/user")
public class UserController {
    private final UserService userService;
    private final UserWebMapper mapper;

    public UserController(UserService userService, UserWebMapper mapper) {
        this.userService = userService;
        this.mapper = mapper;
    }

    @GetMapping("/{userId}")
    public UserDto getUser(@PathVariable UUID userId) {
        User user = userService.findById(userId);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        return mapper.toDto(user);
    }
}
```

Endpoint:

```text
GET /user/{userId}
```

Пароль наружу не возвращается.

## 5. Итоговая DI-конфигурация

Файл изменен: `src/main/java/app/di/ApplicationConfig.java`

В итоге в DI добавлены bean'ы:

```java
@Bean
public CurrentGameDataMapper currentGameDataMapper()

@Bean
public CurrentGameRepository currentGameRepository(CurrentGameJpaRepository repository, CurrentGameDataMapper mapper)

@Bean
public CurrentGameService currentGameService(CurrentGameRepository repository)

@Bean
public UserDataMapper userDataMapper()

@Bean
public UserRepository userRepository(UserJpaRepository repository, UserDataMapper mapper)

@Bean
public UserService userService(UserRepository repository)

@Bean
public AuthService authService(UserService userService)

@Bean
public AuthFilter authFilter(AuthService authService)

@Bean
public CurrentGameWebMapper currentGameWebMapper()

@Bean
public UserWebMapper userWebMapper()
```

Главная замена:

```java
return new JpaCurrentGameRepository(repository, mapper);
```

вместо:

```java
return new InMemoryCurrentGameRepository(storage, mapper);
```

## 6. Отдельный frontend для проверки

Это не часть backend-ТЗ.

Сначала frontend временно был в `src/main/resources/static`, но потом был убран, чтобы backend оставался чистым API.

Добавлена отдельная папка:

```text
front/index.html
front/server.js
front/README.md
```

Запуск:

```powershell
node front/server.js
```

Открыть:

```text
http://localhost:3000
```

`server.js` нужен, чтобы проксировать `/api/*` на backend и не добавлять CORS-настройки в backend ради тестового frontend.

## 7. Проверка

Сборка:

```powershell
./gradlew.bat build --no-daemon
```

Ожидаемый результат:

```text
BUILD SUCCESSFUL
```

Проверка таблиц:

```powershell
psql -U postgres -h localhost -p 5432 -d tictactoe -c "\dt"
```

Ожидаемые таблицы:

```text
current_games
users
```
