package com.daphino.pdfcreator2;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.Toast;

import com.borax12.materialdaterangepicker.date.DatePickerDialog;
import com.kyanogen.signatureview.SignatureView;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class AddActivity extends AppCompatActivity
        implements View.OnClickListener,com.borax12.materialdaterangepicker.date.DatePickerDialog.OnDateSetListener{
    EditText txtName,txtJabatan,txtNIP,txtBidang,txtTanggal,txtAlasan,txtTglDariSampai,txtAlamat,txtNoTelepon,txtPenyetuju,txtTanggalHakCuti,txtJabatanPenyetuju;
    RadioButton radioCuti,radioIzin;
    Spinner spinnerTipeCuti,spinnerLamaCuti,spinnerAlasanPenting,spinnerLamaCuti2,spinnerSisaHakCuti,spinnerHakYangDiambil;
    Button btnSimpan,btnPenyetuju,btnPemohon;
    LinearLayout groupCuti,groupIzin;
    DBHelper helper;
    boolean isCuti = false;
    boolean isHak = false;
    public String pathSign1;
    public String pathSign2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        pathSign1 ="";
        pathSign2 ="";

        txtName = (EditText) findViewById(R.id.txtName);
        txtJabatan = (EditText) findViewById(R.id.txtJabatan);
        txtNIP = (EditText) findViewById(R.id.txtNIP);
        txtBidang = (EditText) findViewById(R.id.txtBidang);
        txtTanggal = (EditText) findViewById(R.id.txtTanggal);
        txtAlasan = (EditText) findViewById(R.id.txtAlasan);
        txtTglDariSampai = (EditText) findViewById(R.id.txtTanggalDaridanSampai);
        txtAlamat = (EditText) findViewById(R.id.txtAlamat);
        txtNoTelepon = (EditText) findViewById(R.id.txtNoTelepon);
        txtPenyetuju = (EditText) findViewById(R.id.txtPenyetuju);
        txtTanggalHakCuti = (EditText) findViewById(R.id.txtTanggalHakCuti);
        txtJabatanPenyetuju = (EditText) findViewById(R.id.txtJabatanPenyetuju);

        radioCuti = (RadioButton) findViewById(R.id.radioCuti);
        radioIzin = (RadioButton) findViewById(R.id.radioIzin);

        spinnerSisaHakCuti = (Spinner) findViewById(R.id.spinnerSisaHakCuti);
        spinnerAlasanPenting = (Spinner) findViewById(R.id.spinnerAlasanPenting);
        spinnerLamaCuti2 = (Spinner) findViewById(R.id.spinnerLamaCuti2);
        spinnerLamaCuti = (Spinner) findViewById(R.id.spinnerLamaCuti);
        spinnerTipeCuti = (Spinner) findViewById(R.id.spinnerJenisCuti);
        spinnerHakYangDiambil = (Spinner) findViewById(R.id.spinnerHakYangDiambil);

        btnSimpan = (Button) findViewById(R.id.btnSimpan);
        btnPenyetuju = (Button) findViewById(R.id.btnPenyetuju);
        btnPemohon = (Button) findViewById(R.id.btnPemohon);

        groupCuti = (LinearLayout) findViewById(R.id.groupCuti);
        groupIzin = (LinearLayout) findViewById(R.id.groupIzin);

        helper = new DBHelper(getApplicationContext());

        btnSimpan.setOnClickListener(this);
        txtTanggal.setOnClickListener(this);
        txtTglDariSampai.setOnClickListener(this);
        txtTanggalHakCuti.setOnClickListener(this);
        radioCuti.setOnClickListener(this);
        radioIzin.setOnClickListener(this);
        btnPemohon.setOnClickListener(this);
        btnPenyetuju.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if(view == btnSimpan){
            String lamaCuti = "0";
            String tanggal= "";
            String alasanPenting = "";
            String tipeAlasanPenting = "";
            String izinAtauCuti = "";
            if(radioIzin.isChecked()){
                lamaCuti = spinnerLamaCuti2.getSelectedItem().toString().replace(" Hari","");
                tanggal = txtTglDariSampai.getText().toString();
                alasanPenting = txtAlasan.getText().toString();
                izinAtauCuti = "Izin";
                tipeAlasanPenting = spinnerAlasanPenting.getSelectedItem().toString();
            }else if(radioCuti.isChecked()){
                lamaCuti = spinnerLamaCuti.getSelectedItem().toString().replace(" Hari","");
                tanggal = txtTanggal.getText().toString();
                alasanPenting = "";
                tipeAlasanPenting = "";
                izinAtauCuti = "Cuti";
            }else{
                Toast.makeText(getApplicationContext(),"Silahkan pilih Cuti / Izin.",Toast.LENGTH_LONG).show();
                return;
            }
            if(helper.insertData(
                    txtName.getText().toString(),
                    txtJabatan.getText().toString(),
                    txtNIP.getText().toString(),
                    txtBidang.getText().toString(),
                    spinnerTipeCuti.getSelectedItem().toString(),
                    lamaCuti,
                    tanggal,
                    alasanPenting,
                    tipeAlasanPenting,
                    txtAlamat.getText().toString(),
                    txtNoTelepon.getText().toString(),
                    izinAtauCuti,
                    txtPenyetuju.getText().toString(),
                    spinnerSisaHakCuti.getSelectedItem().toString().replace(" Hari",""),
                    txtTanggalHakCuti.getText().toString(),
                    spinnerHakYangDiambil.getSelectedItem().toString().replace(" Hari",""),
                    txtJabatanPenyetuju.getText().toString(),
                    pathSign2,
                    pathSign1)){
                Toast.makeText(getApplicationContext(),"Data berhasil di simpan.",Toast.LENGTH_LONG).show();
                Intent it = new Intent(getApplicationContext(),MainActivity.class);
                it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(it);
            }else{
                Toast.makeText(getApplicationContext(),"Data gagal di simpan. Data dengan nama " + txtName.getText().toString() + " sudah ada. Silahkan hapus data dengan nama yang sama.",Toast.LENGTH_LONG).show();
            }
        }else if(view == txtTanggal){
            Calendar now = Calendar.getInstance();
            DatePickerDialog datePickerDialog = DatePickerDialog.newInstance(
                    AddActivity.this,
                    now.get(Calendar.YEAR),
                    now.get(Calendar.MONTH),
                    now.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.show(getFragmentManager(),"Datepickerdialog");
        }else if(view == txtTglDariSampai){
            Calendar now = Calendar.getInstance();
            DatePickerDialog datePickerDialog = DatePickerDialog.newInstance(
                    AddActivity.this,
                    now.get(Calendar.YEAR),
                    now.get(Calendar.MONTH),
                    now.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.show(getFragmentManager(),"Datepickerdialog");

        }else if(view == radioCuti){
            isCuti = true;
            radioIzin.setChecked(false);
            groupCuti.setVisibility(View.VISIBLE);
            groupIzin.setVisibility(View.GONE);
        }else if(view == radioIzin){
            isCuti = false;
            radioCuti.setChecked(false);
            groupIzin.setVisibility(View.VISIBLE);
            groupCuti.setVisibility(View.GONE);
        }else if(view==txtTanggalHakCuti){
            isHak = true;
            Calendar now = Calendar.getInstance();
            DatePickerDialog datePickerDialog = DatePickerDialog.newInstance(
                    AddActivity.this,
                    now.get(Calendar.YEAR),
                    now.get(Calendar.MONTH),
                    now.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.show(getFragmentManager(),"Datepickerdialog");
        }else if(view == btnPemohon){
            Intent it = new Intent(getBaseContext(),SignatureActivity.class);
            it.putExtra("type","pemohon");
            startActivityForResult(it,123);
        }else if(view == btnPenyetuju){
            Intent it = new Intent(getBaseContext(),SignatureActivity.class);
            it.putExtra("type","penyetuju");
            startActivityForResult(it,123);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == 123){
            if(resultCode == Activity.RESULT_OK){
                if(data.getStringExtra("type").toString().equals("pemohon")){
                    pathSign1 = data.getStringExtra("path");
                }else{
                    pathSign2 = data.getStringExtra("path");
                }
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        DatePickerDialog datePickerDialog = (DatePickerDialog) getFragmentManager().findFragmentByTag("Datepickerdialog");
        if(datePickerDialog != null) datePickerDialog.setOnDateSetListener(this);
    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth, int yearEnd, int monthOfYearEnd, int dayOfMonthEnd) {
        String eventDate = dayOfMonthEnd+"-"+(++monthOfYearEnd)+"-"+yearEnd;
        String event2Date = dayOfMonth+"-"+(++monthOfYear)+"-"+year;
        DateFormat date = new SimpleDateFormat("dd-M-yyyy");
        try {
            Date date1 = date.parse(eventDate);
            Date date2 = date.parse(event2Date);

            long difference = date1.getTime() - date2.getTime();
            long diff = TimeUnit.DAYS.convert(difference , TimeUnit.MILLISECONDS);

            date = new SimpleDateFormat("dd MMMM yyyy");
            String datee =  "";
            if(isHak){
                datee = date.format(date2);
                txtTanggalHakCuti.setText(datee);
                isHak = false;
            }else{
                datee = date.format(date2)+" - "+date.format(date1);
                if(date.format(date2).equals(date.format(date1))){
                    datee = date.format(date2);
                }
                if(isCuti){
                    txtTanggal.setText(datee);
                }else{
                    txtTglDariSampai.setText(datee);
                }
            }
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
