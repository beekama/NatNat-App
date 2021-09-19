package com.example.natnat;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.apache.commons.io.IOUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;


public class Database {

    private static final String DATABASE_NAME = "nat.db";
    private static final String NAT_TABLE = "nat";
    private static SQLiteDatabase db = null;
    private final Activity activity;
    String targetPath;

    public Database(Activity srcActivity) {
        this.activity = srcActivity;
        if (db == null) {
            createDatabase(false);
            db = SQLiteDatabase.openDatabase(targetPath, null, SQLiteDatabase.NO_LOCALIZED_COLLATORS | SQLiteDatabase.OPEN_READWRITE);
        }
    }


    private void createDatabase(boolean overwrite) {
        targetPath = activity.getFilesDir().getParent() + "/" + DATABASE_NAME;
        File file = new File(targetPath);
        if (overwrite && file.exists()) {
            boolean removed = file.delete();
            if (!removed) {
                throw new AssertionError("Database could not be removed");
            }
        }
        if (!file.exists()) {
            InputStream in = activity.getResources().openRawResource(R.raw.nat);
            try {
                OutputStream out = new FileOutputStream(targetPath);
                IOUtil.copy(in, out);
                out.close();
                in.close();
            } catch (FileNotFoundException e) {
                throw new AssertionError("Database resource 'nat.db' not found.", e);
            } catch (IOException e) {
                throw new AssertionError("Unable to copy 'nat.db' to internal storage", e);
            }
        }
    }

    public long addNatEntry(NatItem nat) {
        if (nat.restaurantName == null) {
            throw new IllegalArgumentException("restaurantname must be set");
        }

        ContentValues values = new ContentValues();
        values.put("restaurant", nat.restaurantName);
        values.put("meal", nat.meal);
        values.put("notice", nat.notice);
        values.put("knusper", nat.knusper);
        values.put("size", nat.size);
        values.put("beilagen", nat.beilagen);
        values.put("geschmack", nat.geschmack);
        values.put("preis", nat.preis);
        values.put("imagePath", nat.imagePath);

        return db.insert(NAT_TABLE, null, values);
    }


    public ArrayList<NatItem> getNatEntries() {
        ArrayList<NatItem> natItems = new ArrayList<>();
        String[] columns = {"restaurant", "meal", "notice", "knusper", "size", "beilagen", "geschmack", "preis", "imagePath"};

        Cursor c = db.query(NAT_TABLE, columns, null, null, null, null, null);
        if (!c.moveToFirst()) {
            natItems.add(new NatItem("none", "none", "none", 0f, 0f, 0f, 0f, 0f, null));
            return natItems;
        }
        if (c.moveToFirst()) {
            do {
                String restaurant = c.getString(0);
                String meal = c.getString(1);
                String notice = c.getString(2);
                Float knusper = c.getFloat(3);
                Float size = c.getFloat(4);
                Float beilagen = c.getFloat(5);
                Float geschmack = c.getFloat(6);
                Float preis = c.getFloat(7);
                String imagePath = c.getString(8);
                NatItem nat = new NatItem(restaurant, meal, notice, knusper, size, beilagen, geschmack, preis, imagePath);
                natItems.add(nat);
            } while (c.moveToNext());
        }
        c.close();

        return natItems;
    }

    public void editEntry(NatItem editNat, ContentValues contentValues) {
        String[] whereArgs = {editNat.meal, editNat.restaurantName};
        db.update(NAT_TABLE, contentValues, "meal = ? AND restaurant = ?", whereArgs);
    }

    public boolean removeNatEntry(NatItem kickNat) {
        String[] whereArgs = {kickNat.meal, kickNat.restaurantName};
        String[] columns = {"meal", "restaurant"};
        Cursor c = db.query(NAT_TABLE, columns, "meal = ? AND restaurant = ?", whereArgs, null, null, null);
        if (c.moveToNext()) {
            db.delete(NAT_TABLE, "meal = ? AND restaurant = ?", whereArgs);
            return true;
        }
        c.close();
        return false;
    }

    public NatItem getNatItemByPK(String restaurante, String meale) {
        NatItem natItem = null;
        String[] columns = {"restaurant", "meal", "notice", "knusper", "size", "beilagen", "geschmack", "preis", "imagePath"};
        String[] whereArgs = {meale, restaurante};

        Cursor c = db.query(NAT_TABLE, columns, "meal = ? AND restaurant = ?", whereArgs, null, null, null);
        if (c.moveToFirst()) {
            String restaurant = c.getString(0);
            String meal = c.getString(1);
            String notice = c.getString(2);
            Float knusper = c.getFloat(3);
            Float size = c.getFloat(4);
            Float beilagen = c.getFloat(5);
            Float geschmack = c.getFloat(6);
            Float preis = c.getFloat(7);
            String imagePath = c.getString(8);
            natItem = new NatItem(restaurant, meal, notice, knusper, size, beilagen, geschmack, preis, imagePath);
        } else natItem = new NatItem("none", "none", "none", 0f, 0f, 0f, 0f, 0f, null);
        c.close();
        return natItem;
    }

    public JSONObject exportDatabase() throws JSONException {
        JSONObject outJson = new JSONObject();


        JSONArray jsonNatEntries = new JSONArray();
        ArrayList<NatItem> natEntries = getNatEntries();

        if (natEntries != null) {
            for (NatItem nat : natEntries) {
                if (nat.restaurantName.equals("none") && nat.meal.equals("none")) return null;

                JSONObject natEntry = new JSONObject();
                if (nat == null) {
                    continue;
                }
                natEntry.put("nat", nat.toJsonObject());
                jsonNatEntries.put(natEntry);
            }
        }
        outJson.put("natEntries", jsonNatEntries);

        return outJson;
    }

    public void importDatabase(JSONObject inJson, boolean replaceExisting) throws JSONException {
        JSONArray natEntries = null;

        try {
            natEntries = inJson.getJSONArray("natEntries");
        } catch (JSONException e) {
            Log.wtf("INFO", "No NatEntries to import");
        }

        if (natEntries != null) {
            for (int i = 0; i < natEntries.length(); i++) {

                JSONObject jsonNatItem = natEntries.getJSONObject(i).getJSONObject("nat");

                ContentValues values = new ContentValues();
                values.put("restaurant", jsonNatItem.getString("restaurant"));
                values.put("meal", jsonNatItem.getString("meal"));
                values.put("notice", jsonNatItem.getString("notice"));
                values.put("knusper", Float.parseFloat(jsonNatItem.getString("knusper")));
                values.put("size", Float.parseFloat(jsonNatItem.getString("size")));
                values.put("beilagen", Float.parseFloat(jsonNatItem.getString("beilagen")));
                values.put("geschmack", Float.parseFloat(jsonNatItem.getString("taste")));
                values.put("preis", Float.parseFloat(jsonNatItem.getString("preis")));
                //values.put("imagePath", null);

                if (replaceExisting) {
                    db.replace(NAT_TABLE, null, values);
                } else db.insert(NAT_TABLE, null, values);

            }
        }
    }
}

