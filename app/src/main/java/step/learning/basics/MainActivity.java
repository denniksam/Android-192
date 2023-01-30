package step.learning.basics;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button addButton = findViewById( R.id.addButton ) ;
        addButton.setOnClickListener( this::btnAddExclamationClick ) ;
    }

    private void btnAddExclamationClick( View v ) {
        TextView tvHello = findViewById( R.id.tvHello ) ;
        String txt = tvHello.getText().toString() ;
        txt += "!" ;
        tvHello.setText( txt ) ;
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
