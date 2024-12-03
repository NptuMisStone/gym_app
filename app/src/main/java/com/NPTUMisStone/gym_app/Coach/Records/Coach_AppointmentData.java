package com.NPTUMisStone.gym_app.Coach.Records;

import java.util.ArrayList;
import java.util.Date;

public class Coach_AppointmentData {
    private Date date;
    private int scheduleID, appeople, classpeople;
    private String className, week, stTime, edTime, place, placeType, city, district; // 修正字段命名
    private static ArrayList<Coach_AppointmentData> appointments = new ArrayList<>();

    public Coach_AppointmentData(int scheduleID, Date date, String week, String stTime, String edTime,
                                 String className, int appeople, int classpeople,
                                 String place, String placeType, String city, String district) {
        this.scheduleID = scheduleID;
        this.date = date;
        this.week = week;
        this.stTime = stTime;
        this.edTime = edTime;
        this.className = className;
        this.appeople = appeople;
        this.classpeople = classpeople;
        this.place = place;
        this.placeType = placeType;
        this.city = city;
        this.district = district;
    }

    public static ArrayList<Coach_AppointmentData> getAppointments() {
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

    public int getAppeople() {
        return appeople;
    }

    public int getClasspeople() {
        return classpeople;
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
