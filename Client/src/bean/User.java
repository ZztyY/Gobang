package bean;

import java.io.Serializable;

public class User implements Serializable {
    private int id;
    private String username;
    private String password;
    private int wins;

    public User() {

    }

    public User(int id, String username) {
        this.id = id;
        this.username = username;
    }

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public User(int id, String username, String password, int wins) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.wins = wins;
    }

    public int getId() {
        return id;
    }

    public int getWins() {
        return wins;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setWins(int wins) {
        this.wins = wins;
    }
}
