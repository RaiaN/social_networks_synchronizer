package com.example.SocialNetworksSynchronizer;

public class Contact {
    private String name = "";
    private String mobilePhone = "";
    private String homePhone = "";
    private String address = "";

    public Contact(String[] contactInfo) {
        if( contactInfo.length == 4 ) {
            name        = contactInfo[0];
            mobilePhone = contactInfo[1];
            homePhone   = contactInfo[2];
            address     = contactInfo[3];
        } else {

        }
    }

    public void setName(String name) {
        this.name = name;
    }
    public void setMobilePhone(String mobilePhone) {
        this.mobilePhone = mobilePhone;
    }
    public void setHomePhone(String homePhone) {
        this.homePhone = homePhone;
    }
    public void setAddress(String address) {
        this.address = address;
    }

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
}
