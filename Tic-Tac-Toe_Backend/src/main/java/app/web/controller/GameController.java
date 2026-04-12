package app.web.controller;

import app.domain.model.CurrentGame;
import app.domain.service.CurrentGameService;
import app.web.mapper.CurrentGameWebMapper;
import app.web.model.CreateGameRequest;
import app.web.model.CurrentGameDto;
import app.web.filter.AuthFilter;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/game")
public class GameController {
    private final CurrentGameService currentGameService;
    private final CurrentGameWebMapper mapper;

    public GameController(CurrentGameService currentGameService, CurrentGameWebMapper mapper) {
        this.currentGameService = currentGameService;
        this.mapper = mapper;
    }

    @PostMapping
    public CurrentGameDto createGame(@RequestBody(required = false) CreateGameRequest request, HttpServletRequest servletRequest) {
        UUID userId = authenticatedUserId(servletRequest);
        boolean computerOpponent = request == null || request.isComputerOpponent();
        return mapper.toDto(currentGameService.createGame(userId, computerOpponent));
    }

    @GetMapping("/available")
    public List<CurrentGameDto> getAvailableGames() {
        return currentGameService.findAvailableGames().stream()
                .map(mapper::toDto)
                .toList();
    }

    @PostMapping("/{gameId}/join")
    public CurrentGameDto joinGame(@PathVariable UUID gameId, HttpServletRequest servletRequest) {
        try {
            return mapper.toDto(currentGameService.joinGame(gameId, authenticatedUserId(servletRequest)));
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage(), exception);
        }
    }

    @GetMapping("/{gameId}")
    public CurrentGameDto getGame(@PathVariable UUID gameId) {
        CurrentGame game = currentGameService.findById(gameId);
        if (game == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found");
        }
        return mapper.toDto(game);
    }

    @PostMapping("/{gameId}")
    public CurrentGameDto makeMove(
            @PathVariable UUID gameId,
            @RequestBody CurrentGameDto currentGameDto,
            HttpServletRequest servletRequest
    ) {
        try {
            currentGameDto.setId(gameId);
            CurrentGame updatedGame = currentGameService.getNextMove(
                    mapper.toDomain(currentGameDto),
                    authenticatedUserId(servletRequest)
            );
            return mapper.toDto(updatedGame);
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage(), exception);
        }
    }

    private UUID authenticatedUserId(HttpServletRequest request) {
        return (UUID) request.getAttribute(AuthFilter.USER_ID_ATTRIBUTE);
    }
}
