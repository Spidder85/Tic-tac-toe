package app.web.model;

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
