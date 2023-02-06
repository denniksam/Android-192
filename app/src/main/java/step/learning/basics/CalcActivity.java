package step.learning.basics;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class CalcActivity extends AppCompatActivity {
    private TextView tvHistory ;
    private TextView tvResult  ;
    private String minusSign ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calc);

        tvHistory = findViewById( R.id.tvHistory ) ;
        tvResult  = findViewById( R.id.tvResult  ) ;
        tvHistory.setText( "" ) ;
        tvResult.setText( "0" ) ;
        minusSign = getApplicationContext().getString( R.string.calc_minus_sign ) ;

        for( int i = 0; i < 10; i++ ) {
            findViewById(
                getResources().getIdentifier(
                        "button_digit_" + i,
                        "id",
                        getPackageName()
            ) ).setOnClickListener( this::digitClick ) ;
        }
        findViewById( R.id.button_plus_minus ).setOnClickListener( this::pmClick ) ;
        findViewById( R.id.button_inverse ).setOnClickListener( this::inverseClick ) ;

        Log.d( CalcActivity.class.getName(), "onCreate" ) ;
    }

    /**
     * Подія, що виникає при "руйнуванні" активності через зміну конфігурації пристрою
     * @param outState "словник" для збереження ключ-значення
     */
    @Override
    protected void onSaveInstanceState( @NonNull Bundle outState ) {
        super.onSaveInstanceState( outState ) ;   // необхідно залишити
        outState.putCharSequence( "history", tvHistory.getText() ) ;
        outState.putCharSequence( "result",  tvResult.getText()  ) ;
        Log.d( CalcActivity.class.getName(), "Дані збережено" ) ;
    }

    /**
     * Подія, що виникає при відновленні збережених даних після зміни конфігурації
     * @param savedInstanceState "словник" зі збереженими даними
     */
    @Override
    protected void onRestoreInstanceState( @NonNull Bundle savedInstanceState ) {
        super.onRestoreInstanceState( savedInstanceState ) ;
        tvHistory.setText( savedInstanceState.getCharSequence( "history" ) ) ;
        tvResult.setText(  savedInstanceState.getCharSequence( "result"  ) ) ;
        Log.d( CalcActivity.class.getName(), "Дані відновлено" ) ;
    }

    private void pmClick( View v ) {
        // Завдання: натиск кнопки додає знак "-", повторний натиск - прибирає знак
        // Якщо результат "0", то знак не додається
        String result = tvResult.getText().toString() ;
        if( result.startsWith( minusSign ) ) {
            result = result.substring(1 ) ;
        }
        else {
            result = minusSign + result ;
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

    private void inverseClick( View v ) {
        String result = tvResult.getText().toString() ;
        double arg = parseResult( result ) ;
        if( arg == 0 ) {
            Toast                                  // Повідомлення, що "спливає та зникає"
                .makeText(                         //   ~MessageBox
                    CalcActivity.this,             // контекст (активність, що виводить повідомлення)
                    R.string.calc_divide_by_zero,  // рядок або ід ресурсу
                    Toast.LENGTH_SHORT             // тривалість (довжина періоду показу)
                )                                  //
                .show() ;                          // запуск повідомлення
            return ;
        }
        tvHistory.setText( String.format( "1/(%s) =", result ) ) ;
        showResult( 1 / arg ) ;
    }
    private double parseResult( String result ) {
        return Double.parseDouble( result.replace( minusSign, "-" ) ) ;
    }
    private void showResult( double arg ) {
        // реалізувати алгоритм за яким у результаті залишається не більше 10 цифр
        // ("-" та "." не є цифрами та не входять до обмеження)
        String result = String.valueOf( arg ) ;
        int finLength = 10 ;
        if( result.startsWith( "-" ) )
            finLength++ ;
        if( result.contains( "." ) )
            finLength++ ;
        if( result.length() >= finLength ) {
            result = result.substring( 0, finLength ) ;
        }
        tvResult.setText( result.replace( "-", minusSign ) ) ;
    }
}
/*
Д.З. Реалізувати роботу кнопки "корень квадратний"
Забезпечити перевірку аргументу на дозволеність (корень не обчислюється від негативних значень)
Реалізувати ресурс для символу десяткової точки (коми), впровадити роботу з ним.
 */
/*
Конфігурація пристрою, особливості.
- До конфігурації належить: орієнтація пристрою, роздільна здатність, розмір активності, тощо
- При зміні конфігурації відбуваєть "перерахунок" розміщення елементів, а також
 - перезапускається активність.
Операційна система при зміні конфігурації
 - намагається визначити ресурс, який найкраще підходить під задану конфігурацію
    (якщо немає прямого збігу, обирається "найближчий")
 - новий ресурс (навіть якщо змін не було) перевстановлюється і перезапускається активність
    (через що можуть зникнути оперативні дані об'єктів)
 Альтернативи:
 - через маніфест заборонити зміни конфігурацій
 - через маніфест задекларувати власне оброблення подій зміни конфігурації
    у коді реалізувати подію зміни конфігурації (всі зміни необхідно реалізовувати самостійно)
 =============================================================================================
 Збереження та відновлення даних

 */
/*
Реалізувати роботу наступних кнопок калькулятора:
- цифрові кнопки (обмежити введення 10ма цифрами, але знак "-" чи десяткова кома не входять до обмеження)
- кнопка зміни знаку (натиск кнопки додає знак "-", повторний натиск - прибирає знак, Якщо результат "0", то знак не додається)
- десяткова точка (кома) ! може бути тільки одна кома
* backspace (не забути роль коми та знаку)
 */
