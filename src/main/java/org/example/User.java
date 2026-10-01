package org.example;

abstract public class User {
    private String userName;
    private String password;
    private String userId;
    private String userPhoneNumber;

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserName() {
        return this.userName;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPassword() {
        return this.password;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return this.userId;
    }

    public void setUserPhoneNumber(String userPhoneNumber) {
        this.userPhoneNumber = userPhoneNumber;
    }

    public String getUserPhoneNumber() {
        return this.userPhoneNumber;
    }

    abstract public boolean login(User user) ;

    public void logout() {
        System.out.println("感谢您的使用");
    }

    abstract public void changePassword();
}

