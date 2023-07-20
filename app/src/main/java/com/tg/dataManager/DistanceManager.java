package com.tg.dataManager;

public class DistanceManager {

    public static String GetDistanceForField(Double mDistance) {

        if(mDistance == null) {
            return null;
        }

        String distanceUnit = "km";
        //String distanceUnit = "m";
        Double resultDistance = 0.0;
        if(mDistance >= 1000) {
            resultDistance = mDistance / 1000.0;
            distanceUnit = "km";
        }
        else {
            resultDistance = 1d;
            //resultDistance = mDistance;
        }

        return ((int)Math.round(resultDistance) + " " + distanceUnit);

        //return (String.format("%.1f", resultDistance) + " " + distanceUnit);
    }

}
