package step.learning.basics;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button calcButton = findViewById( R.id.calcButton ) ;
        calcButton.setOnClickListener( this::btnCalcClick ) ;

        findViewById( R.id.exitButton )
                .setOnClickListener( this::btnExitClick ) ;
        findViewById( R.id.gameButton )
                .setOnClickListener( this::btnGameClick ) ;
    }
    private void btnGameClick( View v ) {
        Intent gameIntent = new Intent(
                MainActivity.this,
                GameActivity.class ) ;
        startActivity( gameIntent ) ;
    }
    private void btnCalcClick( View v ) {
        Intent calcIntent = new Intent(
                MainActivity.this,
                CalcActivity.class ) ;
        startActivity( calcIntent ) ;
    }
    private void btnExitClick( View v ) {
        finish() ;
    }
}
/*
Д.З. Встановити та налагодити ПЗ:
- Android Studio (+SDK)
- Emulator (+OS) / реальний пристрій
Реалізувати проєкт, створений на занятті,
запустити його на пристрої.
Зробити скріншот коду + результату роботи (якщо буде репозиторій, то додати скріншот до нього)
 */
/*
Одиниці вимірювання у Android
px - pixel
dp, dip - density independent pixel - піксель, адаптований до розміру (на 1 дюйм завжди однакова
   їх кількість)
sp - scalable - відносний до масштабу (для шрифтів)
in - дюйм
mm - міліметр
 */
