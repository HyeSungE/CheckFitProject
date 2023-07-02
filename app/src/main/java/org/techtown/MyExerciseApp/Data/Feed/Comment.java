package org.techtown.MyExerciseApp.Data.Feed;

import java.util.ArrayList;

public class Comment {
    String uid;
    String postId;
    String commentId;
    String commentContent;
    String commentCreationDate;
    ArrayList<Comment> replies;

    public Comment(){}

    public Comment(String uid, String postId, String commentId, String commentContent, String commentCreationDate, ArrayList<Comment> replies) {
        this.uid = uid;
        this.postId = postId;
        this.commentId = commentId;
        this.commentContent = commentContent;
        this.commentCreationDate = commentCreationDate;
        this.replies = replies;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getCommentId() {
        return commentId;
    }

    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }

    public String getCommentContent() {
        return commentContent;
    }

    public void setCommentContent(String commentContent) {
        this.commentContent = commentContent;
    }

    public String getCommentCreationDate() {
        return commentCreationDate;
    }

    public void setCommentCreationDate(String commentCreationDate) {
        this.commentCreationDate = commentCreationDate;
    }

    public ArrayList<Comment> getReplies() {
        return replies;
    }

    public void setReplies(ArrayList<Comment> replies) {
        this.replies = replies;
    }
}
