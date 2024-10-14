package com.NPTUMisStone.gym_app.User.Records;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Date;

public class User_AppointmentData {
    private Date date;
    private Time time;
    private int reservationID, timeLong, status, coachId;
    private byte[] coachimage;
    private String className, classPrice, coachName, note,week;
    static ArrayList<User_AppointmentData> appointments = new ArrayList<>();

    public User_AppointmentData(int reservationID, Date date,String week, Time time, int timeLong, String className, String classPrice,  byte[] coachimage, String coachName,  int status, String note) {
        this.reservationID = reservationID;
        this.date = date;
        this.week=week;
        this.time=time;
        this.timeLong = timeLong;
        this.className = className;
        this.classPrice = classPrice;
        this.coachimage = coachimage;
        this.coachName = coachName;
        this.status = status;
        this.note = note;

    }


    public int getReservationID() {
        return reservationID;
    }

    public Date getDate() {
        return date;
    }
    public String getWeek(){return week;}

    public Time getTime(){return  time;}

    public int getTimeLong() {
        return timeLong;
    }

    public String getClassName() {
        return className;
    }
    public String getClassPrice(){
        return classPrice;
    }

    public byte[] getCoachimage() {
        return coachimage;
    }

    public String getCoachName() {
        return coachName;
    }

    public String getNote() {
        return note;
    }

}
