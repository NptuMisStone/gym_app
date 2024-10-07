package com.NPTUMisStone.gym_app.User.Main;

import android.util.Log;

public class User {
    private int UserId;
    private String UserAccount;
    private String UserName;
    private String UserPhone;
    private int UserSex;
    private String UserMail;
    private byte[] UserImage;
    private static volatile User user = null;
    private User(int id) {
        this.UserId = id;
    }
    public static synchronized User getInstance(){
        if (user == null) {
            user = new User(0);
            Log.e("user", "get_user:注意，已經創建了一個新的User實例。");
        }
        return user;
    }
    public static void setInstance(int id, String userAccount,  String userName,String userPhone,int userSex,String userMail,byte[] userImage) {
        if (user == null) {
            user = new User(id);
            user.setUserAccount(userAccount);
            user.setUserName(userName);
            user.setUserPhone(userPhone);
            user.setUserSex(userSex);
            user.setUserMail(userMail);
            user.setUserImage(userImage);
            Log.e("user", "set_user:注意，已經創建了一個新的User實例。");
        } else {
            user.setId(id);
            user.setUserAccount(userAccount);
            user.setUserName(userName);
            user.setUserPhone(userPhone);
            user.setUserSex(userSex);
            user.setUserMail(userMail);
            user.setUserImage(userImage);
        }
    }
    public void setId(int id) {     this.UserId = id;   }
    public void setUserAccount(String userAccount) {    this.UserAccount = userAccount;  }
    public void setUserName(String userName) {    this.UserName = userName;  }
    public void setUserPhone(String userPhone) {    this.UserPhone = userPhone;  }
    public void setUserSex(int userSex) {    this.UserSex = userSex;  }
    public void setUserMail(String userMail) {    this.UserMail = userMail;  }
    public void setUserImage(byte[] userImage) {    this.UserImage = userImage;  }
    public int getUserId() {    return UserId;  }
    public String getUserAccount() {    return UserAccount;  }
    public String getUserName() {    return UserName;  }
    public String getUserPhone() {    return UserPhone;  }
    public int getUserSex() {    return UserSex;  }
    public String getUserMail() {    return UserMail;  }
    public byte[] getUserImage() {    return UserImage;  }
}
