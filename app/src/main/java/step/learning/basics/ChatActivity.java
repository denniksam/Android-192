package step.learning.basics;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class ChatActivity extends AppCompatActivity {
    private final String CHAT_URL = "https://diorama-chat.ew.r.appspot.com/story" ;
    private String content ;
    private List<ChatMessage> chatMessages ;
    private ChatMessage userMessage ;
    private LinearLayout chatContainer ;
    private EditText etUserName ;
    private EditText etUserMessage ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        new Thread( this::loadUrl ).start() ;

        chatContainer = findViewById( R.id.chat_container    ) ;
        etUserName    = findViewById( R.id.et_chat_user_name ) ;
        etUserMessage = findViewById( R.id.et_chat_message   ) ;

        findViewById( R.id.btn_chat_send ).setOnClickListener( this::sendButtonClick ) ;
    }
    private void sendButtonClick( View view ) {
        String author = etUserName.getText().toString() ;
        if( author.isEmpty() ) {
            Toast.makeText(this, "Enter author name", Toast.LENGTH_SHORT).show();
            etUserName.requestFocus() ;
            return ;
        }
        String messageText = etUserMessage.getText().toString() ;
        this.userMessage = new ChatMessage() ;
        this.userMessage.setAuthor( author ) ;
        this.userMessage.setText( messageText ) ;

        new Thread( this::postUserMessage ).start() ;
    }
    private void postUserMessage() {   // send (POST) message to backend
        try {
            URL chatUrl = new URL( CHAT_URL ) ;
            // POST request - 3 steps
            // 1. Configuring connection
            HttpURLConnection connection = (HttpURLConnection) chatUrl.openConnection() ;
            connection.setDoOutput( true ) ;   // Request has a body
            connection.setDoInput( true ) ;    // Response expected
            connection.setRequestMethod( "POST" ) ;
            connection.setRequestProperty( "Content-Type", "application/json" ) ;
            connection.setRequestProperty( "Accept", "*/*" ) ;
            connection.setChunkedStreamingMode( 0 ) ;  // without chunking

            // 2. Output - forming (writing) a body
            OutputStream body = connection.getOutputStream() ;
            body.write( userMessage.toJsonString().getBytes() ) ;
            body.flush() ;  // send request
            body.close() ;

            // 3. Input - Reading response
            int responseCode = connection.getResponseCode() ;
            if( responseCode >= 400 ) {  // Errors
                Log.d( "postUserMessage", "Request fails with code " + responseCode ) ;
                return ;
            }
            // connection.getHeaderFields()  -- Response headers
            InputStream response = connection.getInputStream() ;
            ByteArrayOutputStream bytes = new ByteArrayOutputStream() ;
            byte[] chunk = new byte[4096] ;
            int len ;
            while( ( len = response.read( chunk ) ) != -1 ) {
                bytes.write( chunk, 0, len ) ;
            }
            String responseBody = new String( bytes.toByteArray(), StandardCharsets.UTF_8 ) ;
            Log.i( "postUserMessage", responseBody ) ;

            // Final. Closing resources
            bytes.close() ;
            response.close() ;
            connection.disconnect() ;

            // refresh chat
            new Thread( this::loadUrl ).start() ;
        }
        catch( Exception ex ) {
            Log.d( "postUserMessage", ex.getMessage() ) ;
        }
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
    private void parseContent() {
        try {
            JSONObject object = new JSONObject( content ) ;
            if( "success".equals( object.getString("status") ) ) {
                JSONArray array = object.getJSONArray( "data" ) ;
                this.chatMessages = new ArrayList<>() ;
                int length = array.length() ;
                for( int i = 0; i < length; ++i ) {
                    this.chatMessages.add(
                            new ChatMessage(
                                    array.getJSONObject( i ) ) ) ;
                }
                runOnUiThread( this::showChatMessages ) ;
            }
        }
        catch( JSONException ex ) {
            Log.d("ChatActivity::parseContent", "JSONException: " + ex.getMessage());
        }
    }
    private void showChatMessages() {
        String author = etUserName.getText().toString() ;
        Drawable otherBg = AppCompatResources.getDrawable(
                getApplicationContext(),
                R.drawable.rates_shape ) ;
        Drawable myBg = AppCompatResources.getDrawable(
                getApplicationContext(),
                R.drawable.rates_r_shape ) ;

        for( ChatMessage chatMessage : this.chatMessages ) {
            TextView tvMessage = new TextView(this);
            tvMessage.setText(
                    String.format("%s: %s - %s%n",
                            chatMessage.getMoment(),
                            chatMessage.getAuthor(),
                            chatMessage.getText()));
            tvMessage.setTextSize(16);
            tvMessage.setPadding(10, 5, 10, 5);
            tvMessage.setBackground(
                    author.equals( chatMessage.getAuthor() )
                        ? myBg
                        : otherBg
            );
            chatContainer.addView(tvMessage);
        }
    }

    private static class ChatMessage {
        /* ORM for JSON data
        {
          "id": "90a727da-b035-11ed-a51d-f23c93f195e6",
          "author": "John Smith",
          "txt": "So laugh out loud",
          "moment": "Feb 19, 2023 9:12:42 AM",
          "idReply": "f9fcdc8d-b034-11ed-a51d-f23c93f195e6",
          "replyPreview": "It is sile..."
        } */
        private UUID   id ;
        private String author ;
        private String text ;
        private Date   moment ;
        private UUID   idReply ;        // if this message is a reply for other message
        private String replyPreview ;   // "navigation field" - fragment of cited message
        private static final SimpleDateFormat scanFormat =   // "Feb 19, 2023 9:12:42 AM"
                new SimpleDateFormat( "MMM d, yyyy KK:mm:ss a", Locale.US ) ;
        public ChatMessage() {
        }
        public ChatMessage( JSONObject object ) throws JSONException {
            // required fields
            setId( UUID.fromString( object.getString( "id" ) ) ) ;
            setAuthor( object.getString( "author" ) ) ;
            setText( object.getString( "txt" ) ) ;
            try {
                setMoment(
                        scanFormat.parse(
                                object.getString( "moment" ) ) ) ;
            }
            catch( ParseException ex ) {
                throw new JSONException( "Date (moment) parse error: "
                        + object.getString( "moment" ) ) ;
            }
            // optional fields
            if( object.has( "idReply" ) ) {
                setIdReply( UUID.fromString( object.getString( "idReply" ) ) ) ;
            }
            if( object.has( "replyPreview" ) ) {
                setReplyPreview( object.getString( "replyPreview" ) ) ;
            }
        }

        public String toJsonString() {
            StringBuilder sb = new StringBuilder() ;
            sb.append(
                    String.format( "{\"author\":\"%s\", \"txt\":\"%s\"",
                            getAuthor(), getText() ) ) ;
            if( idReply != null )
                sb.append(
                        String.format( ", \"idReply\":\"%s\"", getIdReply() ) ) ;
            sb.append( "}" ) ;
            return sb.toString() ;
        }
        public UUID getId() {
            return id;
        }
        public void setId(UUID id) {
            this.id = id;
        }
        public String getAuthor() {
            return author;
        }
        public void setAuthor(String author) {
            this.author = author;
        }
        public String getText() {
            return text;
        }
        public void setText(String text) {
            this.text = text;
        }
        public Date getMoment() {
            return moment;
        }
        public void setMoment(Date moment) {
            this.moment = moment;
        }
        public UUID getIdReply() {
            return idReply;
        }
        public void setIdReply(UUID idReply) {
            this.idReply = idReply;
        }
        public String getReplyPreview() {
            return replyPreview;
        }
        public void setReplyPreview(String replyPreview) {
            this.replyPreview = replyPreview;
        }
    }
}
/*
    Т.З. Реалізувати відображення повідомлень:
     - використати оформлення, різне для власних та інших повідомлень
        (власні повідомлення визначаємо за збігом імені автора у об'єкті-повідомленні та
         полі введення імені автора)
     - змінити порядок відображення: останні знизу
     * автоматично прокручувати контейнер до останнього повідомлення
   Змінити алгоритм розбирання контенту - замість руйнування колекції повідомлень
    реалізувати перевірку чи є повідомлення у колекції (за id), додавати до колекції лише
    нові повідомлення
   * Реалізувати періодичне оновлення контенту, причому якщо нових повідомлень немає,
      то не оновлювати відображення.
   Реалізувати перевірку повідомлення на пустоту (?на граничну довжину)
   * "Розумне" відображення дати/часу: якщо повідомлення за "сьогодні", то виводити тільки
       час. Якщо ні, то повну дату\час
 */