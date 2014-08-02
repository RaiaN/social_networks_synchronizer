package sns;


//Данный класс представляет собой синхронизированный контакт, состоящий из:
//1) Имени контакта в устройстве
//2) Контакта в ВК (со всей полученной информацией)
//2) Контакта в FB (со всей полученной информацией)

//Имеются методы для получения параметров контакта, таких как имя в устройстве, имя в ВК, ...

import java.io.Serializable;

public class SyncContact implements Serializable{
    private int PHONE_NUMBER_LENGTH = 11;

    private String phonebookName = "";
    private String phonebookMobileNumber = "";
    private Contact vkContact = null;
    private Contact fbContact = null;

    public SyncContact() {}

    public SyncContact(String phonebookName, Contact vkContact, Contact fbContact) {
        this.phonebookName = phonebookName;
        this.vkContact = vkContact;
        this.fbContact = fbContact;
    }

    public void setPhonebookName(String name) { this.phonebookName = name; }
    public void setPhonebookMobileNumber(String number) { this.phonebookMobileNumber = number; }
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

    private String tryToMakeValidPhoneNumber(String number) {
        String validNumber = "";

        for( int i = 0; i < number.length() && validNumber.length() <= PHONE_NUMBER_LENGTH; ++i ) {
            if( Character.isDigit(number.charAt(i)) ) {
                validNumber += number.charAt(i);
            }
        }
        if( validNumber.length() < PHONE_NUMBER_LENGTH ) {
            return "";
        }

        if( validNumber.startsWith("7") ) {
            validNumber = "+" + validNumber;
        }

        return validNumber;
    }

    public String getCorrectPhoneNumber() {
        if( phonebookMobileNumber.length() != 0 ) {
            return phonebookMobileNumber;
        } else if( vkContact != null ) {
            String mobilePhone = tryToMakeValidPhoneNumber(vkContact.getMobilePhone());
            if( mobilePhone.length() > 0 ) {
                return mobilePhone;
            }

            String homePhone = tryToMakeValidPhoneNumber(vkContact.getHomePhone());
            if( homePhone.length() > 0 ) {
                return homePhone;
            }
        }

        return "";
    }
}
