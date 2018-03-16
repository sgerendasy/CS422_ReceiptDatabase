package info.devexchanges.textrecognization;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import java.util.ArrayList;

import static info.devexchanges.textrecognization.MainActivity.receiptID;

public class ViewReceiptActivity extends AppCompatActivity {

    private ReceiptObject receipt;
    private ReceiptObject returnReceipt;

    private int returnIndex = 0;
    private TextViewEditSwitcher nameSwitcher;
    private TextViewEditSwitcher storeNumSwitcher;
    private TextViewEditSwitcher addressSwitcher;
    private TextViewEditSwitcher citySwitcher;
    private TextViewEditSwitcher stateSwitcher;
    private TextViewEditSwitcher phoneNumSwitcher;
    private TextViewEditSwitcher dateSwitcher;
    private TextViewEditSwitcher timeSwitcher;
    private TextViewEditSwitcher totalSwitcher;
    private LinearLayout itemList;

    private boolean editMode = false;
    private boolean prevActivityOnSave = true;
    private boolean deleteReceipt = false;

    private ArrayList<TextViewEditSwitcher> itemNames;
    private ArrayList<TextViewEditSwitcher> itemPrices;

    private ArrayList<TextViewEditSwitcher> switcherList = new ArrayList<>();
    private MenuItem edit;
    private MenuItem save;
    private MenuItem cancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_receipt);
        receipt = getIntent().getParcelableExtra("receiptItem");
        returnIndex = getIntent().getIntExtra("receipt-index", 0);
        editMode = getIntent().getBooleanExtra("editMode", false);

        if (receipt == null) {
            receipt = new ReceiptObject(0);
        }

        nameSwitcher = new TextViewEditSwitcher(receipt.getReceiptName(), R.id.receiptNameSwitcher, R.id.receiptNameEdit, R.id.receiptNameText, InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        storeNumSwitcher = new TextViewEditSwitcher(receipt.getStoreNumber(), R.id.storeNumSwitcher, R.id.storeNumEdit, R.id.storeNumText, InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        addressSwitcher = new TextViewEditSwitcher(receipt.getAddress1(), R.id.addressSwitcher, R.id.addressEdit, R.id.addressText, InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        citySwitcher = new TextViewEditSwitcher(receipt.getCity(), R.id.citySwitcher, R.id.cityEdit, R.id.cityText, InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        stateSwitcher = new TextViewEditSwitcher(receipt.getState(), R.id.stateSwitcher, R.id.stateEdit, R.id.stateText, InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        phoneNumSwitcher = new TextViewEditSwitcher(receipt.getPhoneNumber(), R.id.phoneNumSwitcher, R.id.phoneNumEdit, R.id.phoneNumText, InputType.TYPE_CLASS_NUMBER | InputType.TYPE_CLASS_PHONE);
        dateSwitcher = new TextViewEditSwitcher(receipt.getDate(), R.id.dateSwitcher, R.id.dayEdit, R.id.dayText, InputType.TYPE_CLASS_DATETIME | InputType.TYPE_DATETIME_VARIATION_DATE);
        timeSwitcher = new TextViewEditSwitcher(receipt.getTime(), R.id.timeSwitcher, R.id.timeEdit, R.id.timeText, InputType.TYPE_CLASS_DATETIME | InputType.TYPE_DATETIME_VARIATION_TIME);
        totalSwitcher = new TextViewEditSwitcher(receipt.getTotalSpent(), R.id.totalSwitcher, R.id.totalEdit, R.id.totalText, InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);

        updateTextFromReceipt(receipt);

        if(editMode) {
            changeToEditMode();
            prevActivityOnSave = true;
        }
        else {
            changeToViewMode();
            returnReceipt = receipt;
        }

        TextWatcher validateListener = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                validateFields();
            }
        };

        nameSwitcher.edit.addTextChangedListener(validateListener);
        dateSwitcher.edit.addTextChangedListener(validateListener);
        totalSwitcher.edit.addTextChangedListener(validateListener);

        validateFields();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_view_receipt, menu);
        edit = menu.findItem(R.id.edit);
        save = menu.findItem(R.id.save);
        cancel = menu.findItem(R.id.cancel);
        edit.setVisible(!editMode);
        save.setVisible(editMode);
        cancel.setVisible(editMode);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.edit) {
            changeToEditMode();
        }
        else if(item.getItemId() == R.id.save) {
            String error = updateReceiptFromText();
            if(error != null) {
                Toast t = Toast.makeText(this, error, Toast.LENGTH_SHORT);
                t.show();
            }
            else {
                if (!prevActivityOnSave) {
                    changeToViewMode();
                } else {
                    finish();
                }
            }
        }
        else if(item.getItemId() == R.id.cancel) {
            if(prevActivityOnSave) {
                returnReceipt = null;
                finish();
            }
            else {
                updateTextFromReceipt(receipt);
                changeToViewMode();
            }
        }
        else if(item.getItemId() == R.id.delete) {
            new AlertDialog.Builder(this)
                    .setMessage("Are you sure you want to delete this receipt?")
                    .setPositiveButton("delete", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            deleteReceipt = true;
                            finish();
                        }
                    })
                    .setNegativeButton("cancel", null)
                    .show();
        }
        return super.onOptionsItemSelected(item);
    }

    public void changeToViewMode() {
        editMode = false;
        for(TextViewEditSwitcher switcher:switcherList) {
            switcher.changeToText();
        }
        if(edit != null) {
            edit.setVisible(true);
        }
        if(save != null) {
            save.setVisible(false);
        }
        if(cancel != null) {
            cancel.setVisible(false);
        }
    }

    public void changeToEditMode() {
        editMode = true;
        for(TextViewEditSwitcher switcher:switcherList) {
            switcher.changeToEdit();
        }
        if(edit != null) {
            edit.setVisible(false);
        }
        if(save != null) {
            save.setVisible(true);
        }
        if(cancel != null) {
            cancel.setVisible(true);
        }
        prevActivityOnSave = false;//TODO move?
        nameSwitcher.edit.requestFocus();
    }

    public String updateReceiptFromText() {
        if(!validateFields()) {
            return "Error: missing required field";
        }
        returnReceipt = new ReceiptObject(++receiptID);
        returnReceipt.setReceiptName(nameSwitcher.getText());
        returnReceipt.setStoreNumber(storeNumSwitcher.getText());
        returnReceipt.setAddress1(addressSwitcher.getText());
        returnReceipt.setCity(citySwitcher.getText());
        returnReceipt.setState(stateSwitcher.getText());
        returnReceipt.setPhoneNumber(phoneNumSwitcher.getText());
        returnReceipt.setDate(dateSwitcher.getText());
        returnReceipt.setTime(timeSwitcher.getText());
        returnReceipt.setTotalSpent(totalSwitcher.getText());

        for(int i = 0; i < itemNames.size(); i++) {
            returnReceipt.addItem(itemNames.get(i).getText(), itemPrices.get(i).getText());
        }
        return null;
    }

    private boolean validateFields() {
        boolean error = false;
        if(nameSwitcher.getText().equals("")) {
            nameSwitcher.edit.setError("Name must not be blank");
            error = true;
        }

        if(dateSwitcher.getText().equals("")) {
            dateSwitcher.edit.setError("Date must not be blank");
            error = true;
        }
        else {
            String[] date = dateSwitcher.getText().split("/");
            if(date.length != 3) {
                dateSwitcher.edit.setError("Invalid date format");
                error = true;
            }
            else {
                for (String piece : date) {
                    try {
                        Integer.parseInt(piece);
                    } catch (NumberFormatException e) {
                        dateSwitcher.edit.setError("Invalid date format");
                        error = true;
                    }
                }
            }

        }

        if(totalSwitcher.getText().equals("")) {
            totalSwitcher.edit.setError("Total must not be blank");
            error = true;
        }
        else {
            try {
                Double.parseDouble(totalSwitcher.getText());
            }
            catch (NumberFormatException e) {
                totalSwitcher.edit.setError("Invalid format");
            }
        }

        return !error;
    }

    public void updateTextFromReceipt(ReceiptObject receipt) {
        nameSwitcher.setText(receipt.getReceiptName());
        storeNumSwitcher.setText(receipt.getStoreNumber());
        addressSwitcher.setText(receipt.getAddress1());
        citySwitcher.setText(receipt.getCity());
        stateSwitcher.setText(receipt.getState());
        phoneNumSwitcher.setText(receipt.getPhoneNumber());
        dateSwitcher.setText(receipt.getDate());
        timeSwitcher.setText(receipt.getTime());
        totalSwitcher.setText(receipt.getTotalSpent());

        if(itemNames != null) {
            for(TextViewEditSwitcher r:itemNames) {
                switcherList.remove(r);
            }
        }
        if(itemPrices != null) {
            for (TextViewEditSwitcher r : itemPrices) {
                switcherList.remove(r);
            }
        }

        itemNames = new ArrayList<>();
        itemPrices = new ArrayList<>();

        itemList = findViewById(R.id.itemsList);
        for(Item item:receipt.getReceiptItems()) {
            View view = this.getLayoutInflater().inflate(R.layout.receipt_item_price, null);
            view.findViewById(R.id.itemNameSwitcher);
            TextViewEditSwitcher nameListener = new TextViewEditSwitcher(item.getName(), (ViewSwitcher)view.findViewById(R.id.itemNameSwitcher),
                    (EditText)view.findViewById(R.id.itemNameEdit), (TextView)view.findViewById(R.id.itemNameText));
            TextViewEditSwitcher priceListener = new TextViewEditSwitcher(item.getPrice(), (ViewSwitcher)view.findViewById(R.id.itemPriceSwitcher),
                    (EditText)view.findViewById(R.id.itemPriceEdit), (TextView)view.findViewById(R.id.itemPriceText));
            itemNames.add(nameListener);
            itemPrices.add(priceListener);
            itemList.addView(view);
        }
    }

    @Override
    public void finish() {
        if(deleteReceipt) {
            returnReceipt = null;
        }
        else if(returnReceipt == null) {
            returnIndex = -1;
        }
        Intent returnIntent = new Intent();
        returnIntent.putExtra("receipt-return", returnReceipt);
        returnIntent.putExtra("receipt-index", returnIndex);
        setResult(RESULT_OK, returnIntent);

        super.finish();
    }

    //from here: https://stackoverflow.com/questions/4828636/edittext-clear-focus-on-touch-outside
    //clears text box focus if you tab outside it
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if ( v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int)event.getRawX(), (int)event.getRawY())) {
                    v.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        }
        return super.dispatchTouchEvent( event );
    }

    private class TextViewEditSwitcher implements View.OnClickListener, EditText.OnEditorActionListener, View.OnFocusChangeListener {

        private TextView text;
        private EditText edit;
        private ViewSwitcher switcher;
        private boolean isText;
        private int inputType;
        public boolean allowEmptyAsText = true;

        public TextViewEditSwitcher(String initialText, ViewSwitcher switcher, EditText edit, TextView text, int inputType) {
            this.switcher = switcher;
            this.edit = edit;
            this.text = text;
            this.inputType = inputType;
            isText = true;

            switcherList.add(this);

            text.setText(initialText);
            edit.setText(initialText);//this line is probably unnecessary
            edit.setInputType(inputType);
        }

        public TextViewEditSwitcher(String initialText, ViewSwitcher switcher, EditText edit, TextView text) {
            this(initialText, switcher, edit, text, InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        }

        public TextViewEditSwitcher(String initialText, int switcherID, int editID, int textID, int inputType) {
            this(initialText, (ViewSwitcher)findViewById(switcherID), (EditText)findViewById(editID), (TextView)findViewById(textID), inputType);
        }

        public TextViewEditSwitcher(String initialText, int switcherID, int editID, int textID) {
            this(initialText, switcherID, editID, textID, InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        }

        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if(actionId == EditorInfo.IME_ACTION_DONE) {
                changeToText();
                return true;
            }
            return false;
        }

        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (!hasFocus) {
                changeToText();
            }
        }

        @Override
        public void onClick(View v) {
            changeToEdit();
        }

        public String getText() {
            if(isText) {
                return text.getText().toString();
            }
            return edit.getText().toString();
        }

        public void setText(String newText) {
            if(isText) {
                text.setText(newText);
            }
            else {
                edit.setText(newText);
            }
        }

        public void changeToEdit() {
            if(isText) {
                isText = false;
                Log.i("Receipt", "switching to edit");
                switcher.showNext();
                edit.setInputType(inputType);
                edit.setText(text.getText());

                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(edit, 0);
            }
        }

        public void changeToText() {
            edit.clearFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(edit.getWindowToken(), 0);
            if((!getText().isEmpty() || allowEmptyAsText) && !isText) {
                isText = true;
                Log.i("Receipt", "switching to text");
                switcher.showNext();
                edit.setInputType(InputType.TYPE_NULL);
                text.setText(edit.getText());

            }
        }
    }
}

