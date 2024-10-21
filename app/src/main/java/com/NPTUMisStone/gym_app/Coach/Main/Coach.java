package com.NPTUMisStone.gym_app.Coach.Main;

import android.util.Log;

public class Coach {
    private int CoachId;
    private String CoachAccount;
    private String CoachName;
    private String CoachPhone;
    private int CoachSex;
    private String CoachMail;
    private byte[] CoachImage;
    private static volatile Coach coach = null;
    private Coach(int id) {
        this.CoachId = id;
    }
    public static synchronized Coach getInstance(){
        if (coach == null) {
            coach = new Coach(0);
            Log.e("coach", "注意，已經創建了一個新的coach實例。");
        }
        return coach;
    }
    public static void setInstance(int id, String coachAccount, String coachName, String coachPhone, int coachSex, String coachMail,byte[] coachImage) {
        if (coach == null) {
            coach = new Coach(id);
            coach.setCoachAccount(coachAccount);
            coach.setCoachName(coachName);
            coach.setCoachPhone(coachPhone);
            coach.setCoachSex(coachSex);
            coach.setCoachMail(coachMail);
            coach.setCoachImage(coachImage);
            Log.e("coach", "注意，已經創建了一個新的coach實例。");
        } else {
            coach.setCoachId(id);
            coach.setCoachAccount(coachAccount);
            coach.setCoachName(coachName);
            coach.setCoachPhone(coachPhone);
            coach.setCoachSex(coachSex);
            coach.setCoachMail(coachMail);
            coach.setCoachImage(coachImage);
        }
    }
    public void setCoachId(int id) {     this.CoachId = id;   }
    public void setCoachAccount(String coachAccount) {    this.CoachAccount = coachAccount;  }
    public void setCoachName(String coachName) {    this.CoachName = coachName;  }
    public void setCoachPhone(String coachPhone) {    this.CoachPhone = coachPhone;  }
    public void setCoachSex(int coachSex) {    this.CoachSex = coachSex;  }
    public void setCoachMail(String coachMail) {    this.CoachMail = coachMail;  }
    public void setCoachImage(byte[] coachImage) {    this.CoachImage = coachImage;  }
    public int getCoachId() {    return CoachId;  }
    public String getCoachAccount() {    return CoachAccount;  }
    public String getCoachName() {    return CoachName;  }
    public String getCoachPhone() {    return CoachPhone;  }
    public int getCoachSex() {    return CoachSex;  }
    public String getCoachMail() {    return CoachMail;  }
    public byte[] getCoachImage() {    return CoachImage;  }
}
