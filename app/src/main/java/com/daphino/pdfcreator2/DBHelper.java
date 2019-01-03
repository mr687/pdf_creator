package com.daphino.pdfcreator2;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
    SQLiteDatabase db;
    public DBHelper(Context context) {
        super(context, "db_izin", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS Izin(Name text primary key,Jabatan text null,NIP text null,Bidang text null," +
                "TipeCuti text null,LamaCuti text null,Tanggal text null," +
                "AlasanPenting text null,TipeAlasanPenting text null,Alamat text null, NoTelepon text null,IzinAtauCuti text null," +
                "Penyetuju text null,SisaCuti text null,TanggalHakCuti text null,HakCutiYangDiambil text null,JabatanPenyetuju text null," +
                "TTDPenyetuju text null, TTDPemohon text null);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS Izin");
        onCreate(db);
    }

    public boolean insertData(String Name, String Jabatan, String NIP, String Bidang, String TipeCuti,
                              String LamaCuti, String Tanggal,String AlasanPenting,String tipeAlasanPenting,
                              String Alamat, String NoTelepon, String IzinAtauCuti, String Penyetuju, String SisaCuti, String TanggalHakCuti,
                              String HakCutiYangDiambil, String JabatanPenyetuju, String TTDPenyetuju, String TTDPemohon){
        db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("Name",Name);
        contentValues.put("Jabatan",Jabatan);
        contentValues.put("NIP",NIP);
        contentValues.put("Bidang",Bidang);
        contentValues.put("TipeCuti",TipeCuti);
        contentValues.put("LamaCuti",LamaCuti);
        contentValues.put("Tanggal",Tanggal);
        contentValues.put("AlasanPenting",AlasanPenting);
        contentValues.put("TipeAlasanPenting",tipeAlasanPenting);
        contentValues.put("Alamat",Alamat);
        contentValues.put("NoTelepon",NoTelepon);
        contentValues.put("IzinatauCuti",IzinAtauCuti);
        contentValues.put("Penyetuju",Penyetuju);
        contentValues.put("SisaCuti",SisaCuti);
        contentValues.put("TanggalHakCuti",TanggalHakCuti);
        contentValues.put("HakCutiYangDiambil",HakCutiYangDiambil);
        contentValues.put("JabatanPenyetuju",JabatanPenyetuju);
        contentValues.put("TTDPenyetuju",TTDPenyetuju);
        contentValues.put("TTDPemohon",TTDPemohon);
        try {
            db.insertOrThrow("Izin", null, contentValues);
        }catch (SQLiteConstraintException ex){
            ex.printStackTrace();
            return false;
        }catch (Exception ex){
            ex.printStackTrace();
            return  false;
        }
        return true;
    }

    public Cursor getAllData(){
        db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM Izin",null);
        return cursor;
    }
}
