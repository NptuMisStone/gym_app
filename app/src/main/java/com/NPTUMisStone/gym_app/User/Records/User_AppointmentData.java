package com.NPTUMisStone.gym_app.User.Records;

import java.util.ArrayList;
import java.util.Date;

public class User_AppointmentData {
    Date date;
    int reservationID, timeLong, status;
    byte[] coachimage;
    String className, classPrice, coachName, note,week,time;
    static ArrayList<User_AppointmentData> appointments = new ArrayList<>();

    public User_AppointmentData(int reservationID, Date date, String week, String time, int timeLong, String className, String classPrice, byte[] coachimage, String coachName, int status, String note) {
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
    public static ArrayList<User_AppointmentData> getAppointments() {
        if (appointments == null) {
            return null;
        }
        return appointments;
    }

    public int getReservationID() {
        return reservationID;
    }

    public Date getDate() {
        return date;
    }
    public String getWeek(){return week;}

    public String getTime(){return  time;}

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

    public int getStatus() {
        return status;
    }

    public String getNote() {
        return note;
    }

}
