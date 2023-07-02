package org.techtown.MyExerciseApp.Data.Group;

import org.techtown.MyExerciseApp.Data.User;

public class GroupUser {
   private Group group;
    private  User user;

    public GroupUser(Group group, User user) {
        this.group = group;
        this.user = user;
    }

    public GroupUser() {
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
