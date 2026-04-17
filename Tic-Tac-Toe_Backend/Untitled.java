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