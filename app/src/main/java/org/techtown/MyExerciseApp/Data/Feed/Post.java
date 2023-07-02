package org.techtown.MyExerciseApp.Data.Feed;

import java.io.Serializable;
import java.util.List;

public class Post implements Serializable {
    private String postId;
    private String uid;
    private String postNickname;
    private String content;
    private List<String> postImage;
    private String creationDate;
    private String routine;

    public Post(String postId,String uid, String postNickname, String content, List<String> postImage, String creationDate, String routine) {
        this.postId = postId;
        this.uid = uid;
        this.postNickname = postNickname;
        this.content = content;
        this.postImage = postImage;
        this.creationDate = creationDate;
        this.routine = routine;
    }
    public Post() {

    }

    public String getPostId() {//getTodayTimeOnlyNumber
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getPostNickname() {
        return postNickname;
    }

    public void setPostNickname(String postNickname) {
        this.postNickname = postNickname;
    }



    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<String> getPostImage() {
        return postImage;
    }

    public void setPostImage(List<String> postImage) {
        this.postImage = postImage;
    }

    public String getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(String creationDate) {
        this.creationDate = creationDate;
    }

    public String getRoutine() {
        return routine;
    }

    public void setRoutine(String routine) {
        this.routine = routine;
    }
}
