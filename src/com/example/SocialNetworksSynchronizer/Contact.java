package com.example.SocialNetworksSynchronizer;

//Данный класс представляет собой контакт в социальной сети(ВК или FB). Имеет множество полей и методов для доступа к ним
//Одно поле для ВК может иметь значение, тогда как для FB нет, как в случае с номером телефона (mobilePhone или homePhone)


import java.io.Serializable;

public class Contact implements Serializable {
    private String name        = "";
    private String mobilePhone = "";
    private String homePhone   = "";
    private String address     = "";
    private String email       = "";

    public Contact(String[] contactInfo) {
        if( contactInfo.length == 4 ) {
            name        = contactInfo[0];
            mobilePhone = contactInfo[1];
            homePhone   = contactInfo[2];
            address     = contactInfo[3];
        } else if( contactInfo.length == 3 ) {
            name = contactInfo[0];
            address = contactInfo[1];
            email = contactInfo[2];
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
    public void setEmail(String address) {
        this.email = email;
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
    public String getEmail() { return email; }
}
