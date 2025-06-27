package com.example.Project.dtos;

import javafx.beans.property.SimpleStringProperty;

public class TableTampilanEkstrakulikurel {
    private final SimpleStringProperty NamaEskul;
    private final SimpleStringProperty TanggalEskul;
    private final SimpleStringProperty PembinaEskul;

    public TableTampilanEkstrakulikurel(SimpleStringProperty namaEskul, SimpleStringProperty tanggalEskul, SimpleStringProperty pembinaEskul) {
        NamaEskul = namaEskul;
        TanggalEskul = tanggalEskul;
        PembinaEskul = pembinaEskul;
    }

    public String getNamaEskul() {
        return NamaEskul.get();
    }

    public SimpleStringProperty namaEskulProperty() {
        return NamaEskul;
    }

    public String getTanggalEskul() {
        return TanggalEskul.get();
    }

    public SimpleStringProperty tanggalEskulProperty() {
        return TanggalEskul;
    }

    public String getPembinaEskul() {
        return PembinaEskul.get();
    }

    public SimpleStringProperty pembinaEskulProperty() {
        return PembinaEskul;
    }
}
