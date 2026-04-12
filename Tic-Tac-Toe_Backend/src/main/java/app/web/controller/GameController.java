package app.web.controller;

import app.domain.model.CurrentGame;
import app.domain.service.CurrentGameService;
import app.web.mapper.CurrentGameWebMapper;
import app.web.model.CurrentGameDto;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

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

    @PostMapping("/{gameId}")
    public CurrentGameDto makeMove(@PathVariable UUID gameId, @RequestBody CurrentGameDto currentGameDto) {
        try {
            currentGameDto.setId(gameId);
            CurrentGame updatedGame = currentGameService.getNextMove(mapper.toDomain(currentGameDto));
            return mapper.toDto(updatedGame);
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage(), exception);
        }
    }

}
