package models;

import java.sql.*;

public class User {
    private int id;
    private String username;
    private String password;
    private int wins;

    public User() {

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

    public static void createUser(String username, String password) throws SQLException {
        Connection conn = DBUtil.getConn();
        String sql = "insert into user" + "(username, password, wins) " +
                "values(?, ?, 0)";
        PreparedStatement preSt = conn.prepareStatement(sql);
        preSt.setString(1, username);
        preSt.setString(2, password);
        preSt.execute();
    }

    public static User findUser(String username, String password) throws SQLException {
        User user = new User();
        Connection conn = DBUtil.getConn();
        String sql = "select * from user " + "where username=? and password=?";
        PreparedStatement preSt = conn.prepareStatement(sql);
        preSt.setString(1, username);
        preSt.setString(2, password);
        ResultSet res = preSt.executeQuery();
        if (res.next()) {
            user.setId(res.getInt("id"));
            user.setUsername(res.getString("username"));
            user.setPassword(res.getString("password"));
            user.setWins(res.getInt("wins"));
        } else {
            user = null;
        }
        return user;
    }

    public static User findUserByName(String username) throws SQLException {
        User user = new User();
        Connection conn = DBUtil.getConn();
        String sql = "select * from user " + "where username=?";
        PreparedStatement preSt = conn.prepareStatement(sql);
        preSt.setString(1, username);
        ResultSet res = preSt.executeQuery();
        if (res.next()) {
            user.setId(res.getInt("id"));
            user.setUsername(res.getString("username"));
            user.setPassword(res.getString("password"));
            user.setWins(res.getInt("wins"));
        } else {
            user = null;
        }
        return user;
    }

    public static void updateUser(User user) throws SQLException {
        Connection conn = DBUtil.getConn();
        String sql = "update user set username=?,password=?,wins=? where id=?";
        PreparedStatement preSt = conn.prepareStatement(sql);
        preSt.setString(1, user.getUsername());
        preSt.setString(2, user.getPassword());
        preSt.setInt(3, user.getWins());
        preSt.setInt(4, user.getId());
        preSt.execute();
    }

    public static void main(String[] args) throws SQLException {

    }
}

