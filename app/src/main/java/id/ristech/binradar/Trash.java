package id.ristech.binradar;

import com.google.firebase.database.DataSnapshot;

/**
 * Created by Faldy on 7/2/2016.
 */
public class Trash {

    String userID;
    Double latitude;
    Double longitude;
    String description;
    Integer type;

    public Trash(String userID, Double latitude, Double longitude, Integer type, String description) {
        this.userID = userID;
        this.latitude = latitude;
        this.longitude = longitude;
        this.type = type;
        this.description = description;
    }

    public Trash() {
        // required constructor with no arguments
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}
