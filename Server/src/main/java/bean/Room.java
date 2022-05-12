package bean;

import java.net.Socket;

public class Room {
    private int roomNumber;
    private Socket user1;
    private Socket user2;

    public Room() {

    }

    public Room(int roomNumber) {
        this.roomNumber = roomNumber;
    }

    public int getRoomNumber() {
        return roomNumber;
    }

    public Socket getUser1() {
        return user1;
    }

    public Socket getUser2() {
        return user2;
    }

    public void setRoomNumber(int roomNumber) {
        this.roomNumber = roomNumber;
    }

    public void setUser1(Socket user1) {
        this.user1 = user1;
    }

    public void setUser2(Socket user2) {
        this.user2 = user2;
    }
}
