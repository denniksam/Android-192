package step.learning.basics;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class ChatActivity extends AppCompatActivity {
    private final String CHAT_URL = "https://diorama-chat.ew.r.appspot.com/story" ;
    private String content ;
    private LinearLayout chatContainer ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        chatContainer = findViewById( R.id.chat_container ) ;

        new Thread( this::loadUrl ).start() ;
    }
    private void loadUrl() {
        try( InputStream urlStream = new URL( CHAT_URL ).openStream() ) {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream() ;
            byte[] chunk = new byte[4096] ;
            int len ;
            while( ( len = urlStream.read( chunk ) ) != -1 ) {
                bytes.write( chunk, 0, len ) ;
            }
            content = new String( bytes.toByteArray(), StandardCharsets.UTF_8 ) ;
            bytes.close() ;
            runOnUiThread( this::showChatMessages ) ;
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
    private void showChatMessages() {
        TextView tvMessage = new TextView( this ) ;
        tvMessage.setText( content ) ;
        chatContainer.addView( tvMessage ) ;
    }
}
/*
    Д.З. Реалізувати розбір контенту у JSON:
    видобути об'єкт
    перевірити статус
    з поля "data" видобути масив
    Пройти масив циклом, вивести повідомлення у форматі:
    дата: автор - текст
 */