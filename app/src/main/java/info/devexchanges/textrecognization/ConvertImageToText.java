package info.devexchanges.textrecognization;

import  android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseArray;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import static info.devexchanges.textrecognization.MainActivity.receiptID;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;


public class ConvertImageToText extends AppCompatActivity {
    private static final String TAG = "OCRLoadImageActivity";
    Bitmap bmp;
    Uri imageUri;
    InputStream inputStream;

    public ReceiptTextRaw receiptHead = null;

    @Override
    public void onCreate(final Bundle bundle) {
        super.onCreate(bundle);

        String source = getIntent().getStringExtra("source");

        try {
            switch (source) {
                case "gallery":
                    imageUri = getIntent().getParcelableExtra("imageUri");
                    break;
                case "camera":
                    imageUri = getIntent().getData();
                    break;
                default:
                    return;
            }
            if(imageUri == null) return;
            inputStream = getContentResolver().openInputStream(imageUri);
            bmp = BitmapFactory.decodeStream(inputStream);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }


        // begin OCR on image
        TextRecognizer textRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();
        // Check if the TextRecognizer is operational.
        if (!textRecognizer.isOperational()) {
            // Check for low storage.  If there is low storage, the native library will not be
            // downloaded, so detection will not become operational.
            IntentFilter lowstorageFilter = new IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW);
            boolean hasLowStorage = registerReceiver(null, lowstorageFilter) != null;

            if (hasLowStorage) {
                Log.w(TAG, getString(R.string.low_storage_error));
            }
        } else {
            Frame frame = new Frame.Builder().setBitmap(bmp).build();
            SparseArray<TextBlock> items = textRecognizer.detect(frame);
            // create a linked list of receipt Item
            for (int j = 0; j < items.size(); j++) {
                List<? extends com.google.android.gms.vision.text.Text> list = items.valueAt(j).getComponents();
                for (int k = 0; k < list.size(); k++) {
                    Point[] points = list.get(k).getCornerPoints();
                    String description = list.get(k).getValue();
                    ReceiptTextRaw newItem = new ReceiptTextRaw(description, points);
                    if (receiptHead == null) {
                        receiptHead = newItem;
                    } else {
                        receiptHead.placeNewItem(newItem);
                    }
                }
            }
            Intent intent = new Intent();
            // default for nothing added
            intent.putExtra("result", false);
            if (receiptHead != null) {
                receiptHead.consolidate();
                // let intent know a receipt was added
                intent.putExtra("result", true);
                ReceiptObject newReceipt = new ReceiptObject(receiptID + 1);
                ParseReceiptData.parseAndPopulateReceipt(newReceipt, receiptHead);
                intent.putExtra("receiptItem", newReceipt);
            }
            setResult(CommonStatusCodes.SUCCESS, intent);
            finish();
        }
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}
