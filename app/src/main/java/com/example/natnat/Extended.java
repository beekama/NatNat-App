package com.example.natnat;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class Extended extends AppCompatActivity {
    private Database db;
    protected byte[] img;
    protected ImageButton image;
    NatItem natItem;
    private Uri originImageUri;
    Bitmap bitmap;
    private Boolean EDIT = false;

    public void onCreate(Bundle savedInstanceState) {

        db = new Database(this);

        Intent intent = getIntent();
        if (intent != null) {
            Bundle bundle = intent.getExtras();
            String meal = intent.getStringExtra("meal");
            String res = bundle.getString("restaurant");
            Log.wtf("meal", meal);
            Log.wtf("restaurant", res);
            natItem = db.getNatItemByPK(res, meal);
        }

        super.onCreate(savedInstanceState);
        View layout = findViewById(R.id.extended);
        setContentView(R.layout.extended);


        /* Setup Toolbar */
        Toolbar toolbar = findViewById(R.id.toolbar);
        TextView toolbarTitle = findViewById(R.id.toolbar_title);
        toolbarTitle.setText(R.string.extended);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        final ImageButton backHome = findViewById(R.id.toolbar_back);
        backHome.setOnClickListener((v -> finish()));
        backHome.setImageResource(R.drawable.ic_baseline_close_24);


        final TextView tvRestaurant = findViewById(R.id.tvRestaurant);
        TextView tvMeal = findViewById(R.id.tvName);
        TextView tvKnusper = findViewById(R.id.tvKnusper);
        TextView tvSize = findViewById(R.id.tvSize);
        TextView tvBeilagen = findViewById(R.id.tvBeilagen);
        TextView tvGeschmack = findViewById(R.id.tvGeschmack);
        TextView tvPreis = findViewById(R.id.tvPreis);
        TextView tvNotice = findViewById(R.id.tvNotice);
        TextView tvGesamt = findViewById(R.id.tvGesamt);

        tvRestaurant.setText(R.string.restaurant);
        tvMeal.setText(R.string.meal);
        tvKnusper.setText(R.string.knusper);
        tvSize.setText(R.string.size);
        tvBeilagen.setText(R.string.beilagen);
        tvGeschmack.setText(R.string.taste);
        tvPreis.setText(R.string.preis);
        tvNotice.setText(R.string.notice);
        tvGesamt.setText(R.string.gesamt);


        TextView etRestaurant = findViewById(R.id.etRestaurant);
        TextView etMeal = findViewById(R.id.etName);
        EditText etNotice = findViewById(R.id.etNotice);

        etRestaurant.setText(natItem.restaurantName);
        etMeal.setText(natItem.meal);
        etNotice.setText(natItem.notice);


        RatingBar rbKnusper = findViewById(R.id.extratingKnusper);
        RatingBar rbSize = findViewById(R.id.extratingSize);
        RatingBar rbBeilagen = findViewById(R.id.extratingBeilagen);
        RatingBar rbGeschmack = findViewById(R.id.extratingGeschmack);
        RatingBar rbPreis = findViewById(R.id.extratingPreis);
        RatingBar rbGesamt = findViewById(R.id.extratingGesamt);

        rbKnusper.setRating(natItem.knusper);
        rbSize.setRating(natItem.size);
        rbBeilagen.setRating(natItem.beilagen);
        rbGeschmack.setRating(natItem.geschmack);
        rbPreis.setRating(natItem.preis);
        rbGesamt.setRating(natItem.gesamt);


        image = findViewById(R.id.foto);
        Button edit = findViewById(R.id.edit);
        edit.setText(R.string.edit);
        String imagePath = natItem.imagePath;

        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (EDIT) {
                    rbKnusper.setIsIndicator(true);
                    rbSize.setIsIndicator(true);
                    rbBeilagen.setIsIndicator(true);
                    rbGeschmack.setIsIndicator(true);
                    rbPreis.setIsIndicator(true);
                    rbGesamt.setIsIndicator(true);

                    edit.setText(R.string.edit);
                    etNotice.setFocusable(false);
                    image.setOnClickListener(null);
                    edit.setOnFocusChangeListener(null);
                    collectData(natItem.restaurantName, natItem.meal, rbKnusper, rbSize, rbBeilagen, rbGeschmack, rbPreis, natItem.notice);
                    hideKeyboard();

                    EDIT = false;
                } else {
                    rbKnusper.setIsIndicator(false);
                    rbSize.setIsIndicator(false);
                    rbBeilagen.setIsIndicator(false);
                    rbGeschmack.setIsIndicator(false);
                    rbPreis.setIsIndicator(false);
                    rbGesamt.setIsIndicator(false);
                    etNotice.setFocusable(true);

                    edit.setText(R.string.submit);
                    etNotice.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                        @Override
                        public void onFocusChange(View v, boolean hasFocus) {
                            if (!hasFocus) {
                                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Adding.INPUT_METHOD_SERVICE);
                                inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
                            }
                        }
                    });

                    image.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Utils.verifyStoragePermission(Extended.this);
                            getUri.launch(new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI));
                        }
                    });
                    EDIT = true;
                }
            }
        });

        try {
            if (imagePath != null) {
                File f = new File(imagePath);
                if (f != null) {
                    bitmap = BitmapFactory.decodeStream(new FileInputStream(f));
                    image.setImageBitmap(bitmap);
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /* looking for image in internal/external storage */
    ActivityResultLauncher<Intent> getUri = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent intent = result.getData();
                        originImageUri = intent.getData();
                        bitmap = null;
                        try {
                            bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), originImageUri);
                            image.setImageBitmap(bitmap);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });

    private void collectData(String restaurant, String meal, RatingBar rbKnusper, RatingBar rbSize, RatingBar rbBeilagen, RatingBar rbGeschmack, RatingBar rbPreis, String notice) {
        NatItem newNat = null;
        try {
            String imgPath = null;

            Float knusper = rbKnusper.getRating();
            Float size = rbSize.getRating();
            Float beilagen = rbBeilagen.getRating();
            Float taste = rbGeschmack.getRating();
            Float preis = rbPreis.getRating();

            if (bitmap != null) {
                if (bitmap.getWidth() < bitmap.getHeight()) {
                    int rotate = Utils.getImageOrientation(Extended.this, originImageUri, originImageUri.getPath());
                    bitmap = Utils.rotateBitmap(bitmap, rotate);
                }

                ContextWrapper cw = new ContextWrapper(getApplicationContext());
                File dir = cw.getDir("imageDir", Context.MODE_PRIVATE);
                String newPath = String.format("%s_%s.png", restaurant, meal);
                File myImg = new File(dir, newPath);
                imgPath = myImg.getAbsolutePath();
                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(myImg);
                    bitmap.compress(Bitmap.CompressFormat.PNG, 50, fos);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            ContentValues cv = new ContentValues();
            cv.put("knusper", knusper);
            cv.put("size", size);
            cv.put("beilagen", beilagen);
            cv.put("geschmack", taste);
            cv.put("preis", preis);
            cv.put("imagePath", imgPath);
            db.editEntry(natItem, cv);
        } catch (IllegalArgumentException e) {
            Toast toast = Toast.makeText(getApplicationContext(), "Restaurant and meal field must not be empty!", Toast.LENGTH_LONG);
            toast.show();
        }
    }


    public void hideKeyboard() {
        View focusedView = getCurrentFocus();
        if (focusedView == null) return;
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(focusedView.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        focusedView.clearFocus();
    }
}
