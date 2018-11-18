package app.bookshare.model;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

public class UserModel {
    private String email;
    private String first_name;
    private String last_name;
    private String profileImage;
    private String uid;
    private String phoneNo;
    private String address;

    public UserModel() {
        //Default constructor for firebase
    }

    public UserModel(String email, String first_name, String last_name,
                     String profileImage, String uid, String phoneNo, String address) {
        this.email = email;
        this.first_name = first_name;
        this.last_name = last_name;
        this.profileImage = profileImage;
        this.uid = uid;
        this.phoneNo = phoneNo;
        this.address = address;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirst_name() {
        return first_name;
    }

    public void setFirst_name(String first_name) {
        this.first_name = first_name;
    }

    public String getLast_name() {
        return last_name;
    }

    public void setLast_name(String last_name) {
        this.last_name = last_name;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getPhoneNo() {
        return phoneNo;
    }

    public void setPhoneNo(String phoneNo) {
        this.phoneNo = phoneNo;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();

        result.put("email", email);
        result.put("first_name", first_name);
        result.put("last_name", last_name);
        result.put("profileImage", profileImage);
        result.put("uid", uid);
        result.put("phoneNo", phoneNo);
        result.put("address", address);

        return result;
    }
}
