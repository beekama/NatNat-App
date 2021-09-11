package com.example.natnat;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;

public class RankAdapter extends RecyclerView.Adapter {

    private Context context;
    private ArrayList<NatItem> items;
    Database db;
    UpdateDbInterface iface;

    public RankAdapter(Context context, ArrayList<NatItem> items, Database db, UpdateDbInterface iface) {
        this.context = context;
        this.items = items;
        this.db = db;
        this.iface = iface;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert inflater != null;
        View view = inflater.inflate(R.layout.rank_nat_item, parent, false);
        return new LocalViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        LocalViewHolder lvh = (LocalViewHolder) holder;
        lvh.rank.setText(Integer.toString(position + 1));
        lvh.textView.setText(items.get(position).restaurantName);
        lvh.textView.setTextSize(18);
        lvh.meal.setText(items.get(position).meal);
        lvh.textView.setTextSize(15);
        lvh.stars.setRating(items.get(position).gesamt);

        try {
            if(items.get(position).imagePath != null){
            File f = new File(items.get(position).imagePath);
            if (f != null) {
                Bitmap bitmap = BitmapFactory.decodeStream(new FileInputStream(f));
                lvh.imageView.setImageBitmap(bitmap);
            }}
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        lvh.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString("restaurant", items.get(position).restaurantName);
                Intent myIntent = new Intent(v.getContext(), Extended.class);
                myIntent.putExtra("meal", items.get(position).meal);
                myIntent.putExtras(bundle);
                v.getContext().startActivity(myIntent);
            }
        });
        lvh.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                DialogInterface.OnClickListener dialogClickList = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE:
                                iface.removeEntry(items.get(position));
                                break;
                            case DialogInterface.BUTTON_NEGATIVE:
                                break;
                        }
                    }
                };
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage("Do you really want to remove this NatEntry?").setPositiveButton("Yes", dialogClickList).setNegativeButton("Cancel", dialogClickList).show();
                Toast toast = Toast.makeText(context, "successfully removed item", Toast.LENGTH_LONG);
                toast.show();
                return true;
            }
        });

    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class LocalViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        final TextView rank;
        final TextView meal;
        final TextView textView;
        final ImageView imageView;
        final RatingBar stars;

        LocalViewHolder(View itemView) {
            super(itemView);
            rank = itemView.findViewById(R.id.rank);
            meal = itemView.findViewById(R.id.tvMeal);
            textView = itemView.findViewById(R.id.tvRestaurant);
            itemView.setOnClickListener(this);
            imageView = itemView.findViewById(R.id.ivKnusperNat);
            stars = itemView.findViewById(R.id.stars);
        }

        @Override
        public void onClick(View view) {

        }
    }
}
