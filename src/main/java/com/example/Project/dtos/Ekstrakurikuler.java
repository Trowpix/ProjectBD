package com.example.Project.dtos;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;


public class Ekstrakurikuler {
    private final StringProperty id;
    private final StringProperty nama;
    private final StringProperty jadwal;
    private final StringProperty pembina;
    private final BooleanProperty terdaftar;

    public Ekstrakurikuler(String id, String nama, String jadwal, String pembina) {
        this.id = new SimpleStringProperty(id);
        this.nama = new SimpleStringProperty(nama);
        this.jadwal = new SimpleStringProperty(jadwal);
        this.pembina = new SimpleStringProperty(pembina);
        this.terdaftar = new SimpleBooleanProperty(false);
    }

    public String getId() { return id.get(); }
    public StringProperty idProperty() { return id; }

    public String getNama() { return nama.get(); }
    public StringProperty namaProperty() { return nama; }

    public String getJadwal() { return jadwal.get(); }
    public StringProperty jadwalProperty() { return jadwal; }

    public String getPembina() { return pembina.get(); }
    public StringProperty pembinaProperty() { return pembina; }

    public boolean isTerdaftar() { return terdaftar.get(); }
    public BooleanProperty terdaftarProperty() { return terdaftar; }
    public void setTerdaftar(boolean isTerdaftar) { this.terdaftar.set(isTerdaftar); }
}
