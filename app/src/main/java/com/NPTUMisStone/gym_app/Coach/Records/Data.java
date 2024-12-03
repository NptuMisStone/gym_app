package com.NPTUMisStone.gym_app.Coach.Records;

import java.util.ArrayList;
import java.util.Date;

public class Data {
    private final Date date;
    private final int scheduleID, apPeople, classPeople;
    private final String className, week, stTime, edTime, place, placeType;
    private String city, district; // 修正字段命名
    private static final ArrayList<Data> appointments = new ArrayList<>();

    public Data(int scheduleID, Date date, String week, String stTime, String edTime,
                String className, int apPeople, int classPeople,
                String place, String placeType, String city, String district) {
        this.scheduleID = scheduleID;
        this.date = date;
        this.week = week;
        this.stTime = stTime;
        this.edTime = edTime;
        this.className = className;
        this.apPeople = apPeople;
        this.classPeople = classPeople;
        this.place = place;
        this.placeType = placeType;
        this.city = city;
        this.district = district;
    }

    public static ArrayList<Data> getAppointments() {
        return appointments;
    }

    public int getScheduleID() {
        return scheduleID;
    }

    public Date getDate() {
        return date;
    }

    public String getWeek() {
        return week;
    }

    public String getStTime() {
        return stTime;
    }

    public String getEdTime() {
        return edTime;
    }

    public String getClassName() {
        return className;
    }

    public int getApPeople() {
        return apPeople;
    }

    public int getClassPeople() {
        return classPeople;
    }

    public String getPlace() {
        return place;
    }

    public String getPlaceType() {
        return placeType;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }
}
