package com.example.natnat;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Comparator;

public class NatItem implements Comparable<NatItem> {

    final String restaurantName;
    final String notice;
    final String meal;
    final float knusper;
    final float size;
    final float beilagen;
    final float geschmack;
    final float preis;
    final float gesamt;
    final String imagePath;

    public NatItem(String restaurantName, String meal, String notice, float knusper, float size, float beilagen, float geschmack, float preis, String imagePath) {
        this.restaurantName = restaurantName;
        this.meal = meal;
        this.notice = notice;
        this.knusper = knusper;
        this.size = size;
        this.beilagen = beilagen;
        this.geschmack = geschmack;
        this.preis = preis;
        this.imagePath = imagePath;
        this.gesamt = (knusper + size + beilagen + geschmack + preis) / 5;
    }

    public JSONObject toJsonObject() throws JSONException{
        JSONObject ret = new JSONObject();
        ret.put("restaurant", restaurantName);
        ret.put("meal", meal);
        ret.put("notice", notice);
        ret.put("knusper", knusper);
        ret.put("size", size);
        ret.put("beilagen", beilagen);
        ret.put("taste", geschmack);
        ret.put("preis", preis);
        ret.put("gesamt", gesamt);
        //todo image
        return ret;
    }

    @Override
    public int compareTo(NatItem o) {
        if (this.gesamt == o.gesamt)
            return 0;
        else if (this.gesamt > o.gesamt)
            return -1;
        else
            return 1;
    }
}

