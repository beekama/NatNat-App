package com.example.natnat;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class Adding extends AppCompatActivity {
    private Database db;
    protected Bitmap bitmap;
    protected ImageView image;
    protected Uri originImageUri;
    protected int rotate = 0;

    public void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        db = new Database(this);

        /* Setup Toolbar */
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        TextView toolbarTitle = findViewById(R.id.toolbar_title);
        toolbarTitle.setText(R.string.add);
        toolbar.setTitle("");

        View layout = findViewById(R.id.ratingLayout);


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

        tvRestaurant.setText(R.string.restaurant);
        tvMeal.setText(R.string.meal);
        tvKnusper.setText(R.string.knusper);
        tvSize.setText(R.string.size);
        tvBeilagen.setText(R.string.beilagen);
        tvGeschmack.setText(R.string.taste);
        tvPreis.setText(R.string.preis);
        tvNotice.setText(R.string.notice);


        EditText etRestaurant = findViewById(R.id.etRestaurant);
        EditText etMeal = findViewById(R.id.etName);
        EditText etNotice = findViewById(R.id.etNotice);
        RatingBar rbKnusper = findViewById(R.id.etKnusper);
        RatingBar rbSize = findViewById(R.id.etSize);
        RatingBar rbBeilagen = findViewById(R.id.etBeilagen);
        RatingBar rbGeschmack = findViewById(R.id.etGeschmack);
        RatingBar rbPreis = findViewById(R.id.etPreis);

        Button submit = findViewById(R.id.submit);
        submit.setText(R.string.submit);

        Button upload = findViewById(R.id.uploadButton);
        upload.setText(R.string.upload);

        image = findViewById(R.id.foto);


        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.verifyStoragePermission(Adding.this);
                getUri.launch(new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI));
                //    startActivityForResult(new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI), 1);
            }
        });


        // hide keyboard when background is clicked
        etRestaurant.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Adding.INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        });

        etMeal.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Adding.INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        });

        etNotice.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Adding.INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        });


        submit.setOnClickListener(v -> {
            collectData(etRestaurant, etMeal, rbKnusper, rbSize, rbBeilagen, rbGeschmack, rbPreis, etNotice);
            hideKeyboard();
            layout.clearFocus();
        });

    }


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
                            //  ByteArrayOutputStream bos = new ByteArrayOutputStream();
/*                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);
                            img = bos.toByteArray();*/
                            image.setImageBitmap(bitmap);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();

                        }
                    }
                }
            });


    private void collectData(EditText etRestaurant, EditText etMeal, RatingBar rbKnusper, RatingBar rbSize, RatingBar rbBeilagen, RatingBar rbGeschmack, RatingBar rbPreis, EditText etNotice) {
        NatItem newNat = null;
        try {
            String imgPath = null;
            String restaurant = etRestaurant.getText().toString();
            String meal = etMeal.getText().toString();
            if (restaurant.equals("") || meal.equals("")) {
                throw new IllegalArgumentException("Restaurant and meal field must not be empty!");
            }
            Float knusper = rbKnusper.getRating();
            Float size = rbSize.getRating();
            Float beilagen = rbBeilagen.getRating();
            Float taste = rbGeschmack.getRating();
            Float preis = rbPreis.getRating();
            String notice = etNotice.getText().toString();
            if (bitmap != null) {
                if (bitmap.getWidth() < bitmap.getHeight()) {
                    int rotate = Utils.getImageOrientation(Adding.this, originImageUri, originImageUri.getPath());
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
            newNat = new NatItem(restaurant, meal, notice, knusper, size, beilagen, taste, preis, imgPath);
            if (db.addNatEntry(newNat) == -1) {
                Toast alreadyExists = Toast.makeText(Adding.this, "A Nat like this already exists in the Database. ADDING FAILED!", Toast.LENGTH_LONG);
                alreadyExists.show();
            } else {
                Toast successfullyAdded = Toast.makeText(Adding.this, "successfully added new NatEntry", Toast.LENGTH_LONG);
                successfullyAdded.show();

            }
            finish();
        } catch (IllegalArgumentException e) {
            Toast toast = Toast.makeText(getApplicationContext(), "Restaurant and meal field must not be empty!", Toast.LENGTH_LONG);
            toast.show();
        }
    }

    private void hideKeyboard() {
        View focusedView = getCurrentFocus();
        if (focusedView == null) return;
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(focusedView.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        focusedView.clearFocus();
    }

}
