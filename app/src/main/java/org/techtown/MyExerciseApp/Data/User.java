package org.techtown.MyExerciseApp.Data;

import java.io.Serializable;

public class User implements Serializable {
    private String uid;
    private String username;
    private String email;
    private String password;
    private String phoneNumber;
    private String profileImage;
    private String nickname;
    private String creationTime;
    private String description;
    public User() {

    }

    public User(String uid, String username, String email, String password, String phoneNumber, String profileImage, String nickname, String creationTime, String description) {
        this.uid = uid;
        this.username = username;
        this.email = email;
        this.password = password;
        this.phoneNumber = phoneNumber;
        this.profileImage = profileImage;
        this.nickname = nickname;
        this.creationTime = creationTime;
        this.description = description;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(String creationTime) {
        this.creationTime = creationTime;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    public void copyUser(User user) {
        this.uid = user.getUid();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.password = user.getPassword();
        this.phoneNumber = user.phoneNumber;
        this.profileImage = user.profileImage;
        this.nickname = user.getNickname();
        this.creationTime = user.getCreationTime();
        this.description = user.getDescription();
    }

}
