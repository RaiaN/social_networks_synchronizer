package com.example.SocialNetworksSynchronizer;


//Данный класс представляет собой синхронизированный контакт, состоящий из:
//1) Имени контакта в устройстве
//2) Контакта в ВК (со всей полученной информацией)
//2) Контакта в FB (со всей полученной информацией)

//Имеются методы для получения параметров контакта, таких как имя в устройстве, имя в ВК, ...

public class SyncContact {
    private String phonebookName = "";
    private Contact vkContact = null;
    private Contact fbContact = null;

    public SyncContact() {}

    public SyncContact(String phonebookName, Contact vkContact, Contact fbContact) {
        this.phonebookName = phonebookName;
        this.vkContact = vkContact;
        this.fbContact = fbContact;
    }

    public void setPhonebookName(String name) { this.phonebookName = name; }
    public void setVkContact(Contact vkContact) {
        this.vkContact = vkContact;
    }
    public void setFbContact(Contact fbContact) {
        this.fbContact = fbContact;
    }
    public Contact getVkContact() {
        return vkContact;
    }
    public Contact getFbContact() {
        return fbContact;
    }

    public String getPhonebookName() {
        return phonebookName;
    }
    public String getVkName() {
        if( vkContact != null ) {
            return vkContact.getName();
        }
        return "";
    }
    public String getFbName() {
        if( fbContact != null ) {
            return fbContact.getName();
        }
        return "";
    }
}
