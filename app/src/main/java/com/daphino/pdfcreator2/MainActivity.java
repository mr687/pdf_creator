package com.daphino.pdfcreator2;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Environment;
import android.provider.ContactsContract;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.itextpdf.text.BadElementException;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.TabSettings;
import com.itextpdf.text.pdf.BaseField;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfAnnotation;
import com.itextpdf.text.pdf.PdfAppearance;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfFormField;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.RadioCheckField;
import com.itextpdf.text.pdf.draw.LineSeparator;
import com.itextpdf.text.pdf.draw.VerticalPositionMark;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity
        implements View.OnClickListener {
    String[] list;
    ListView listView;
    Menu menu;
    Cursor cursor;
    DBHelper helper;
    Button btnAdd;
    TextView txtUncheck, txtCheck;

    SimpleDateFormat currentDate;
    Date dateNow;
    Button btnCetak;

    float interval = 80f;
    char checked='\u00FE';
    char unchecked='\u00A8';

    private static Font titleFont = new Font(Font.FontFamily.TIMES_ROMAN,14,Font.BOLD);
    private static Font contentFont = new Font(Font.FontFamily.TIMES_ROMAN,12);
    private static Font contentFontBold = new Font(Font.FontFamily.TIMES_ROMAN,12, Font.BOLD);
    private static Font contentFontStrikeThru= new Font(Font.FontFamily.HELVETICA, 12f, Font.STRIKETHRU);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnAdd = (Button) findViewById(R.id.button2);
        listView = (ListView) findViewById(R.id.listView1);

        helper = new DBHelper(this);

        currentDate = new SimpleDateFormat("dd-MM-yyyy");
        dateNow = new Date();

        btnAdd.setOnClickListener(this);
        refreshList();
    }
    public void refreshList(){
        try{
            Cursor cursor = helper.getAllData();
            if(cursor != null){
                list = new String[cursor.getCount()];
                cursor.moveToFirst();
                if(cursor.getCount()>0){
                    for (int i = 0; i < cursor.getCount();i++){
                        cursor.moveToPosition(i);
                        list[i] = cursor.getString(0).toString();
                    }
                }

                if(list != null){
                    listView.setAdapter(new ArrayAdapter(this, android.R.layout.simple_list_item_1, list));
                    listView.setSelected(true);
                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                            final  String selection = list[i];
                            final CharSequence[] dialogitem = {"Open PDF", "Hapus Surat"};
                            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                            builder.setTitle("Pilihan");
                            builder.setItems(dialogitem, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int item) {
                                    switch(item){
                                        case 0 :
                                            SQLiteDatabase db = helper.getWritableDatabase();
                                            Cursor cursor = db.rawQuery("SELECT * FROM Izin WHERE Name= '" +
                                                    selection + "'",null);
                                            cetakPDF(cursor);

                                            File file = new File(Environment.getExternalStorageDirectory() + File.separator + "PDF Creator 2" + File.separator + currentDate.format(dateNow) + ".pdf");
                                            Intent intent = new Intent(Intent.ACTION_VIEW);
                                            Uri uri = FileProvider.getUriForFile(MainActivity.this, BuildConfig.APPLICATION_ID + ".provider",file);
                                            intent.setDataAndType(uri,"application/pdf");
                                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                            startActivity(Intent.createChooser(intent,"Open With"));
                                            break;
                                        case 1 :
                                            db = helper.getWritableDatabase();
                                            db.execSQL("DELETE FROM Izin WHERE Name = '"+selection+"'");
                                            refreshList();
                                            Toast.makeText(MainActivity.this,"Data berhasil di hapus.",Toast.LENGTH_LONG).show();
                                            break;
                                    }
                                }
                            });
                            builder.create().show();
                        }
                    });
                    ((ArrayAdapter)listView.getAdapter()).notifyDataSetChanged();
                }
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }
    @Override
    public void onClick(View view) {
        if(view == btnAdd){
            Intent it = new Intent(MainActivity.this,AddActivity.class);
            startActivity(it);
        }
    }

    private void cetakPDF(Cursor cursor){
        if(cursor.getCount() < 1){
            return;
        }
        cursor.moveToFirst();
        Document document=  new Document(PageSize.A4,38,38,50,38);
        File file = new File(Environment.getExternalStorageDirectory() + File.separator + "PDF Creator 2");
        boolean success =true;
        if(!file.exists()){
            success = file.mkdirs();
        }
        if(success == true){
            try{
                //generate fileName berdasarkan tanggal sekarang
                String fileName = currentDate.format(dateNow);
                fileName = fileName + ".pdf";

                PdfWriter writer = PdfWriter.getInstance(document,new FileOutputStream(file.getPath()+"/"+fileName));
                document.open();
                addMeta(document,"SURAT PERMOHONAN PELAKSANAAN CUTI/IZIN");
                addHeader(document,"SURAT PERMOHONAN PELAKSANAAN CUTI/IZIN");
                addBody(document,cursor,writer);
                addFooter(document,cursor);
                document.close();
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }else{
            Toast.makeText(getApplicationContext(),"Create PDF Creator folder is failed. Please allow this application to write your storage.",Toast.LENGTH_LONG).show();
        }
    }

    private void addMeta(Document document,String title){
        document.addTitle(title.toUpperCase());
        document.addSubject(title.toUpperCase());
        document.addSubject(title.toUpperCase());
        document.addAuthor("mr687");
        document.addCreator("mr687");
    }
    private  void addHeader(Document document, String perihal) throws DocumentException {
        Paragraph paragraph = new Paragraph();
        AssetManager assetManager = getAssets();
        InputStream is = null;
        try {
            is = assetManager.open("images/img.jpg");
        } catch (IOException e) {
            e.printStackTrace();
        }
        Bitmap  bmp = BitmapFactory.decodeStream(is);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG,100,stream);
        Image image = null;
        try {
            image = Image.getInstance(stream.toByteArray());
            image.scaleToFit(60,60);
        } catch (IOException e) {
            e.printStackTrace();
        }
        image.setAlignment(Image.MIDDLE);
        document.add(image);
        document.add(new Paragraph(" "));
        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(100);
        table.getDefaultCell().setBorder(0);
        table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
        table.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(new Paragraph("SURAT PERMOHONAN",titleFont));
        table.addCell(new Paragraph("PELAKSANAAN CUTI/IZIN",titleFont));
        document.add(table);
        enterLine(paragraph,2);
        document.add(paragraph);
    }
    private void addBody(Document document,Cursor cursor,PdfWriter writer) throws DocumentException, IOException {
        Paragraph paragraph = new Paragraph();

        PdfPTable table = new PdfPTable(2);
        table.getDefaultCell().setBorder(0);
        table.setWidthPercentage(90);
        table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
        table.setHorizontalAlignment(Element.ALIGN_LEFT);
        table.getDefaultCell().setBorder(0);
        table.addCell(paragraphWithTab("Nama",cursor.getString(0).toString(),80f,contentFont));
        table.addCell(paragraphWithTab("NIP",cursor.getString(2).toString(),50f,contentFont));
        table.addCell(paragraphWithTab("Grade/Jabatan",cursor.getString(1).toString(),80f,contentFont));
        table.addCell(paragraphWithTab("Bidang",cursor.getString(3).toString(),50f,contentFont));
        document.add(table);

        enterLine(paragraph,1);
        document.add(paragraph);
        paragraph.clear();

        String tanggal = "";

        table = new PdfPTable(1);
        table.setWidthPercentage(95);
        table.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
        table.getDefaultCell().setBorder(0);
        Phrase phrase=new Phrase();
        phrase.setFont(contentFont);
        Phrase phr = new Phrase();
        phr.setFont(contentFont);
        Phrase phraseIzin = new Phrase();
        phraseIzin.setFont(contentFont);

        RadioCheckField bt = new RadioCheckField(writer, new Rectangle(50, 575, 60, 585),
                "check1", "Yes");
        bt.setCheckType(RadioCheckField.TYPE_CHECK);
        bt.setBorderWidth(BaseField.BORDER_WIDTH_THIN);
        bt.setBorderColor(BaseColor.BLACK);
        bt.setBackgroundColor(BaseColor.WHITE);
        if(cursor.getString(11).toString().trim().equals("Cuti")){
            bt.setChecked(true);
        }else{
            bt.setChecked(false);
        }
        PdfFormField ck = null;
        try {
            ck = bt.getCheckField();
        } catch (IOException e) {
            e.printStackTrace();
        }
        writer.addAnnotation(ck);

        RadioCheckField bt2 = null;
        if(cursor.getString(11).toString().trim().equals("Izin")){
            bt2= new RadioCheckField(writer, new Rectangle(50, 487, 60, 497),
                    "check1", "Yes");
        }else if(cursor.getString(11).toString().trim().equals("Cuti")){
            bt2 = new RadioCheckField(writer, new Rectangle(50, 476, 60, 486),
                    "check1", "Yes");
        }
        bt2.setCheckType(RadioCheckField.TYPE_CHECK);
        bt2.setBorderWidth(BaseField.BORDER_WIDTH_THIN);
        bt2.setBorderColor(BaseColor.BLACK);
        bt2.setBackgroundColor(BaseColor.WHITE);
        if(cursor.getString(11).toString().trim().equals("Izin")){
            bt2.setChecked(true);
        }else{
            bt2.setChecked(false);
        }
        ck = null;
        try {
            ck = bt2.getCheckField();
        } catch (IOException e) {
            e.printStackTrace();
        }
        writer.addAnnotation(ck);
        RadioCheckField bt3 = null;
        if(cursor.getString(11).toString().trim().equals("Izin")){
            bt3= new RadioCheckField(writer, new Rectangle(38, 196, 48, 206),
                    "check1", "Yes");
        }else{
            bt3= new RadioCheckField(writer, new Rectangle(38, 184, 48, 194),
                    "check1", "Yes");
        }
        bt3.setCheckType(RadioCheckField.TYPE_CHECK);
        bt3.setBorderWidth(BaseField.BORDER_WIDTH_THIN);
        bt3.setBorderColor(BaseColor.BLACK);
        bt3.setBackgroundColor(BaseColor.WHITE);
        bt3.setChecked(false);
        ck = null;
        try {
            ck = bt3.getCheckField();
        } catch (IOException e) {
            e.printStackTrace();
        }
//        writer.addAnnotation(ck);

        phrase.add("Mohon diizinkan melaksanakan Cuti ");
        if(cursor.getString(11).toString().trim().equals("Cuti")){
            tanggal = cursor.getString(6).toString();
            if(cursor.getString(4).toString().trim().equals("Tahunan")){
                phr.add("Hak Cuti (Tahunan");
                phr.add(new Chunk("/Besar",contentFontStrikeThru));
                phr.add("*) jatuh pada tanggal "+ cursor.getString(14).toString() + "\n");
                phr.add("Hak Cuti (Tahunan");
                phr.add(new Chunk("/Besar",contentFontStrikeThru));
                phr.add("*) yang telah diambil adalah "+ cursor.getString(15).toString() + " hari \n");
                phr.add("Sisa hak Cuti (Tahunan");
                phr.add(new Chunk("/Besar",contentFontStrikeThru));
                phr.add("*) adalah "+ cursor.getString(13).toString() + " hari **");

                phrase.add("(Tahunan");
                phrase.add(new Chunk("/Berat/Bersalin",contentFontStrikeThru));
                phrase.add("*)");
            }else if(cursor.getString(4).toString().trim().equals("Besar")) {
                phr.add("Hak Cuti (");
                phr.add(new Chunk("Tahunan/",contentFontStrikeThru));
                phr.add("Besar*) jatuh pada tanggal "+ tanggal + "\n");
                phr.add("Hak Cuti (");
                phr.add(new Chunk("Tahunan/",contentFontStrikeThru));
                phr.add("Besar*) yang telah diambil adalah "+ cursor.getString(5).toString() + " hari \n");
                phr.add("Sisa hak Cuti (");
                phr.add(new Chunk("Tahunan/",contentFontStrikeThru));
                phr.add("Besar");
                int sisa = 12 - Integer.parseInt(cursor.getString(5).toString());
                phr.add("*) adalah "+ sisa + " hari **");

                phrase.add("(");
                phrase.add(new Chunk("Tahunan/", contentFontStrikeThru));
                phrase.add("Berat");
                phrase.add(new Chunk("/Bersalin", contentFontStrikeThru));
                phrase.add("*)");
            }
            else if(cursor.getString(4).toString().trim().equals("Bersalin")) {
                phr.add("Hak Cuti (Tahunan/");
                phr.add("Besar*) jatuh pada tanggal "+ "\n");
                phr.add("Hak Cuti (Tahunan/");
                phr.add("Besar*) yang telah diambil adalah" + " hari \n");
                phr.add("Sisa hak Cuti (Tahunan/");
                phr.add("Besar*)");
                phr.add(" adalah "+ "hari **)");

                phrase.add("(");
                phrase.add(new Chunk("Tahunan/", contentFontStrikeThru));
                phrase.add(new Chunk("Berat/", contentFontStrikeThru));
                phrase.add("Bersalin");
                phrase.add("*)");
            }
            phraseIzin.add("Mohon izin karena Alasan Penting");
            phraseIzin.add("            ");
            phraseIzin.add("(Sakit/Haid/Gugur Kandungan/Di Luar Tanggungan Perusahaan/Perpanjangan Izin Di Luar Tanggungan Perusahaan*) ");
            phraseIzin.add("selama     hari,            ");
            phrase.add(" selama ");
            phrase.add(cursor.getString(5).toString());
            phrase.add(" hari, tanggal ");
            phrase.add(tanggal);
        }else if(cursor.getString(11).toString().trim().equals("Izin")){
            tanggal = "";
            phr.add("Hak Cuti (Tahunan/");
            phr.add("Besar*) jatuh pada tanggal "+ "\n");
            phr.add("Hak Cuti (Tahunan/");
            phr.add("Besar*) yang telah diambil adalah" + " hari \n");
            phr.add("Sisa hak Cuti (Tahunan/");
            phr.add("Besar*)");
            phr.add(" adalah "+ "hari **");
            phrase.add("(Tahunan/Berat/Bersalin*)");
            phrase.add("selama 0 hari, tanggal");
            phraseIzin.add("Mohon izin karena Alasan Penting ");
            phraseIzin.add(cursor.getString(7).toString());
            phraseIzin.add(" ");
            if(cursor.getString(8).toString().trim().equals("Sakit")){
                phraseIzin.add("(Sakit ");
                phraseIzin.add(new Chunk("/Haid/Gugur Kandungan/Di Luar Tanggungan Perusahaan/Perpanjangan Izin Di Luar Tanggungan Perusahaan",contentFontStrikeThru));
                phraseIzin.add("*) ");
            }else if(cursor.getString(8).toString().trim().equals("Haid")){
                phraseIzin.add("(");
                phraseIzin.add(new Chunk("Sakit/",contentFontStrikeThru));
                phraseIzin.add("Haid");
                phraseIzin.add(new Chunk("/Gugur Kandungan/Di Luar Tanggungan Perusahaan/Perpanjangan Izin Di Luar Tanggungan Perusahaan",contentFontStrikeThru));
                phraseIzin.add("*) ");
            }else if(cursor.getString(8).toString().trim().equals("Gugur Kandungan")){
                phraseIzin.add("(");
                phraseIzin.add(new Chunk("Sakit/Haid/",contentFontStrikeThru));
                phraseIzin.add("Gugur Kandungan");
                phraseIzin.add(new Chunk("/Di Luar Tanggungan Perusahaan/Perpanjangan Izin Di Luar Tanggungan Perusahaan",contentFontStrikeThru));
                phraseIzin.add("*) ");
            }else if(cursor.getString(8).toString().trim().equals("Di Luar Tanggungan Perusahaan")){
                phraseIzin.add("(");
                phraseIzin.add(new Chunk("Sakit/Haid/Gugur Kandungan/",contentFontStrikeThru));
                phraseIzin.add("Di Luar Tanggungan Perusahaan");
                phraseIzin.add(new Chunk("/Di Luar Tanggungan Perusahaan/Perpanjangan Izin Di Luar Tanggungan Perusahaan",contentFontStrikeThru));
                phraseIzin.add("*) ");
            }else if(cursor.getString(8).toString().trim().equals("Perpanjangan Izin Di Luar Tanggungan Perusahaan")){
                phraseIzin.add("(");
                phraseIzin.add(new Chunk("Sakit/Haid/Gugur Kandungan/Di Luar Tanggungan Perusahaan/",contentFontStrikeThru));
                phraseIzin.add("Perpanjangan Izin Di Luar Tanggungan Perusahaan");
                phraseIzin.add("*) ");
            }
            phraseIzin.add("selama ");
            phraseIzin.add(cursor.getString(5).toString());
            phraseIzin.add(" hari, ");
            phraseIzin.add(cursor.getString(6).toString());
        }
        Paragraph p = new Paragraph();
        p.setFont(contentFont);
        p.add(phrase);
        table.addCell(p);

        table.addCell(new Paragraph(" "));

        table.addCell(phr);

        table.addCell(new Paragraph(" "));

        Paragraph pp = new Paragraph();
        pp.setFont(contentFont);
        pp.add(phraseIzin);
        table.addCell(pp);

        table.addCell(new Paragraph(" "));

        document.add(table);

        paragraph.clear();
        enterLine(paragraph,1);
        document.add(paragraph);

        table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setWidths(new int[]{10,11});
        table.getDefaultCell().setBorder(0);
        table.addCell("Alamat dan nomor telepon selama izin / cuti  :");
        phrase = new Phrase();
        phrase.setFont(contentFont);
        phrase.add(cursor.getString(9).toString());
        phrase.add("\n");
        phrase.add(cursor.getString(10).toString());
        table.addCell(new Paragraph(phrase));

        table.addCell(new Paragraph(" "));
        table.addCell(new Paragraph(" "));

        table.addCell(new Paragraph("Nomor telepon : "+cursor.getString(10).toString(),contentFont));
        table.addCell(new Paragraph(" "));
        document.add(table);

        currentDate = new SimpleDateFormat("dd MMMM yyyy");

        table= new PdfPTable(2);
        table.setWidthPercentage(100);
        table.getDefaultCell().setBorder(0);
        table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(new Paragraph(" "));
        table.addCell(new Paragraph(" "));

        table.addCell(new Paragraph(" "));
        table.addCell(new Paragraph("Jakarta, "+currentDate.format(dateNow),contentFont));

        table.addCell(new Paragraph("Menyetujui,",contentFont));
        table.addCell(new Paragraph("Pemohon,",contentFont));

        table.addCell(new Paragraph(cursor.getString(16).toString(),contentFont));
        table.addCell(new Paragraph(" "));

        table.addCell(insertImage(cursor.getString(17).toString()));
        table.addCell(insertImage(cursor.getString(18).toString()));

        table.addCell(new Paragraph(" "));
        table.addCell(new Paragraph(" "));

        table.addCell(new Paragraph(cursor.getString(12).toString(),contentFont));
        table.addCell(new Paragraph(cursor.getString(0).toString(),contentFont));

        document.add(table);
        currentDate = new SimpleDateFormat("dd-MM-yyyy");

        paragraph.clear();
        paragraph.setFont(new Font(Font.FontFamily.TIMES_ROMAN,10));
        enterLine(paragraph,2);
        paragraph.add("Keterangan : \n");
        paragraph.add("     Centang yang sesuai \n");
        paragraph.add("*  ) Coret yang tidak perlu \n");
        paragraph.add("** ) Wajib diisi oleh yang bersangkutan \n");
        paragraph.add("***) Pejabat yang berwenang memberikan cuti/izin \n");
        enterLine(paragraph,1);
        Chunk underline = new Chunk("Dalam hal pelaksanaan Cuti/Izin diizinkan, maka :",new Font(Font.FontFamily.TIMES_ROMAN,10));
        underline.setUnderline(0.1f, -2f); //0.1 thick, -2 y-location
        paragraph.add(underline);
        paragraph.add("\n");
        paragraph.add("Dalam hal setelah menjalankan cuti/izin, pegawai tidak melaporkan diri kepada atasan langsung dan/atau tidak bekerja kembali sebagaimana bisa tanpaketerangan yang sah, dianggap mangkir dan diproses sesuai peraturan disiplin pegawai yang berlaku.");

        document.add(paragraph);
    }
    private void addFooter(Document document,Cursor cursor){

    }
    private Image insertImage(String path) throws IOException, BadElementException {
        File file = new File(path);
        Image image = null;
        if(file.exists()){
            image = Image.getInstance(path);
        }
        return image;
    }
    private void enterLine(Paragraph paragraph, int number){
        for(int i =0;i < number;i++){
            paragraph.add(new Paragraph(" "));
        }
    }
    private void addSeparator(Paragraph paragraph,int length){
        LineSeparator lineSeparator = new LineSeparator();
        lineSeparator.setPercentage(length);
        lineSeparator.setLineColor(new BaseColor(0,0,0));
        paragraph.add(lineSeparator);
    }

    private void addGlueTable(PdfPTable table, Paragraph value1, Paragraph value2){
        Chunk chunk = new Chunk(new VerticalPositionMark());
        Phrase phrase = new Phrase();
        phrase.setFont(contentFont);
        phrase.add(value1);
        phrase.add(chunk);
        phrase.add(value2);
        table.addCell(phrase);
    }

    private Paragraph paragraphWithTab(String key, String value,float interval,Font font){
        Paragraph paragraph =new Paragraph();
        paragraph.setFont(font);
        paragraph.setTabSettings(new TabSettings(interval));
        paragraph.add(key);
        paragraph.add(Chunk.TABBING);
        if(value == "") {
            value = "-";
        }
        paragraph.add(":  " + value);
        return paragraph;
    }
}