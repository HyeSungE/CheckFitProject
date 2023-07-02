package org.techtown.MyExerciseApp.MyClass;

import java.text.SimpleDateFormat;
import java.util.Date;

public class GetToday {
    public GetToday() {
    }
    //오늘의 날짜 스트링으로 반환
    public String getToday() {
        long now;
        Date date;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        now = System.currentTimeMillis();
        date = new Date(now);
        return simpleDateFormat.format(date);
    }

    public String getTodayTime() {
        long now;
        Date date;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        now = System.currentTimeMillis();
        date = new Date(now);
        return simpleDateFormat.format(date);
    }
    public String getTodayTimeOnlyNumber() {
        long now;
        Date date;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        now = System.currentTimeMillis();
        date = new Date(now);
        return simpleDateFormat.format(date);
    }
}
