package com.tg.dataManager;

import com.tg.dataObject.GenderDataObject;

import java.util.ArrayList;

public class GenderDataManager {

    public static ArrayList<GenderDataObject> GetGendersList() {
        //TODO : GET THESE FROM API
        String[] values = new String[] { "Erkek", "Kadın" };

        final ArrayList<GenderDataObject> list = new ArrayList<GenderDataObject>();
        for (int i = 0; i < values.length; ++i) {

            GenderDataObject ndo = new GenderDataObject();
            ndo.name = values[i];
            ndo.id = (i + 1);

            list.add(ndo);
        }

        return list;
    }

    public static void SelectGenderById(int id) {
        //TODO :
    }

}
