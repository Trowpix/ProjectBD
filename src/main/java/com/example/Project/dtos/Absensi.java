package com.example.Project.dtos;

import java.sql.Date;

public class Absensi {
    private Date tanggal;
    private String mataPelajaran;
    private String status;

    public Absensi(Date tanggal, String mataPelajaran, String status) {
        this.tanggal = tanggal;
        this.mataPelajaran = mataPelajaran;
        this.status = status;
    }

    public Date getTanggal() {
        return tanggal;
    }

    public void setTanggal(Date tanggal) {
        this.tanggal = tanggal;
    }

    public String getMataPelajaran() {
        return mataPelajaran;
    }

    public void setMataPelajaran(String mataPelajaran) {
        this.mataPelajaran = mataPelajaran;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}