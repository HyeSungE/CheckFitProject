package org.techtown.MyExerciseApp.Data.Group;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class Group implements Serializable {
    String groupUid;
    String groupLeaderUid;
    String groupName;
    String groupDescription;
    String groupCreationDate;
    String groupImageName;
    int groupLimitSize;
    HashMap<String,GroupRoutine> groupRoutines;
    List<String> groupMembers;

    public Group() {}

    public Group(String groupUid, String groupLeaderUid, String groupName, String groupDescription, String groupCreationDate, String groupImageName, int groupLimitSize, HashMap<String,GroupRoutine> groupRoutines, List<String> groupMembers) {
        this.groupUid = groupUid;
        this.groupLeaderUid = groupLeaderUid;
        this.groupName = groupName;
        this.groupDescription = groupDescription;
        this.groupCreationDate = groupCreationDate;
        this.groupImageName = groupImageName;
        this.groupLimitSize = groupLimitSize;
        this.groupRoutines = groupRoutines;
        this.groupMembers = groupMembers;
    }

    public Group(String groupUid,String groupLeaderUid, String groupName, String groupDescription, String groupCreationDate, int groupLimitSize) {
        this.groupUid = groupUid;
        this.groupLeaderUid = groupLeaderUid;
        this.groupName = groupName;
        this.groupDescription = groupDescription;
        this.groupCreationDate = groupCreationDate;
        this.groupLimitSize = groupLimitSize;
        groupRoutines = new HashMap<>();
        groupMembers = new ArrayList<>();
    }

    public String getGroupUid() {
        return groupUid;
    }

    public void setGroupUid(String groupUid) {
        this.groupUid = groupUid;
    }

    public String getGroupLeaderUid() {
        return groupLeaderUid;
    }

    public void setGroupLeaderUid(String groupLeaderUid) {
        this.groupLeaderUid = groupLeaderUid;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getGroupDescription() {
        return groupDescription;
    }

    public void setGroupDescription(String groupDescription) {
        this.groupDescription = groupDescription;
    }

    public String getGroupCreationDate() {
        return groupCreationDate;
    }

    public void setGroupCreationDate(String groupCreationDate) {
        this.groupCreationDate = groupCreationDate;
    }

    public String getGroupImageName() {
        return groupImageName;
    }

    public void setGroupImageName(String groupImageName) {
        this.groupImageName = groupImageName;
    }

    public int getGroupLimitSize() {
        return groupLimitSize;
    }

    public void setGroupLimitSize(int groupLimitSize) {
        this.groupLimitSize = groupLimitSize;
    }

    public HashMap<String,GroupRoutine> getGroupRoutines() {
        return groupRoutines;
    }

    public void setGroupRoutines(HashMap<String,GroupRoutine> groupRoutines) {
        this.groupRoutines = groupRoutines;
    }

    public List<String> getGroupMembers() {
        return groupMembers;
    }

    public void setGroupMembers(List<String> groupMembers) {
        this.groupMembers = groupMembers;
    }

    public Date getCreationDate(){
        String creationDate = getGroupCreationDate();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = null;
        try {
            date = simpleDateFormat.parse(creationDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

}
