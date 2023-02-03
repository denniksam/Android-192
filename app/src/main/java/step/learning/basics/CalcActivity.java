package step.learning.basics;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class CalcActivity extends AppCompatActivity {
    private TextView tvHistory ;
    private TextView tvResult  ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calc);

        tvHistory = findViewById( R.id.tvHistory ) ;
        tvResult  = findViewById( R.id.tvResult  ) ;
        tvHistory.setText( "" ) ;
        tvResult.setText( "0" ) ;

        for( int i = 0; i < 10; i++ ) {
            findViewById(
                getResources().getIdentifier(
                        "button_digit_" + i,
                        "id",
                        getPackageName()
            ) ).setOnClickListener( this::digitClick ) ;
        }
        findViewById( R.id.button_plus_minus ).setOnClickListener( this::pmClick ) ;
    }
    private void pmClick( View v ) {
        // Завдання: натиск кнопки додає знак "-", повторний натиск - прибирає знак
        // Якщо результат "0", то знак не додається
        String result = tvResult.getText().toString() ;
        if( result.startsWith( "-" ) ) {
            result = result.substring(1 ) ;
        }
        else {
            result = "-" + result ;
        }
        tvResult.setText( result ) ;
    }
    private void digitClick( View v ) {
        // Завдання: обмежити введення 10ма цифрами
         String result = tvResult.getText().toString() ;
         if( result.length() >= 10 ) return ;

         String digit = ((Button) v).getText().toString() ;

         if( result.equals( "0" ) ) {
             result = digit ;
         }
         else {
             result += digit ;
         }
         tvResult.setText( result ) ;
     }
}
/*
Реалізувати роботу наступних кнопок калькулятора:
- цифрові кнопки (обмежити введення 10ма цифрами, але знак "-" чи десяткова кома не входять до обмеження)
- кнопка зміни знаку (натиск кнопки додає знак "-", повторний натиск - прибирає знак, Якщо результат "0", то знак не додається)
- десяткова точка (кома) ! може бути тільки одна кома
* backspace (не забути роль коми та знаку)
 */
