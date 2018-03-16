package info.devexchanges.textrecognization;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.FileProvider;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.CommonStatusCodes;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;




/*
    This runs the main page of the application.


    Stores the main instance.


    Sets up and maintains the database.


    Submits requests to add, edit, delete, and display information about the users Receipt data.
 */

public class MainActivity extends AppCompatActivity implements ReceiptListFragment.OnFragmentInteractionListener {

    // Unique numbers to create an intent for camera/image intents
    public static final int REQUEST_IMAGE_CAPTURE = 9001;
    public static final int LOAD_IMAGE = 9002;
    public static final int OCR_LOAD_IMAGE = 9003;
    static final int VIEW_RECEIPT = 9004;
    Uri photoURI;
    public static int receiptID;
    public ArrayList<ReceiptObject> receiptList;
    public HashSet<Integer> selectedIndices = new HashSet<>();
    public ArrayAdapter<ReceiptObject> receiptAdapter;
    private ReceiptListFragment receiptListFragment;
    private GraphFragment graphFragment;
    private DBHandler dbHandler;

    public MenuItem edit;
    public MenuItem delete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receiptlist);

        // Receipt features
        receiptList = new ArrayList<>();  // Stores the ReceiptObjects for the application

        // Database features.
        dbHandler = new DBHandler(this); // Creates a database instance to be populated by the stored files.
        loadReceipts();  // Loads information from the database on the phone into the current application

        // FIXME: Comment out the statement below to remove fake data.
        addFakeData();

        // Instantiates the receiptList if not done already.
        if(receiptList.size() == 0) receiptID = 0;
        else receiptID = receiptList.size();

        receiptListFragment = ReceiptListFragment.newInstance(receiptList, MainActivity.this);
        graphFragment = GraphFragment.newInstance(receiptList, this);

        receiptAdapter = new ArrayAdapter<ReceiptObject>(this, R.layout.reciept_list_item, receiptList) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if(convertView == null) {
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.reciept_list_item, parent, false);
                }
                ((TextView)convertView.findViewById(R.id.name)).setText(getItem(position).getReceiptName());
                ((TextView)convertView.findViewById(R.id.date)).setText(getItem(position).getDate());
                ((TextView)convertView.findViewById(R.id.price)).setText(getItem(position).getTotalSpent());
                if(selectedIndices.contains(position)) {
                    ((TextView) convertView.findViewById(R.id.checkBox)).setVisibility(View.VISIBLE);
                }
                else {
                    ((TextView) convertView.findViewById(R.id.checkBox)).setVisibility(View.GONE);
                }
                return convertView;
            }
        };

        ViewPager tabs = findViewById(R.id.tabs);
        tabs.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                if(position == 0) {
                    return graphFragment;
                }
                else if(position == 1) {
                    return receiptListFragment;
                }
                return null;
            }

            @Override
            public int getCount() {
                return 2;
            }

            @Override
            public CharSequence getPageTitle(int position) {
                if(position == 0) {
                    return "graph";
                }
                else {
                    return "List";
                }
            }
        });

        TabLayout tabView = findViewById(R.id.tabLayout);
        tabView.setupWithViewPager(tabs);
    }

    @Override
    protected void onStop() {
        super.onStop();
        saveReceipts();
    }

    // Invoked with the circle + button on main is clicked
    public void addButtonClicked(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.menu_new_receipt, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if(item.getItemId() == R.id.cameraMenuItem) {
                    readFromCamera();
                }
                if(item.getItemId() == R.id.galleryMenuItem) {
                    readFromGallery();
                }
                if(item.getItemId() == R.id.textMenuItem) {
                    newBlankReceipt();
                }
                return false;
            }
        });
        popup.show();
    }

    // Invoked when "Camera" button is clicked.
    public void readFromCamera() {

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            try {
                photoURI = FileProvider.getUriForFile(MainActivity.this,
                        BuildConfig.APPLICATION_ID + ".provider", createImageFile());
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            } catch (IOException ex) {
                // Error occurred while creating the File
                Toast t = Toast.makeText(this,
                        "Couldn't create a location to store a captured image",
                        Toast.LENGTH_SHORT);
                t.show();
            }
            catch (SecurityException se){
                Toast t = Toast.makeText(this,
                        "No Camera Permissions set",
                        Toast.LENGTH_SHORT);
                t.show();
            }
        }}

    private File createImageFile() throws IOException {
        if(!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())){
            return null;
        }
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = new File(getApplicationContext().getExternalFilesDir(
                Environment.DIRECTORY_PICTURES), "${applicationId}");
        if(!storageDir.exists()){
            if(!storageDir.mkdirs()) return null;
        }
        return File.createTempFile(imageFileName,".jpg", storageDir);
    }


    // Invoked when "Gallery" button is clicked.
    public void readFromGallery(){
        Intent grabLocalImage = new Intent(Intent.ACTION_PICK);
        File imageDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        String imageDirectoryPath = imageDirectory.getPath();
        Uri data = Uri.parse(imageDirectoryPath);
        // specify MIME (Multiple Internet Messaging Extensions) type: all image types
        grabLocalImage.setDataAndType(data, "image/*");
        startActivityForResult(grabLocalImage, LOAD_IMAGE);
    }

    // Invoked when "Blank Receipt" button is clicked.
    public void newBlankReceipt() {
        Intent intent = new Intent(this, ViewReceiptActivity.class);
        intent.putExtra("receiptItem", new ReceiptObject(receiptID+ 1));
        intent.putExtra("receipt-index", -1);
        intent.putExtra("editMode", true);

        startActivityForResult(intent, VIEW_RECEIPT);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_receiptlist, menu);
        edit = menu.findItem(R.id.edit);
        delete = menu.findItem(R.id.delete);
        edit.setVisible(false);
        delete.setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.edit) {
            onClickEdit(item);
        }
        else if(item.getItemId() == R.id.delete) {
            onClickDelete(item);
            delete.setVisible(false);
        }
        return super.onOptionsItemSelected(item);
    }

    // Invoked when a receipt is tapped
    public void onClickEdit(MenuItem v) {

        int position = selectedIndices.iterator().next();
        ReceiptObject clicked = receiptList.get(position);
        Intent intent = new Intent(MainActivity.this, ViewReceiptActivity.class);
        intent.putExtra("receiptItem", clicked);
        intent.putExtra("receipt-index", position);
        intent.putExtra("editMode", true);

        selectedIndices.clear();
        edit.setVisible(false);
        delete.setVisible(false);

        startActivityForResult(intent, VIEW_RECEIPT);
    }

    public void onClickDelete(MenuItem v) {
        new AlertDialog.Builder(this)
                .setMessage("Are you sure you want to delete this receipt?")
                .setPositiveButton("delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ArrayList<Integer> indices = new ArrayList<>();
                        indices.addAll(selectedIndices);
//                        Collections.sort(indices);
                        for(int i = indices.size() - 1; i >= 0; i--) {
                            receiptList.remove(indices.get(i).intValue());
                        }
                        receiptAdapter.notifyDataSetChanged();
                        graphFragment.updateGraph();
                        selectedIndices.clear();
                        edit.setVisible(false);
                        delete.setVisible(false);
                        saveReceipts();
                    }
                })
                .setNegativeButton("cancel", null)
                .show();

    }
    /*
        Loads the information from the database into the receiptList
        for use in the application.
     */
    public void loadReceipts(){
        ArrayList<ArrayList<String>> loadedReceipts = dbHandler.loadReceiptHandler();
        ArrayList<ArrayList<String>> loadedItems = dbHandler.loadItemsHandler();
        for(int i = 0; i < loadedReceipts.size(); i++) {
            int tempCount = 0;

            ArrayList<String> tempReceiptArray = loadedReceipts.get(i);
            int receiptID = Integer.parseInt(tempReceiptArray.get(0));
            ReceiptObject tempReceipt = new ReceiptObject(receiptID);
            tempReceipt.setReceiptName(tempReceiptArray.get(1));
            tempReceipt.setDate(tempReceiptArray.get(2));
            tempReceipt.setTime(tempReceiptArray.get(3));
            tempReceipt.setAddress1(tempReceiptArray.get(4));
            tempReceipt.setCity(tempReceiptArray.get(5));
            tempReceipt.setState(tempReceiptArray.get(6));
            tempReceipt.setPhoneNumber(tempReceiptArray.get(7));
            tempReceipt.setTotalSpent(tempReceiptArray.get(8));

            if (loadedItems.size() > 0) {
                ArrayList<String> currItem = loadedItems.get(tempCount);

                while (tempCount < loadedItems.size() && currItem.size() > 2) {
                    tempReceipt.addItem(currItem.get(1), currItem.get(2));
                    currItem = loadedItems.get(tempCount++);
                }
                receiptList.add(tempReceipt);
            }}}

    /*
        Saves the data in the current instance of the app for later use
        Wipes the database information first then reloads with current instance.
     */
    public void saveReceipts(){

        // Wipe the database first
        dbHandler.deleteReceiptTableHandler();
        dbHandler.deleteItemTableHandler();

        // Repopulates the database with current info

        int listSize = receiptList.size();
        if (listSize != 0){
            for (int i = 0; i<listSize; i++){
                dbHandler.addReceiptHandler(receiptList.get(i));
                ArrayList<Item> tempReceiptItems = receiptList.get(i).getReceiptItems();
                for (int j = 0; j<tempReceiptItems.size(); j++){
                    dbHandler.addItemHandler(tempReceiptItems.get(j));
                }
            }}}

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        int baseRequestCode = requestCode & 0xffff; //apparently when you startactivityforresult from a fragment it puts random stuff in higher bits.
        if(baseRequestCode == VIEW_RECEIPT && resultCode == RESULT_OK) {
            ReceiptObject receipt = data.getParcelableExtra("receipt-return");
            int index = data.getIntExtra("receipt-index", 0);
            if(index == -1 && receipt != null) {
                receiptList.add(receipt);
            }
            else if(index != -1){
                if(receipt == null) {
                    receiptList.remove(index);
                }
                else {
                    receiptList.set(index, receipt);
                }
            }
            receiptAdapter.notifyDataSetChanged();
            graphFragment.updateGraph();
            saveReceipts();
        }
        else if(baseRequestCode == REQUEST_IMAGE_CAPTURE) {
            if (resultCode == RESULT_OK){
                Intent intent = new Intent(this, ConvertImageToText.class);
                intent.setDataAndType(photoURI, "image/jpeg");
                intent.putExtra("source", "camera");
                startActivityForResult(intent, OCR_LOAD_IMAGE);
            } else {
                Toast t = Toast.makeText(this, "Error capturing image", Toast.LENGTH_SHORT);
                t.show();
            }
        }
        // if returned result is an image from onGalleryImageClicked
        else if(baseRequestCode == LOAD_IMAGE){
            if((resultCode  == CommonStatusCodes.SUCCESS || resultCode == CommonStatusCodes.SUCCESS_CACHE) && data != null){
                Uri imageUri = data.getData();
                Intent intent = new Intent(this, ConvertImageToText.class);
                intent.putExtra("imageUri", imageUri);
                intent.putExtra("source", "gallery");
                startActivityForResult(intent, OCR_LOAD_IMAGE);
            }
            else {
                String result = "Getting Image Failed: ";
                //statusMessage.setText(result);
                Toast t = Toast.makeText(this, result, Toast.LENGTH_SHORT);
                t.show();
            }
        }
        else if(baseRequestCode == OCR_LOAD_IMAGE){
            if(resultCode == CommonStatusCodes.SUCCESS && data != null){
                boolean receiptAdded = data.getBooleanExtra("result", false);
                if(receiptAdded){

                    // create a new receipt, pass in name (number)
                    Toast t = Toast.makeText(this, R.string.ocr_success, Toast.LENGTH_SHORT);
                    t.show();
                    ReceiptObject newReceipt = data.getParcelableExtra("receiptItem");
                    Intent intent = new Intent(MainActivity.this, ViewReceiptActivity.class);
                    intent.putExtra("receiptItem", newReceipt);
                    intent.putExtra("receipt-index", -1);
                    intent.putExtra("editMode", true);
                    startActivityForResult(intent, VIEW_RECEIPT);
                }
                else{
                    Toast t = Toast.makeText(this, R.string.ocr_failure, Toast.LENGTH_SHORT);
                    t.show();
                }
            }
            else {
                String result = "Getting Image Failed";
                Toast t = Toast.makeText(this, result, Toast.LENGTH_SHORT);
                t.show();
            }
        }
        // else returned result is not from OcrCapture or ConvertImageToText
        else {super.onActivityResult(baseRequestCode, resultCode, data);}
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }


    /**
     * Created by Yehui. March 11, 2018
     * This method is for testing purpose only.
     * 20 entries will be added to receiptList.
     * The dates ranges from Aug 2017 to Feb 2018
     * The total ranges from 5 to 30.
     *
     */
    private void addFakeData(){
        receiptList.add(new ReceiptObject("2","02/24/2018","Starbucks Store #12345","20.22"));

        receiptList.add(new ReceiptObject("3","02/22/2018","Starbucks Store #14132","25.15"));

        receiptList.add(new ReceiptObject("6","02/13/2018","Starbucks Store #12345","11.52"));

        receiptList.add(new ReceiptObject("12","01/01/2018","Starbucks Store #12345","22.61"));

        receiptList.add(new ReceiptObject("14","12/06/2017","Starbucks Store #22251","28.66"));

        receiptList.add(new ReceiptObject("15","12/04/2017","Starbucks Store #14132","16.63"));

        receiptList.add(new ReceiptObject("20","11/11/2017","Starbucks Store #12345","24.04"));

        receiptList.add(new ReceiptObject("22","10/22/2017","Starbucks Store #12345","21.52"));

        receiptList.add(new ReceiptObject("23","10/17/2017","Starbucks Store #14132","8.03"));

        receiptList.add(new ReceiptObject("26","10/06/2017","Starbucks Store #12345","12.35"));

        receiptList.add(new ReceiptObject("35","09/03/2017","Starbucks Store #12345","10.23"));

        receiptList.add(new ReceiptObject("36","09/03/2017","Starbucks Store #76898","19.53"));

        receiptList.add(new ReceiptObject("37","08/24/2017","Starbucks Store #14132","14.15"));

        receiptList.add(new ReceiptObject("39","08/08/2017","Starbucks Store #12345","12.05"));

        receiptList.add(new ReceiptObject("40","08/06/2017","Starbucks Store #12345","17.59"));

        receiptList.add(new ReceiptObject("46","07/01/2017","Starbucks Store #14132","28.23"));
    }
}
