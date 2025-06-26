package com.example.bdsqltester.dtos;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;

public class TableViewGrade {
    private final SimpleStringProperty mapelName;
    private final SimpleDoubleProperty utsValue;
    private final SimpleDoubleProperty uasValue;
    private final SimpleDoubleProperty rataRataValue;

    public TableViewGrade(String mapelName, double utsValue, double uasValue, double rataRataValue){
        this.mapelName = new SimpleStringProperty(mapelName);
        this.utsValue = new SimpleDoubleProperty(utsValue);
        this.uasValue = new SimpleDoubleProperty(uasValue);
        this.rataRataValue = new SimpleDoubleProperty(rataRataValue);
    }

    public String getMapelName() {
        return mapelName.get();
    }
    public double getUtsValue() {
        return utsValue.get();
    }
    public double getUasValue() {
        return uasValue.get();
    }
    public double getRataRataValue() {
        return rataRataValue.get();
    }

    public SimpleDoubleProperty rataRataValueProperty() {
        return rataRataValue;
    }
    public SimpleDoubleProperty uasValueProperty() {
        return uasValue;
    }
    public SimpleStringProperty mapelNameProperty() {
        return mapelName;
    }
    public SimpleDoubleProperty utsValueProperty() {
        return utsValue;
    }
}