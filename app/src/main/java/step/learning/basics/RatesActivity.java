package step.learning.basics;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class RatesActivity extends AppCompatActivity {
    private final String nbuApiUrl = "https://bank.gov.ua/NBUStatService/v1/statdirectory/exchange?json" ;
    private TextView tvContent ;
    private String content;     // рядок для tvContent
    private List<Rate> rates ;  // колекція ORM об'єктів (видобутих з JSON)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rates);

        tvContent = findViewById( R.id.tv_rates_content ) ;

        // loadUrl() ; -- NetworkOnMainThreadException
        new Thread( this::loadUrl ).start() ;
    }
    private void loadUrl() {   // load content from Internet site given by URL
        try( InputStream urlStream = new URL( nbuApiUrl ).openStream() ) {
            /*StringBuilder sb = new StringBuilder() ;
            int sym ;
            while( ( sym = urlStream.read() ) != -1 ) {
                sb.append( (char) sym ) ;
            }
            // tvContent.setText( sb.toString() ) ;  -- CalledFromWrongThreadException
            // content = sb.toString()  -- помилки кодування
            content = new String(
                sb.toString().getBytes( StandardCharsets.ISO_8859_1 ),
                StandardCharsets.UTF_8 ) ;
             */
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            byte[] chunk = new byte[65536];
            int len;
            while( ( len = urlStream.read( chunk ) ) != -1 ) {
                bytes.write( chunk, 0, len ) ;
            }
            content = new String( bytes.toByteArray(), StandardCharsets.UTF_8 ) ;
            bytes.close() ;
            new Thread( this::parseContent ).start() ;
        }
        catch( android.os.NetworkOnMainThreadException ignored ) {
            Log.d( "loadUrl", "NetworkOnMainThreadException " ) ;
        }
        catch( MalformedURLException ex ) {
            Log.d( "loadUrl", "MalformedURLException: " + ex.getMessage() ) ;
        }
        catch( IOException ex ) {
            Log.d( "loadUrl", "IOException: " + ex.getMessage() ) ;
        }
    }
    private void parseContent() {   // try parse site content to object form
        try {
            JSONArray array = new JSONArray( content ) ;
            rates = new ArrayList<>() ;
            int len = array.length() ;
            for( int i = 0; i < len; ++i ) {
                rates.add( new Rate( array.getJSONObject( i ) ) ) ;
            }
        }
        catch( JSONException ex ) {
            Log.d( "parseContent", "JSONException: " + ex.getMessage() ) ;
            return ;
        }
        runOnUiThread( this::showContent ) ;
    }
    private void showContent() {
        StringBuilder sb = new StringBuilder() ;
        /*
        Д.З. Реалізувати відображення курсів валют з ORM-колекції rates
        Дату (exchangedate) вивести один раз на початку переліку
        Далі вивести всі інші відомості про валюти та їх курси
        Додати ScrollView для прокрутки контенту
         */
        tvContent.setText( content ) ;
    }

    static class Rate {   // ORM - відображення JSON на об'єкти
        private int r030;
        private String txt;
        private double rate;
        private String cc;
        private String exchangeDate;

        public Rate( JSONObject obj ) throws JSONException {
            setR030( obj.getInt(    "r030" ) ) ;
            setTxt(  obj.getString( "txt"  ) ) ;
            setRate( obj.getDouble( "rate" ) ) ;
            setCc(   obj.getString( "cc"   ) ) ;
            setExchangeDate( obj.getString( "exchangedate" ) ) ;
        }

        public int getR030() {
            return r030;
        }
        public void setR030(int r030) {
            this.r030 = r030;
        }
        public String getTxt() {
            return txt;
        }
        public void setTxt(String txt) {
            this.txt = txt;
        }
        public double getRate() {
            return rate;
        }
        public void setRate(double rate) {
            this.rate = rate;
        }
        public String getExchangeDate() {
            return exchangeDate;
        }
        public String getCc() {
            return cc;
        }
        public void setCc(String cc) {
            this.cc = cc;
        }
        public void setExchangeDate(String exchangeDate) {
            this.exchangeDate = exchangeDate;
        }
    }
    /* {    "r030": 156,
            "txt": "Юань Женьміньбі",
            "rate": 5.3345,
            "cc": "CNY",
            "exchangedate": "17.02.2023" },
     */
}
/*
Робота з мережею Internet
На прикладі отримання курсів валют з API НБУ
https://bank.gov.ua/NBUStatService/v1/statdirectory/exchange?json
Головним об'єктом для роботи є URL. Аналогічно з подібними об'єктами (File) створення нового
 об'єкту не призводить до дій з ресурсом.
Особливості:
 - після створення об'єкту URL необхідно а) відкрити з'єднання, б) отримати потік даних
 - відкривати підключення до сайтів неможна з основного (UI) потоку. Це призводить до
     android.os.NetworkOnMainThreadException
     рішення - виділяти код підключення у окремий метод та запускати його в окремому потоці
 - для відкриття підключення застосунку потрібен системний дозвіл. Без нього можливі виключення:
      java.lang.SecurityException: Permission denied (missing INTERNET permission?)
      IOException: Unable to resolve host "bank.gov.ua": No address associated with hostname
      рішення - зазначити дозвіл у маніфесті <uses-permission android:name="android.permission.INTERNET" />
      + можливо доведеться перевстановити застосунок, оскільки дозволи можуть не оновитись
 - оскільки робота з мережею відбувається у окремому потоці, звернення до UI елементів блокується:
      android.view.ViewRootImpl$CalledFromWrongThreadException: Only the original thread that created a view hierarchy can touch its views.
 - за стандартом HTTP контент передається у кодуванні ASCII (ISO), але його внутрішнє кодування
      у більшості випадків - UTF-8. Внутрішнє кодування Java - UTF-16
      Особливість UTF-8 у тому, що один символ кодується різною кількістю байтів. Відповідно,
      перекодувати символи неможна, необхідно повернути рядок у байт-масив та повторити процедуру
      кодування.
 */