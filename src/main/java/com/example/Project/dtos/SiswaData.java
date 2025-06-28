package com.example.Project.dtos;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class SiswaData {
    private final StringProperty nis;
    private final StringProperty nama;

    public SiswaData(String nis, String nama) {
        this.nis = new SimpleStringProperty(nis);
        this.nama = new SimpleStringProperty(nama);
    }

    public String getNis() { return nis.get(); }
    public StringProperty nisProperty() { return nis; }
    public String getNama() { return nama.get(); }
    public StringProperty namaProperty() { return nama; }
}