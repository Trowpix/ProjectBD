package com.example.bdsqltester.dtos;

import java.sql.ResultSet;
import java.sql.SQLException;

public class User {
    public String id;
    public String username;
    public String password;
    public String role;

    public User(){
        this.id = null;
        this.username = null;
        this.password = null;
        this.role = null;
    }
    public User(String id, String name, String password, String role) {
        this.id = id;
        this.username = name;
        this.password = password;
        this.role = role;
    }

    public User(ResultSet rs) throws SQLException {
        this.id = rs.getString("login_id");
        this.password = rs.getString("password");
        this.role = rs.getString("role");
    }
}
