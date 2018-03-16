package info.devexchanges.textrecognization;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

//==============================================================================================
// Description of below:
// ReceiptObject is created to store information gathered about the restaurant and to hold the
// Item purchased that are stored using the Item class.
//==============================================================================================



public class ReceiptObject implements Parcelable {


    //NOTE: make sure if you change these variables you also change the parcelable to match.
    private String receiptName;
    private String storeNumber;
    private String date;
    private String time;
    private String Address1;
    private String city;
    private String state;
    private String phoneNumber;
    private String totalSpent;
    private ArrayList<Item> receiptItems;
    public int receiptID;


    private int[] date_2;

    // =========================================================================================
    // Constructor for Receipt Object
    // Creates a new receiptsPreParsed ArrayList
    public ReceiptObject(int receiptNumber){
        this.receiptName = "Receipt " + receiptNumber;
        receiptID = receiptNumber;
        receiptItems = new ArrayList<>();
    }


    public ReceiptObject(String receiptID, String date, String merchantName, String totalSpent){
        this.receiptID = Integer.parseInt(receiptID);
        this.receiptName = merchantName;
        this.setDate(date);
        this.totalSpent = totalSpent;
        this.receiptItems = new ArrayList<>();

        String[] dateArr = date.split("/");
        this.date_2 = new int[]{Integer.parseInt(dateArr[0]), Integer.parseInt(dateArr[1]), Integer.parseInt(dateArr[2])};
    }

    public int[] getDate_2() {
        return date_2;
    }



    // =========================================================================================

    public void addItem(String name,String price){
        Item newItem = new Item(name, price);
        newItem.setReceiptID(receiptID);
        receiptItems.add(newItem);
    }

    // =========================================================================================
    // Getter and setter functions
    public String getDate() {
        return date;
    }

    public String getReceiptName(){
        String[] receiptPeices = receiptName.split(" ");
        String finalReceiptName = "";
        for(String s : receiptPeices){
            finalReceiptName += Character.toUpperCase(s.charAt(0)) + s.substring(1) + " ";
        }
        return finalReceiptName;
    }

    public String getTime() {return time;}

    public String getCity(){return city;}

    public String getState(){return state;}

    public String getPhoneNumber(){return phoneNumber;}

    public String getAddress1() {
        return Address1;
    }

    public ArrayList<Item> getReceiptItems() {
        return receiptItems;
    }

    public String getStoreNumber() {
        return storeNumber;
    }

    public String getTotalSpent() {
        return totalSpent;
    }

    public int getReceiptID() {return receiptID;}

    public void setReceiptName(String receiptName){this.receiptName = receiptName;}

    public void setDate(String date) {
        if(date == null) {
            date = "";
        }
        this.date = date;
        String[] dateArr = date.split("/");
        if(dateArr.length >= 2) {
            this.date_2 = new int[]{Integer.parseInt(dateArr[0]), Integer.parseInt(dateArr[1]), Integer.parseInt(dateArr[2])};
        }
        else {
            this.date_2 = new int[]{0, 0, 0};
        }
    }

    public void setTime(String time) {this.time = time;}

    public void setCity(String city){this.city = city;}

    public void setState(String state){this.state = state;}

    public void setPhoneNumber(String phoneNumber){this.phoneNumber = phoneNumber;}

    public void setAddress1(String address1) {
        this.Address1 = address1;
    }

    public void setStoreNumber(String storeNumber) {
        this.storeNumber = storeNumber;
    }

    public void setTotalSpent(String totalSpent) {
        this.totalSpent = totalSpent;
    }

    public void setReceiptID(int receiptID) {this.receiptID = receiptID;}



    // =========================================================================================
    // Functions to make this class Parcelable
    public ReceiptObject(Parcel source) {
        this.receiptName = source.readString();
        this.storeNumber = source.readString();
        setDate(source.readString());
        this.time = source.readString();
        this.Address1 = source.readString();
        this.city = source.readString();
        this.state = source.readString();
        this.phoneNumber = source.readString();
        this.totalSpent = source.readString();
        this.receiptItems = new ArrayList<>();
        source.readList(receiptItems, Item.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(receiptName);
        dest.writeString(storeNumber);
        dest.writeString(date);
        dest.writeString(time);
        dest.writeString(Address1);
        dest.writeString(city);
        dest.writeString(state);
        dest.writeString(phoneNumber);
        dest.writeString(totalSpent);
        dest.writeList(receiptItems);
    }

    public static final Parcelable.Creator<ReceiptObject> CREATOR = new Parcelable.Creator<ReceiptObject>(){

        @Override
        public ReceiptObject createFromParcel(Parcel source) {
            return new ReceiptObject(source);
        }

        @Override
        public ReceiptObject[] newArray(int size) {
            return new ReceiptObject[0];
        }
    };

    // =========================================================================================
    // toString is used to print out contents of the receiptObject
    // this includes printing the Item in the receiptsPreParsed list
    @Override
    public String toString() {
        String itemsTemp = "";
        for (Item s : receiptItems){
            itemsTemp += s.toString();
            itemsTemp += '\n';
        }
        return "Receipt{" + "Receipt: " + receiptName +
                "Starbucks Store='" + storeNumber + '\'' +
                ", date='" + date + '\'' +
                ", time='" + time + '\'' +
                ", address='" + Address1 + '\'' +
                ", city='" + city + '\'' +
                ", state='" + state + '\'' +
                ", phone Number='" + phoneNumber + '\'' +
                ", Item: '" + itemsTemp + '\'' +
                ", total='" + totalSpent + '\'' +
                '}';
    }


}



// =========================================================================================
// =========================================================================================
class Item implements Parcelable {
    private String price;
    private String name;
    private int ReceiptID;

    // =========================================================================================
    public Item(String _name, String _price){
        name = _name;
        price = _price;
    }

    // =========================================================================================
    // Getter and setter functions
    public String getPrice() {
        return price;
    }

    public String getName() {
        return name;
    }

    public int getReceiptID() {return ReceiptID;}

    public void setName(String name) {
        this.name = name;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public void setReceiptID(int receiptID) {ReceiptID = receiptID;}


    // =========================================================================================
    @Override
    public String toString() {
        return name + "\t" + price;
    }

    // =========================================================================================
    // Functions to make this class parcelable
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(price);
        dest.writeString(name);
    }

    public Item(Parcel source) {
        price = source.readString();
        name = source.readString();
    }

    public static final Parcelable.Creator<Item> CREATOR = new Parcelable.Creator<Item>(){

        @Override
        public Item createFromParcel(Parcel source) {
            return new Item(source);
        }

        @Override
        public Item[] newArray(int size) {
            return new Item[size];
        }
    };
}