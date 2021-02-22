package com.lepu.anxin.room;

import android.content.Context;
import android.content.res.AssetManager;

import com.blankj.utilcode.util.LogUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Addr {

    public static List<String> proviences = new ArrayList<>();
    public static List<List<String>> citys = new ArrayList<>();
    public static List<List<List<String>>> diss = new ArrayList<>();

    public static void initAddrs(Context context) {
        AssetManager am =  context.getAssets();
        String list;
        StringBuilder sb = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(am.open("addr.json")));
            String line;
            while ((line = br.readLine()) != null ) {
                sb.append(line);
            }
            list = sb.toString();

            Gson gson = new Gson();
            Provience[] ls = gson.fromJson(list, Provience[].class);
            // pro
            for (Provience p : ls) {
                proviences.add(p.name);
                List<String> tmpCity = new ArrayList<>();
                List<List<String>> tmpDis = new ArrayList<>();
                for (City c: p.children) {
                    tmpCity.add(c.name);

                    List<String> dis = new ArrayList<>();
                    for (Dis d: c.children) {
                        dis.add(d.name);
                    }
                    tmpDis.add(dis);
                }
                citys.add(tmpCity);
                diss.add(tmpDis);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static class Address {
        List<Provience> addr;
    }

    static class Provience {
        String name;
        String code;
        List<City> children;


    }

    static class City {
        String name;
        String code;
        List<Dis> children;

        @Override
        public String toString() {
            return name + "\n" + children.size();
        }
    }
    static class Dis {
        String name;
        String code;
    }
}
