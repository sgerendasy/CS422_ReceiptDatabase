package info.devexchanges.textrecognization;

import android.graphics.Point;

public class ReceiptTextRaw {
    private String description;
    private Point UL, UR, LL, LR;
    private ReceiptTextRaw previous;
    public ReceiptTextRaw next;
    // acceptable error range measured in radians.
    public static final double OCR_ACCEPTABLE_ERROR = 0.045;

    ReceiptTextRaw(String description, Point[] points){
        this.description = description;
        this.UL = points[0];
        this.UR = points[1];
        this.LL = points[2];
        this.LR = points[3];
        this.previous = null;
        this.next = null;
    }

    public String getDescription(){
        return description;
    }

    void placeNewItem(ReceiptTextRaw other){
        // sort Item in higher-to-lower order.
        ReceiptTextRaw current = this;
        // while item exists in linked list to traverse,
        // and the other item is lower than the current item
        while(current.next != null && other.UL.y > current.UL.y){
            current = current.next;
        }
        // if the other item is higher than the current item
        if(other.UL.y < current.UL.y){
            other.next = current;
            // if current isn't head
            if(current.previous != null){
                other.previous = current.previous;
                current.previous.next = other;
            }
        }
        // else if the other item is lower or equal to current item
        else{
            // if current isn't tail
            if(current.next != null){
                other.next = current.next;
                current.next.previous = other;
            }
            current.next = other;
            other.previous = current;
        }
    }

    private boolean sameLineAs(ReceiptTextRaw other){

        double a1y = Math.abs(this.UR.y - this.UL.y);
        double a1x = Math.abs(this.UR.x - this.UL.x);
        double a1 = Math.atan2(a1x, a1y);
        double a2x = Math.abs(other.UL.x - this.UL.x);
        double a2y = Math.abs(other.UL.y - this.UL.y);
        if(a2x == 0){
            return false;
        }
        double a2 = Math.atan2(a2x, a2y);
        return Math.abs(a1 - a2) < OCR_ACCEPTABLE_ERROR;
    }
    private boolean isLeftSide(ReceiptTextRaw other){
        // returns true if ReceiptTextRaw 'this' is left to receiptItem 'other'
        return this.UR.x < other.UR.x;
    }

    void consolidate(){
        ReceiptTextRaw current = this;
        while(current.next != null){
            if(current.sameLineAs(current.next)){
                if (this.isLeftSide(current.next)){
                    current.description += "\t";
                    current.description += current.next.description;
                }
                else{
                    String tempDescription = current.next.description;
                    tempDescription += "\t";
                    current.description = tempDescription + current.description;
                }
                current.next = current.next.next;
                if(current.next != null){
                    current.next.previous = current;
                }
                else{
                    break;
                }
            }
            current = current.next;
        }
    }
}
