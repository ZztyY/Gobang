package bean;

import java.io.Serializable;

public class ChessData implements Serializable {
    private int roomNumber;
    private int[] chess;
    private boolean offense;

    public ChessData(int roomNumber, int[] chess, boolean offense) {
        this.roomNumber = roomNumber;
        this.chess = chess;
        this.offense = offense;
    }

    public void setRoomNumber(int roomNumber) {
        this.roomNumber = roomNumber;
    }

    public void setChess(int[] chess) {
        this.chess = chess;
    }

    public void setOffense(boolean offense) {
        this.offense = offense;
    }

    public int getRoomNumber() {
        return roomNumber;
    }

    public boolean isOffense() {
        return offense;
    }

    public int[] getChess() {
        return chess;
    }
}
