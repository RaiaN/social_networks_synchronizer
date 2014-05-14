package com.example.SocialNetworksSynchronizer;

//Данный класс представляет собой контакт в социальной сети(ВК или FB). Имеет множество полей и методов для доступа к ним
//Одно поле для ВК может иметь значение, тогда как для FB нет, как в случае с номером телефона (mobilePhone или homePhone)


import java.io.Serializable;
import java.util.HashMap;

public class Contact implements Serializable {
    private String name        = "";
    private String birthday    = "";
    private String mobilePhone = "";
    private String homePhone   = "";
    private String address     = "";
    private String skype       = "";
    private String twitter     = "";
    private String instagram   = "";
    private String photoUrl    = "";
    private String university  = "";
    private String faculty     = "";


    public Contact(HashMap<String, String> contactInfo) {
        name        = contactInfo.get("name");
        mobilePhone = contactInfo.get("mobilePhone");
        homePhone   = contactInfo.get("homePhone");
        address     = contactInfo.get("address");
        birthday    = contactInfo.get("birthday");
        skype       = contactInfo.get("skype");
        twitter     = contactInfo.get("twitter");
        instagram   = contactInfo.get("instagram");
        photoUrl    = contactInfo.get("photoUrl");
        university  = contactInfo.get("university");
        faculty     = contactInfo.get("faculty");
    }

    public void setName(String name) {
        this.name = name;
    }
    public void setBirthday(String birthday) { this.birthday = birthday; }
    public void setMobilePhone(String mobilePhone) {
        this.mobilePhone = mobilePhone;
    }
    public void setHomePhone(String homePhone) {
        this.homePhone = homePhone;
    }
    public void setAddress(String address) {
        this.address = address;
    }
    public void setSkype(String skype) { this.skype = skype; }
    public void setTwitter(String twitter) {
        this.twitter = twitter;
    }
    public void setInstagram(String instagram) { this.instagram = instagram; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }
    public void setUniversity(String university) { this.university = university; }
    public void setFaculty(String faculty) { this.faculty = faculty; }

    public String getName() {
        return name;
    }
    public String getMobilePhone() {
        return mobilePhone;
    }
    public String getHomePhone() {
        return homePhone;
    }
    public String getAddress() {
        return address;
    }
    public String getSkype() { return skype; }
    public String getBirthday() { return birthday; }
    public String getTwitter() { return twitter; }
    public String getInstagram() { return instagram; }
    public String getPhotoUrl() { return photoUrl; }
    public String getUniversity() {
        return university;
    }
    public String getFaculty() {
        return faculty;
    }
}
