package info.devexchanges.textrecognization;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashSet;


public class ParseReceiptData {

    public static final int numberOfCases = 12;
    public static final int itemsCaseNumber = 6;

    private static final String[] statesArray = new String[]{"AK","AL","AR","AZ","CA","CO","CT",
            "DE","FL","GA","HI","IA","ID","IL","IN","KS","KY", "LA","MA","MD","ME","MI","MN","MO",
            "MS","MT","NC","ND","NE","NH","NJ","NM","NV","NY","OH","OK", "OR","PA","RI","SC","SD",
            "TN","TX","UT","VA","VT","WA","WI","WV","WY"};
    static final HashSet<String> statesSet = new HashSet<>(Arrays.asList(statesArray));

    // Loops over each line and checks to see if that line matches
    // any of the expected line patterns
    static void parseAndPopulateReceipt(ReceiptObject newReceipt, ReceiptTextRaw current){
        int currentTableIndex = 0;
        while(current.next != null){
            for(int i = currentTableIndex; i < numberOfCases; i++){
                boolean result = checkLine(i, current.getDescription(), newReceipt);
                if(result){
                    if(i != itemsCaseNumber)currentTableIndex = i + 1;
                    break;
                }
            }
            current = current.next;
        }
    }

    // contains pattern definitions for each line expected in a Starbucks receipt
    // returns a boolean for the result of a given line matching a specified pattern
    static boolean checkLine(int table, String line, ReceiptObject targetReceipt){
        String[] items;
        int itemsLength;
        boolean result;
        switch(table){
            // Check store number
            case(0):
                result = line.contains("Starbucks Store");
                items = line.split(" ");
                if(items.length != 3) return false;
                if(result){
                    targetReceipt.setStoreNumber(items[2]);
                    targetReceipt.setReceiptName(line);
                }
                return result;

            // Check address
            case(1):
                items = line.split(" ");
                if(items.length != 3) return false;
                for(int i = 0; i < items.length; i++){
                    try{
                        Integer.parseInt(items[i]);
                        result = (i == 0);
                    }
                    catch (NumberFormatException e){ result = (i != 0); }
                    if (!result) return false;
                }
                targetReceipt.setAddress1(line);
                return true;

            // Check city, state, phone number
            case(2):
                items = line.split(" ");
                if(items.length != 3) return false;
                if(!statesSet.contains(items[1])) return false;
                // check that all Item in 3rd position are numbers
                String[] phoneNumber = items[2].split("-");
                for(String s : phoneNumber){
                    try{
                        Integer.parseInt(s);
                        // should we make sure we only return true if entire 10-digit number was captured?
                    }
                    catch (NumberFormatException e){ return false;}
                }
                targetReceipt.setCity(items[0].replace(",", ""));
                targetReceipt.setState(items[1]);
                targetReceipt.setPhoneNumber(items[2]);
                return true;

            // Check CHK #
            case(3):
                return line.contains("CHK");

            // Check date/time
            case(4):
                items = line.split(" ");
                itemsLength = items.length;
                if(itemsLength > 3) return false;
                // check date format
                if(itemsLength > 0){
                    DateFormat sourceFormat = new SimpleDateFormat("dd/MM/yyyy");
                    try{
                        sourceFormat.parse(items[0]);
                    }
                    catch (ParseException e){ return false;}
                }
                // check time format
                if(itemsLength > 1){
                    String[] time = items[1].split(":");
                    if(time.length != 2) return false;
                    try{
                        int hour = Integer.parseInt(time[0]);
                        if(!(hour >= 0 && hour < 24)) return false;
                    }
                    catch (NumberFormatException e){ return false;}
                    try{
                        int minute = Integer.parseInt(time[1]);

                        if(!(minute >= 0 && minute <= 59)) return false;
                    }
                    catch (NumberFormatException e){ return false;}
                }
                // check if AM PM
                if(itemsLength > 2){
                    if(!(items[2].equals("AM") || items[2].equals("PM"))) return false;
                }
                targetReceipt.setDate(items[0]);
                targetReceipt.setTime(items[1] + items[2]);
                return true;

            // Check ID/Drawer#/Reg#
            case(5):
                items = line.split(" ");
                if(items.length != 5) return false;
                try{
                    Integer.parseInt(items[0]);
                    Integer.parseInt(items[2]);
                    Integer.parseInt(items[4]);
                }
                catch (NumberFormatException e){return false;}
                return(items[1].contains("Drawer") && items[3].contains("Reg"));

            // Check Item
            case(6):
                // this is the weird one. Could have many, could have \t-separated prices
                if(line.contains("Visa") || line.contains("Mastercard")) return false;
                items = line.split("\t");
                if(items.length < 2) return false;
                try{
                    Float.parseFloat(items[1]);
                }
                catch (NumberFormatException e){ return false;}
                targetReceipt.addItem(items[0], items[1]);
                return true;

            // Check Subtotal
            case(7):
                items = line.split("\t");
                if(items.length < 2) return false;
                return (items[0].equals("Subtotal") && items[1].contains("$"));

            // Check Total
            case(8):
                items = line.split("\t");
                if(items.length < 2) return false;
                if(!(items[0].equals("Total") && items[1].contains("$"))) return false;
                items[1] = items[1].replace("$","");
                targetReceipt.setTotalSpent(items[1]);
                return true;

            // Check Change Due
            case(9):
                items = line.split(" ");
                if(items.length != 3) return false;
                return (line.contains("Change Due $"));

            // Check "Check Closed"
            case(10):
                return (line.equals("Check Closed"));

            // Check date/time again (in case it was missed the first time
            case(11):
                items = line.split(" ");
                itemsLength = items.length;
                if(itemsLength > 3) return false;
                // check date format
                if(itemsLength > 0){
                    DateFormat sourceFormat = new SimpleDateFormat("dd/MM/yyyy");
                    try{
                        sourceFormat.parse(items[0]);
                    }
                    catch (ParseException e){ return false;}
                }
                // check time format
                if(itemsLength > 1){
                    String[] time = items[1].split(":");
                    if(time.length != 2) return false;
                    try{
                        int hour = Integer.parseInt(time[0]);
                        if(!(hour >= 0 && hour < 24)) return false;
                    }
                    catch (NumberFormatException e){ return false;}
                    try{
                        int minute = Integer.parseInt(time[1]);
                        if(!(minute >= 0 && minute <= 59)) return false;
                    }
                    catch (NumberFormatException e){ return false;}
                }
                // check if AM PM
                if(itemsLength > 2){
                    if(!(items[2].equals("AM") || items[2].equals("PM"))) return false;
                }
                targetReceipt.setDate(items[0]);
                targetReceipt.setTime(items[1] + items[2]);
                return true;
            default:
                return false;
        }
    }
}
