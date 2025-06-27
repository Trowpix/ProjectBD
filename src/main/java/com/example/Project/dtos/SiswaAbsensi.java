package com.example.Project.dtos;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.CheckBox;

public class SiswaAbsensi {
    private final StringProperty nis;
    private final StringProperty nama;
    private final CheckBox hadir;
    private final CheckBox izin;
    private final CheckBox alpha;

    public SiswaAbsensi(String nis, String nama) {
        this.nis = new SimpleStringProperty(nis);
        this.nama = new SimpleStringProperty(nama);
        this.hadir = new CheckBox();
        this.izin = new CheckBox();
        this.alpha = new CheckBox();

        // Logika agar hanya satu CheckBox yang bisa dipilih
        hadir.setOnAction(e -> {
            if (hadir.isSelected()) {
                izin.setSelected(false);
                alpha.setSelected(false);
            }
        });
        izin.setOnAction(e -> {
            if (izin.isSelected()) {
                hadir.setSelected(false);
                alpha.setSelected(false);
            }
        });
        alpha.setOnAction(e -> {
            if (alpha.isSelected()) {
                hadir.setSelected(false);
                izin.setSelected(false);
            }
        });
    }

    public String getNis() { return nis.get(); }
    public StringProperty nisProperty() { return nis; }
    public String getNama() { return nama.get(); }
    public StringProperty namaProperty() { return nama; }
    public CheckBox getHadir() { return hadir; }
    public CheckBox getIzin() { return izin; }
    public CheckBox getAlpha() { return alpha; }

    public String getStatus() {
        if (hadir.isSelected()) return "Hadir";
        if (izin.isSelected()) return "Izin";
        if (alpha.isSelected()) return "Alpha";
        return null; // Atau "Belum Diabsen"
    }
}