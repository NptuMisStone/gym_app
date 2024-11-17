package com.NPTUMisStone.gym_app.Coach.Records;


import java.util.ArrayList;
import java.util.Date;

public class Coach_AppointmentData {
    Date date;
    int scheduleID,appeople,classpeople;
    String className,week,StTime,EdTime,place,placeType;
    static ArrayList<Coach_AppointmentData> appointments = new ArrayList<>();

    public Coach_AppointmentData(int scheduleID, Date date, String week, String StTime, String EdTime, String className,int appeople,int classpeople,String place,String placeType) {
        this.scheduleID=scheduleID;
        this.date = date;
        this.week=week;
        this.StTime=StTime;
        this.EdTime=EdTime;
        this.className = className;
        this.appeople=appeople;
        this.classpeople=classpeople;
        this.place=place;
        this.placeType=placeType;
    }
    public static ArrayList<Coach_AppointmentData> getAppointments() {
        if (appointments == null) {
            return null;
        }
        return appointments;
    }

    public int getScheduleID(){return scheduleID;}
    public Date getDate() {
        return date;
    }
    public String getWeek(){return week;}
    public String getStTime(){return StTime;}
    public String getEdTime(){return EdTime;}
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
}
