package org.techtown.MyExerciseApp.Data.Mypage;

public class MypageBodypartRank implements Comparable<MypageBodypartRank>{
    int rank;
    String bodypartName;
    int value;

    public MypageBodypartRank(int rank, String bodypartName, int value) {
        this.rank = rank;
        this.bodypartName = bodypartName;
        this.value = value;
    }


    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public String getBodypartName() {
        return bodypartName;
    }

    public void setBodypartName(String bodypartName) {
        this.bodypartName = bodypartName;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }



    @Override
    public int compareTo(MypageBodypartRank mypageBodypartRank) {
        return getRank() - mypageBodypartRank.getRank();
        //return mypageBodypartRank.getRank() - getRank();
    }
}
