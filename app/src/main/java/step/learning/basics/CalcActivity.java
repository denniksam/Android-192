package step.learning.basics;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class CalcActivity extends AppCompatActivity {
    private TextView tvHistory ;
    private TextView tvResult  ;
    private String minusSign ;
    private boolean needClearResult ;   // потреба стирання результату перед введенням (після операцій)
    private boolean needClearHistory ;
    private double operand1 ;    // збережені відомості про перший аргумент операції
    private String operation ;   // та текст кнопки (операції), яка має виконатись

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
        findViewById( R.id.button_clear_e ).setOnClickListener( this::clearEntryClick ) ;
        findViewById( R.id.button_clear_all ).setOnClickListener( this::clearAllClick ) ;
        findViewById( R.id.button_backspace ).setOnClickListener( this::backspaceClick ) ;
        findViewById( R.id.button_plus ).setOnClickListener( this::fnButtonClick ) ;
        findViewById( R.id.button_minus ).setOnClickListener( this::fnButtonClick ) ;
        findViewById( R.id.button_multiply ).setOnClickListener( this::fnButtonClick ) ;
        findViewById( R.id.button_divide ).setOnClickListener( this::fnButtonClick ) ;
        findViewById( R.id.button_equal ).setOnClickListener( this::equalClick ) ;

        // Log.d( CalcActivity.class.getName(), "onCreate" ) ;
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
         String result = tvResult.getText().toString() ;
         if( needClearResult ) {
             needClearResult = false ;
             result = "0" ;
         }
         if( result.length() >= 10 ) return ;  // Завдання: обмежити введення 10ма цифрами
         String digit = ((Button) v).getText().toString() ;
         if( result.equals( "0" ) ) {
             result = digit ;
         }
         else {
             result += digit ;
         }
         tvResult.setText( result ) ;
         if( needClearHistory ) {
             tvHistory.setText( "" ) ;
             needClearHistory = false ;
         }
     }
    private void inverseClick( View v ) {
        String result = tvResult.getText().toString() ;
        double arg = parseResult( result ) ;
        if( arg == 0 ) {
            alert( R.string.calc_divide_by_zero ) ;
            return ;
        }
        tvHistory.setText( String.format( "1/(%s) =", result ) ) ;
        showResult( 1 / arg ) ;
    }
    private void alert( int stringId ) {
        Toast                                  // Повідомлення, що "спливає та зникає"
            .makeText(                         //   ~MessageBox
                CalcActivity.this,             // контекст (активність, що виводить повідомлення)
                stringId,                      // рядок або ід ресурсу
                Toast.LENGTH_SHORT             // тривалість (довжина періоду показу)
            )                                  //
            .show() ;                          // запуск повідомлення
        // vibrate
        Vibrator vibrator ;
        long[] vibratePattern = { 0, 200, 100, 200 } ;   // Pause(ms)-Active(ms)-P-A....
        if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ) {
            VibratorManager vibratorManager = (VibratorManager)
                    getSystemService( Context.VIBRATOR_MANAGER_SERVICE ) ;
            vibrator = vibratorManager.getDefaultVibrator() ;
        }
        else {
            vibrator = (Vibrator) getSystemService(
                    Context.VIBRATOR_SERVICE ) ;   // Deprecated API 31 (S)
        }

        if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ) {
            vibrator.vibrate(
                    // VibrationEffect.createOneShot( 300, VibrationEffect.DEFAULT_AMPLITUDE )
                    VibrationEffect.createWaveform( vibratePattern, -1 )
            ) ;
        }
        else {   // Deprecated API 26 (O)
            // vibrator.vibrate( 300 ) ;  // однократна вібрація 300 мілісекунд
            vibrator.vibrate( vibratePattern, -1 ) ;  // -1 без повторення
        }
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
    private void clearEntryClick( View v ) {   // CE button
        tvResult.setText( "0" ) ;
    }
    private void clearAllClick( View v ) {    // C button
        tvHistory.setText( "" ) ;
        tvResult.setText( "0" ) ;
    }
    private void backspaceClick( View v ) {
        if( needClearHistory ) {
            tvHistory.setText( "" ) ;
            needClearHistory = false ;
        }
        if( needClearResult ) {
            needClearResult = false ;
        }
        String result = tvResult.getText().toString() ;
        int len = result.length() ;
        result = result.substring( 0, len - 1 ) ;
        if( result.equals( minusSign ) || len <= 1 ) {
            result = "0" ;
        }
        tvResult.setText( result ) ;
    }
    private void fnButtonClick( View v ) {   // for fn buttons (+*-/)
        String fn = ((Button) v).getText().toString() ;
        String result = tvResult.getText().toString() ;
        String history = String.format( "%s %s", result, fn ) ;
        tvHistory.setText( history ) ;
        needClearResult = true ;
        operation = fn ;
        operand1 = parseResult( result ) ;
    }
    private void equalClick( View v ) {   // for button =
        String result  = tvResult.getText().toString() ;
        String history = tvHistory.getText().toString() ;
        tvHistory.setText( String.format( "%s %s =", history, result ) ) ;
        double operand2 = parseResult( result ) ;
        if( operation.equals( getString( R.string.btn_calc_plus ) ) ) {
            showResult( operand1 + operand2 ) ;
        }
        needClearResult = true ;
        needClearHistory = true ;
    }
}
/*
    Д.З.
    Змінити алгоритм відображення результату - передбачити
      виведення експоненційної форми ( 1.73240E8 = 1.73240 x 10^8 )
      [на поточний момент відомості про експоненту відрізаються]
    Реалізувати роботу усіх fn кнопок (не забути про ділення на 0)
    Організувати збереження необхідних даних (аргумент1, операція, тощо)
      при змінах конфігурації пристрою
    Перевіряти збережені дані на null перед їх обробленням
 */
/*
    Робота з системними сервісами (на прикладі вібрації)
1. Для роботи з системними пристроями необхідно отримати на це дозвіл
    Згідно з політикою пристрою деякі дозволи надаються автоматично,
    інші потребують згоди користувача. Але навіть автоматичний дозвіл
    запитати необхідно. Запит на дозвіл оформюється у маніфесті
        <uses-permission android:name="android.permission.VIBRATE" />
2. Формалізм роботи з сервісами зазнає змін з розвитком версій ОС (API)
    для сумісності з різними версіями програма оформлюється у вигляді
    розгалужених команд під різні версії ОС
        Build.VERSION.SDK_INT - номер API ОС пристрою
 */
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
