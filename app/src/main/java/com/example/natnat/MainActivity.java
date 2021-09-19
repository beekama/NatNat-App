package com.example.natnat;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.app.VoiceInteractor;
import android.app.usage.StorageStatsManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.CursorWindow;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;

public class MainActivity extends AppCompatActivity implements UpdateDbInterface {
    private Database db;
    private RecyclerView ranking;
    protected RecyclerView.Adapter<?> rankAdapter;

    private static final int JSON_INDENT = 2;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        db = new Database(this);

        /*        *//* On default the CursorWindowSize you need to send/receive data from/to sqlite is too small for bigger images *//*  //outdated now saving in internal storage
        try {
            Field field = CursorWindow.class.getDeclaredField("sCursorWindowSize");
            field.setAccessible(true);
            field.set(null, 100 * 1024 * 1024); //the 100MB is the new size
        } catch (Exception e) {
            Log.wtf("naaaat","Cursor window size too small");
        }*/


        /* Setup Toolbar */
        Toolbar toolbar = findViewById(R.id.toolbar);
        TextView toolbarTitle = findViewById(R.id.toolbar_title);
        toolbarTitle.setText(R.string.toolbarHeader);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        /* Add new Nat-Entry */
        Button addButton = findViewById(R.id.addbutton);
        addButton.setText(R.string.add);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent adding = new Intent(v.getContext(), Adding.class);
                startActivity(adding, ActivityOptions.makeSceneTransitionAnimation(MainActivity.this).toBundle());
            }
        });

        /* rank with existing Nat-Entries */
        ranking = findViewById(R.id.ranklist);
        LinearLayoutManager rankingLayoutManager = new LinearLayoutManager(MainActivity.this, LinearLayoutManager.VERTICAL, false);
        ranking.setLayoutManager(rankingLayoutManager);
        setAdapter();

        /* import/export */
        ImageButton exportButton = findViewById(R.id.toolbar_back);
        exportButton.setImageResource(R.drawable.ic_baseline_cloud_upload_24);
        exportButton.setOnClickListener(v -> {
            Intent fileDialog = new Intent(Intent.ACTION_CREATE_DOCUMENT);
            fileDialog.addCategory(Intent.CATEGORY_OPENABLE);
            fileDialog.setType("text/plain");
            putUri.launch(fileDialog);
        });

        ImageButton importButton = findViewById(R.id.toolbar_right);
        importButton.setImageResource(R.drawable.ic_baseline_cloud_download_24);
        importButton.setOnClickListener(v -> {
            Intent fileDialog = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            fileDialog.addCategory(Intent.CATEGORY_OPENABLE);
            fileDialog.setType("text/plain");
            getUri.launch(fileDialog);
            setAdapter();
        });
    }

    /* save JSON_export_backup in files */
    ActivityResultLauncher<Intent> putUri = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        OutputStream outputStream;
                        Uri uri = result.getData().getData();
                        try {
                            outputStream = getContentResolver().openOutputStream(uri);
                            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(outputStream));
                            JSONObject json = db.exportDatabase();
                            if (json == null) {
                                Toast.makeText(MainActivity.this, "Empty database - export canceled!", Toast.LENGTH_LONG).show();
                                return;
                            }
                            bw.write(json.toString(JSON_INDENT));
                            bw.flush();
                            bw.close();
                        } catch (IOException e) {
                            Toast error = Toast.makeText(MainActivity.this, "IO Exception: " + e.getMessage(), Toast.LENGTH_LONG);
                            error.show();
                        } catch (JSONException e) {
                            Toast error = Toast.makeText(MainActivity.this, "Export failed: " + e.getMessage(), Toast.LENGTH_LONG);
                            error.show();
                        }
                        Toast noticeExportSuccess = Toast.makeText(MainActivity.this, "Nat-Database exported successfully!", Toast.LENGTH_LONG);
                        noticeExportSuccess.show();
                    } else if (result.getResultCode() == Activity.RESULT_CANCELED) {
                        Toast noticeExportCancelled = Toast.makeText(MainActivity.this, "Nat-Database export canceled!", Toast.LENGTH_LONG);
                        noticeExportCancelled.show();
                    }

                }
            });

    /* get JSON_import_backpu from files */
    ActivityResultLauncher<Intent> getUri = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        final InputStream inputStream;
                        final StringBuilder inJson = new StringBuilder();
                        try {
                            Uri uri = result.getData().getData();
                            inputStream = getContentResolver().openInputStream(uri);
                            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                            bufferedReader.lines().forEach(inJson::append);
                            bufferedReader.close();
                            JSONObject json = new JSONObject(inJson.toString());

                            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                            builder.setMessage("Do you want to replace existing Nat-Entries?");
                            builder.setPositiveButton("Replace", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    try {
                                        db.importDatabase(json, true);
                                        setAdapter();
                                    } catch (JSONException e) {
                                        Toast error = Toast.makeText(MainActivity.this, "JSON Parse Error: " + e.getMessage(), Toast.LENGTH_LONG);
                                        error.show();
                                    }
                                }

                            }).setNegativeButton("Skip", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    try {
                                        db.importDatabase(json, false);
                                        setAdapter();
                                    } catch (JSONException e) {
                                        Toast error = Toast.makeText(MainActivity.this, "JSON Parse Error: " + e.getMessage(), Toast.LENGTH_LONG);
                                        error.show();
                                    }
                                }
                            });
                            builder.show();
                            Toast noticeImportSuccess = Toast.makeText(MainActivity.this, "Nat-Database imported successfully!", Toast.LENGTH_LONG);
                            noticeImportSuccess.show();
                        } catch (IOException e) {
                            Toast error = Toast.makeText(MainActivity.this, "IO Exception: " + e.getMessage(), Toast.LENGTH_LONG);
                            error.show();
                        } catch (JSONException e) {
                            Toast error = Toast.makeText(MainActivity.this, "JSON Parse Error: " + e.getMessage(), Toast.LENGTH_LONG);
                            error.show();
                        }
                    } else if (result.getResultCode() == Activity.RESULT_CANCELED) {
                        Toast noticeExportCancelled = Toast.makeText(MainActivity.this, "Nat-Database import canceled!", Toast.LENGTH_LONG);
                        noticeExportCancelled.show();
                    }
                }
            });


    @Override
    protected void onResume() {
        super.onResume();
        ArrayList<NatItem> rankList = db.getNatEntries();
        Collections.sort(rankList);

        RecyclerView.Adapter<?> rankAdapter = new RankAdapter(MainActivity.this, rankList, db, this);
        ranking.setAdapter(rankAdapter);
        rankAdapter.notifyDataSetChanged();
    }

    @Override
    public void removeEntry(NatItem natItem) {
        db.removeNatEntry(natItem);
        setAdapter();
    }

    private void setAdapter() {
        ArrayList<NatItem> rankList = db.getNatEntries();
        Collections.sort(rankList);
        rankAdapter = new RankAdapter(MainActivity.this, rankList, db, this);
        ranking.setAdapter(rankAdapter);
        rankAdapter.notifyDataSetChanged();
    }
}