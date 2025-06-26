package com.example.bdsqltester.dtos;

import javafx.beans.property.SimpleStringProperty;

public class TableTampilanSiswa {
    private final SimpleStringProperty nama;
    private final SimpleStringProperty nis;
    private final SimpleStringProperty kelamin;

    public TableTampilanSiswa(String nama, String nis, String kelamin) {
        this.nama = new SimpleStringProperty(nama);
        this.nis = new SimpleStringProperty(nis);
        this.kelamin = new SimpleStringProperty(kelamin);
    }

    public String getNama() {
        return nama.get();
    }

    public SimpleStringProperty namaProperty() {
        return nama;
    }

    public String getNis() {
        return nis.get();
    }

    public SimpleStringProperty nisProperty() {
        return nis;
    }

    public String getKelamin() {
        return kelamin.get();
    }

    public SimpleStringProperty kelaminProperty() {
        return kelamin;
    }
}
