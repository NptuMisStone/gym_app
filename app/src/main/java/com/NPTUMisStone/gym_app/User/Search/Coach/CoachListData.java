package com.NPTUMisStone.gym_app.User.Search.Coach;

import java.util.ArrayList;

public class CoachListData {

    int id;
    String coachName;
    byte[] coachImage;
    String coachDescription;
    static ArrayList<CoachListData> coaches = new ArrayList<>();

    public CoachListData(int id, byte[] coachImage, String coachName, String coachDescription) {
        this.id = id;
        this.coachImage = coachImage;
        this.coachName = coachName;
        this.coachDescription = coachDescription;
    }

    public static void addCoach(CoachListData coach) {
        coaches.add(coach);
    }

    private static void setCoaches(ArrayList<CoachListData> coaches) {
        CoachListData.coaches = coaches;
    }

    public static ArrayList<CoachListData> getCoachObjects() {
        if (coaches == null) {
            return null;
        }
        return coaches;
    }

    public int getId() {
        return id;
    }

    public byte[] getcoachImage() {
        return coachImage;
    }

    public String getCoachName() {
        return coachName;
    }

    public String getCoachDescription() {
        return coachDescription;
    }
}
