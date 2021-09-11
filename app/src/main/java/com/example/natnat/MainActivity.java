package com.example.natnat;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ActivityOptions;
import android.content.Intent;
import android.database.CursorWindow;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;

public class MainActivity extends AppCompatActivity implements UpdateDbInterface {
    private Database db;
    private RecyclerView ranking;
    protected RecyclerView.Adapter<?> rankAdapter;

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
    }

    @Override
    protected void onResume() {
        super.onResume();
        ArrayList<NatItem> rankList = db.getNatEntries();
        Collections.sort(rankList);

        RecyclerView.Adapter<?> rankAdapter = new RankAdapter(MainActivity.this, rankList, db, this);
        ranking.setAdapter(rankAdapter);
    }

    @Override
    public void removeEntry(NatItem natItem) {
        db.removeNatEntry(natItem);
        setAdapter();
    }

    private void setAdapter(){
        ArrayList<NatItem> rankList = db.getNatEntries();
        Collections.sort(rankList);
        rankAdapter = new RankAdapter(MainActivity.this, rankList, db, this);
        ranking.setAdapter(rankAdapter);
        rankAdapter.notifyDataSetChanged();
    }
}