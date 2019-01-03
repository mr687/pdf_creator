package com.daphino.pdfcreator2;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.itextpdf.text.Font;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DetailActivity extends AppCompatActivity implements View.OnClickListener {
    TextView txtName,txtJabatan,txtNIP,txtBidang,txtTanggal,txtAlasan,txtTglDariSampai,txtAlamat,txtNoTelepon,txtPenyetuju;
    SimpleDateFormat currentDate;
    Date dateNow;
    Button btnCetak;
    DBHelper helper;

    float interval = 80f;

    private static Font titleFont = new Font(Font.FontFamily.TIMES_ROMAN,12,Font.BOLD);
    private static Font subTitleFont = new Font(Font.FontFamily.TIMES_ROMAN,12);
    private static Font subTitleFontBold = new Font(Font.FontFamily.TIMES_ROMAN,12, Font.BOLD);
    private static Font contentFont = new Font(Font.FontFamily.TIMES_ROMAN,12);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        txtName = (TextView) findViewById(R.id.textView1);
        txtJabatan = (TextView) findViewById(R.id.textView2);
        txtNIP = (TextView) findViewById(R.id.textView3);
        txtBidang = (TextView) findViewById(R.id.textView4);
        txtTanggal = (TextView) findViewById(R.id.textView5);
        txtAlasan = (TextView) findViewById(R.id.textView6);
        txtTglDariSampai = (TextView) findViewById(R.id.textView7);
        txtAlamat = (TextView) findViewById(R.id.textView8);
        txtNoTelepon = (TextView) findViewById(R.id.textView9);
        txtPenyetuju = (TextView) findViewById(R.id.textView10);

        btnCetak = (Button) findViewById(R.id.btnCetak);

        currentDate = new SimpleDateFormat("dd-MM-yyyy");
        dateNow = new Date();

        helper = new DBHelper(this);
        SQLiteDatabase db = helper.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM Surat WHERE nosurat = '" +
                getIntent().getStringExtra("nosurat") + "'",null);
        if(cursor.getCount() > 0){
            cursor.moveToFirst();
            txtName.setText(cursor.getString(0).toString());
            txtJabatan.setText(cursor.getString(1).toString());
            txtNIP.setText(cursor.getString(2).toString());
            txtBidang.setText(cursor.getString(3).toString());
            txtTanggal.setText(cursor.getString(4).toString());
            txtAlasan.setText(cursor.getString(5).toString());
            txtTglDariSampai.setText(cursor.getString(6).toString());
            txtAlamat.setText(cursor.getString(7).toString());
            txtNoTelepon.setText(cursor.getString(8).toString());
            txtPenyetuju.setText(cursor.getString(9).toString());
            txtJabatan.setText(cursor.getString(10).toString());
            //txtTempatTujuan.setText(cursor.getString(11).toString());
        }

        btnCetak.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {

    }
}
