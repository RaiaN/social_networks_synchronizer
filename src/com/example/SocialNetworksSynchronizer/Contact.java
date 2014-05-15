package com.example.SocialNetworksSynchronizer;

//Данный класс представляет собой контакт в социальной сети(ВК или FB). Имеет множество полей и методов для доступа к ним
//Одно поле для ВК может иметь значение, тогда как для FB нет, как в случае с номером телефона (mobilePhone или homePhone)


import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Contact implements Serializable {
    public static String NAME         = "name";
    public static String BIRTHDAY     = "birthday";
    public static String MOBILE_PHONE = "mobilePhone";
    public static String HOME_PHONE   = "homePhone";
    public static String ADDRESS      = "address";
    public static String SKYPE        = "skype";
    public static String TWITTER      = "twitter";
    public static String INSTAGRAM    = "instagram";
    public static String PHOTO_URL    = "photoUrl";
    public static String EDUCATION    = "education";

    public static String []FIELDS     = new String[]{ NAME, BIRTHDAY, MOBILE_PHONE, HOME_PHONE, ADDRESS,
                                                      SKYPE, TWITTER, INSTAGRAM, EDUCATION };

    private String name           = "";
    private String birthday       = "";
    private String mobilePhone    = "";
    private String homePhone      = "";
    private String address        = "";
    private String skype          = "";
    private String twitter        = "";
    private String instagram      = "";
    private String photoUrl       = "";
    private String education      = "";
    private byte[] bytesImage     = null;

    public Contact(HashMap<String, String> contactInfo) {
        name        = contactInfo.get(NAME);
        mobilePhone = contactInfo.get(MOBILE_PHONE);
        homePhone   = contactInfo.get(HOME_PHONE);
        address     = contactInfo.get(ADDRESS);
        birthday    = contactInfo.get(BIRTHDAY);
        skype       = contactInfo.get(SKYPE);
        twitter     = contactInfo.get(TWITTER);
        instagram   = contactInfo.get(INSTAGRAM);
        photoUrl    = contactInfo.get(PHOTO_URL);
        education   = contactInfo.get(EDUCATION);
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
    public void setEducation(String education) { this.education = education; }
    public void setImage(byte[] bytesImage) { this.bytesImage = bytesImage; }

    public String getName() {
        return name;
    }
    public String getBirthday() { return birthday; }
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
    public String getTwitter() { return twitter; }
    public String getInstagram() { return instagram; }
    public String getPhotoUrl() { return photoUrl; }
    public String getEducation() {
        return education;
    }
    public List<String> getAllInfo() {
        List<String> info = new ArrayList<String>();
        info.add(getName());
        info.add(getBirthday());
        info.add(getMobilePhone());
        info.add(getHomePhone());
        info.add(getAddress());
        info.add(getSkype());
        info.add(getTwitter());
        info.add(getInstagram());
        info.add(getEducation());
        return info;
    }

    public byte[] getImage() { return bytesImage; }
}
