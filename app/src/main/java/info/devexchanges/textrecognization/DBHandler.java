package info.devexchanges.textrecognization;


import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.Context;
import android.content.ContentValues;
import android.database.Cursor;

import java.io.File;
import java.util.ArrayList;

public class DBHandler extends SQLiteOpenHelper {

        //information about database
        private static final int DATABASE_VERSION = 1;
        private static final String DATABASE_NAME = "receiptsDB.db";
        public static final String RECEIPTS_TABLE_NAME = "Receipts";
        public static final String ITEMS_TABLE_NAME = "Items";

        // =========================================================================================
        //initialize the database
        public DBHandler(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        // =========================================================================================
        // Called if the database tables do not exist or were deleted.
        @Override
        public void onCreate(SQLiteDatabase db) {

            // SQLite code for creating the table to store the Receipts general data
            String CREATE_RECEIPT_TABLE = "CREATE TABLE " + "Receipts " + " ( "
                    + "ReceiptID" + " TEXT, "
                    + "Name " + "TEXT,"
                    + "Date " + "TEXT,"
                    + "Time " + "TEXT,"
                    + "Address1 " + "TEXT,"
                    + "City " + "TEXT,"
                    + "State " + "TEXT,"
                    + "PhoneNumber " + "TEXT,"
                    + "Total " + "TEXT" + " )";
            db.execSQL(CREATE_RECEIPT_TABLE);

            // SQLite code for creating the table to store the item specific data
            String CREATE_ITEMS_TABLE = "CREATE TABLE " + "Items " + " ( " + "ReceiptID" +
                    " TEXT, " + "Description " + "TEXT," + "Price " + "TEXT )";
            db.execSQL(CREATE_ITEMS_TABLE);
        }

        // =========================================================================================
        // Called when edits are made to the database
        @Override
        public void onUpgrade(SQLiteDatabase db, int i, int i1) {
            db.execSQL("DROP TABLE IF EXISTS " + RECEIPTS_TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + ITEMS_TABLE_NAME);

            onCreate(db);
        }

        // =========================================================================================
        // Called in the mainActivity class when the app starts up.
        // This loads the information stored in the Receipts table in the database
        //     and populates the receiptList in mainActivity.
        public ArrayList<ArrayList<String>> loadReceiptHandler() {
            String result_temp;
            String query = "Select*FROM " + RECEIPTS_TABLE_NAME;
            SQLiteDatabase db = this.getWritableDatabase();
            Cursor cursor = db.rawQuery(query, null);

            ArrayList<ArrayList<String>> tempReceiptObjects = new ArrayList<>();

            while (cursor.moveToNext()) {
                ArrayList<String> tempReceiptInfo = new ArrayList<>();
                for (int i = 0; i < 9; i++){
                    result_temp = cursor.getString(i);
                    tempReceiptInfo.add(result_temp);
                }
                tempReceiptObjects.add(tempReceiptInfo);
            }
            cursor.close();
            db.close();
            return tempReceiptObjects;
        }

        // =========================================================================================
        // Called in the mainActivity class when the app starts up.
        // This loads the information stored in the Items table in the database
        //     and populates the receiptList objects with there respective items in mainActivity.
        public ArrayList<ArrayList<String>> loadItemsHandler() {
            String result_temp;
            String query = "Select*FROM " + ITEMS_TABLE_NAME;
            SQLiteDatabase db = this.getWritableDatabase();
            Cursor cursor = db.rawQuery(query, null);

            ArrayList<ArrayList<String>> tempItemObjects = new ArrayList<>();

            while (cursor.moveToNext()) {
                ArrayList<String> tempItemInfo = new ArrayList<>();
                for (int i = 0; i < 3; i++) {
                    result_temp = cursor.getString(i);
                    tempItemInfo.add(result_temp);
                }
                tempItemObjects.add(tempItemInfo);
            }
            cursor.close();
            db.close();
            return tempItemObjects;
        }

        // =========================================================================================
        // Adds a receipt object the the database.
        public void addReceiptHandler(ReceiptObject receipt) {
            ContentValues values = new ContentValues();
            values.put("ReceiptID", Integer.toString(receipt.getReceiptID()));
            values.put("Name", receipt.getReceiptName());
            values.put("Date", receipt.getDate());
            values.put("Time", receipt.getTime());
            values.put("Address1", receipt.getAddress1());
            values.put("City", receipt.getCity());
            values.put("State", receipt.getState());
            values.put("PhoneNumber", receipt.getPhoneNumber());
            values.put("Total", receipt.getTotalSpent());
            SQLiteDatabase db = this.getWritableDatabase();
            db.insert(RECEIPTS_TABLE_NAME, null, values);
            db.close();
        }

        // =========================================================================================
        // Adds an item to the database
        public void addItemHandler(Item item) {
            ContentValues values = new ContentValues();
            values.put("ReceiptID", Integer.toString(item.getReceiptID()));
            values.put("Description", item.getName());
            values.put("Price", item.getPrice());
            SQLiteDatabase db = this.getWritableDatabase();
            db.insert(ITEMS_TABLE_NAME, null, values);
            db.close();
        }

        // =========================================================================================
        // For use in the saveReceipts function in mainActivity
        // Resets the Receipts table - Deletes everything in the table.
        public boolean deleteReceiptTableHandler() {
            boolean result = false;
                SQLiteDatabase db = this.getWritableDatabase();
                db.execSQL("DELETE FROM " + RECEIPTS_TABLE_NAME);
                db.delete(RECEIPTS_TABLE_NAME, null, null);
                db.close();

            return result;
        }

        // =========================================================================================
        // For use in the saveReceipts function in mainActivity
        // Resets the Items table - Deletes everything in the table.
        public boolean deleteItemTableHandler() {
            boolean result = false;
                String query = "DELETE FROM " + ITEMS_TABLE_NAME;
                SQLiteDatabase db = this.getWritableDatabase();
                db.execSQL(query);
            db.delete(ITEMS_TABLE_NAME, null, null);
            db.close();
            return result;
        }

        // =========================================================================================
        
    }

