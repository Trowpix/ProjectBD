package com.example.Project.dtos;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class GuruData {
    private final StringProperty nip;
    private final StringProperty nama;

    public GuruData(String nip, String nama) {
        this.nip = new SimpleStringProperty(nip);
        this.nama = new SimpleStringProperty(nama);
    }

    public String getNip() { return nip.get(); }
    public StringProperty nipProperty() { return nip; }
    public String getNama() { return nama.get(); }
    public StringProperty namaProperty() { return nama; }
}