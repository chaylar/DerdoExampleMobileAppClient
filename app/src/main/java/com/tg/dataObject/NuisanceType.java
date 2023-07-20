package com.tg.dataObject;

public class NuisanceType {

    public int typeCode;

    public String nuisanceName;

    @Override
    public String toString(){
        return this.nuisanceName;
    }

    public NuisanceType(int typeCode, String nuisanceName) {
        this.nuisanceName = nuisanceName;
        this.typeCode = typeCode;
    }

    public NuisanceType() {

    }

}
