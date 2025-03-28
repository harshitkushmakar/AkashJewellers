package com.example.akashjewller;

public class UserClass {

    String name, phone_number, email, password;


    public UserClass(String name, String phone_number, String email, String password) {
        this.name = name;
        this.phone_number = phone_number;
        this.email = email;
        this.password = password;
    }

    public UserClass() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone_number() {
        return phone_number;
    }

    public void setPhone_number(String phone_number) {
        this.phone_number = phone_number;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
