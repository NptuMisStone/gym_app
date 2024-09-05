package com.NPTUMisStone.gym_app.User_And_Coach;

import java.util.Date;

public class Advertisement {
    byte[] image;
    String link;
    Date startDate;
    Date endDate;

    public Advertisement(byte[] image, String link, Date startDate, Date endDate) {
        this.image = image;
        this.link = link;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public byte[] getImage() {
        return image;
    }

    public String getLink() {
        return link;
    }

    public Date getStartDate() {
        return startDate;
    }

    public Date getEndDate() {
        return endDate;
    }
}
