package step.learning.basics;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

// import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.net.ssl.HttpsURLConnection;

public class GameActivity extends AppCompatActivity {
    private final int[][] cells = new int[4][4] ;  // числові значення комірок поля
    private final TextView[][] tvCells = new TextView[4][4] ;   // посилання на візуальні елементи поля
    private final Random random = new Random() ;
    private Animation spawnCellAnimation ;   // посилання на ресурс анімації
    private int score ;
    private TextView tvScore ;
    private int bestScore ;
    private TextView tvBestScore ;
    private final String bestScoreFilename = "best_score_192.txt" ;
    private boolean isContinuePlaying ;   // продовження гри після набору 2048

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        tvScore = findViewById( R.id.tv_score ) ;
        bestScore = loadBestScore() ;
        tvBestScore = findViewById( R.id.tv_best_score ) ;
        tvBestScore.setText( getString( R.string.tv_best_score_pattern, bestScore ) ) ;

        // Завантажуємо ресурс анімації
        spawnCellAnimation = AnimationUtils.loadAnimation(
                GameActivity.this,
                R.anim.spawn_cell
        ) ;
        // Ініціалізуємо анімацію
        spawnCellAnimation.reset() ;

        // Складаємо масив з посиланнями на TextView комірок
        for( int i = 0; i < 4; ++i ) {
            for( int j = 0; j < 4; ++j ) {
                tvCells[i][j] = findViewById(
                        getResources().getIdentifier(
                                "game_cell_" + i + j,
                                "id",
                                getPackageName()
                        ) ) ;
            }
        }

        // Задаємо детектор жестів - свайпи
        findViewById( R.id.game_layout )
                .setOnTouchListener( new OnSwipeListener( GameActivity.this ) {
                    @Override
                    public void onSwipeLeft() {
                        if( moveLeft() ) spawnCell() ;
                        else Toast.makeText(GameActivity.this, "No Left Move", Toast.LENGTH_SHORT).show();
                    }
                    @Override
                    public void onSwipeRight() {
                        if( moveRight() ) spawnCell() ;
                        else Toast.makeText(GameActivity.this, "No Right Move", Toast.LENGTH_SHORT).show();
                    }
                    @Override
                    public void onSwipeTop() {
                        Toast.makeText(GameActivity.this, "Top", Toast.LENGTH_SHORT).show();
                    }
                    @Override
                    public void onSwipeBottom() {
                        Toast.makeText(GameActivity.this, "Bottom", Toast.LENGTH_SHORT).show();
                    }
                } ) ;
        findViewById( R.id.game_start_new )
                .setOnClickListener( this::newGameClick ) ;

        startNewGame() ;
    }

    private void newGameClick( View v ) {

    }

    private boolean saveBestScore() {
        // MODE_PRIVATE - робота з приватним каталогом програми, не потребує дозволів на файлову систему, але обмежує шляхи файлів
        try( FileOutputStream fos = openFileOutput( bestScoreFilename, Context.MODE_PRIVATE ) ) {
            DataOutputStream writer = new DataOutputStream( fos ) ;
            writer.writeInt( bestScore ) ;
            writer.flush() ;
            writer.close() ;
        }
        catch( IOException ex ) {
            Log.d( "saveBestScore", ex.getMessage() ) ;
            return false ;
        }
        return true ;
    }
    private int loadBestScore() {
        int best = 0 ;
        try( FileInputStream fis = openFileInput( bestScoreFilename ) ) {
            DataInputStream reader = new DataInputStream( fis ) ;
            best = reader.readInt() ;
            reader.close() ;
        }
        catch( IOException ex ) {
            Log.d( "loadBestScore", ex.getMessage() ) ;
        }
        return best ;
    }

    private boolean isWin() {   // Перевіряємо чи зібрав користувач 2048 хоча б у одній комірці
        for( int i = 0; i < 4; ++i ) {
            for( int j = 0; j < 4; ++j ) {
                if( cells[i][j] == 8 ) {   // 2048
                    return true ;
                }
            }
        }
        return false ;
    }
    private void showWinDialog() {   // Виведення повідмлення про виграш та оброблення варіантів Продовжити/Нова гра/Вийти
        new AlertDialog.Builder(     // конструктор діалогових вікон за патерном Builder
                GameActivity.this, androidx.appcompat.R.style.Theme_AppCompat_Dialog_Alert )
                .setTitle( R.string.game_victory_title )
                .setIcon( android.R.drawable.ic_dialog_info )
                .setMessage( "Ви зібрали 2048 та виграли!")
                .setCancelable( false )  // неможна закрити без вибору дії
                .setPositiveButton("Продовжити", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick( DialogInterface dialog, int whichButton ) {
                        isContinuePlaying = true ;
                    } } )
                .setNegativeButton( "Вихід", (dialog, whichButton) -> {
                    finish() ;
                } )
                .setNeutralButton( "Нова гра", (dialog, whichButton) -> {
                    startNewGame() ;
                } )
                .show() ;
    }

    private void startNewGame() {
        score = 0 ;
        isContinuePlaying = false ;
        // зануляємо поле
        for( int i = 0; i < 4; ++i ) {
            for( int j = 0; j < 4; ++j ) {
               cells[i][j] = 0 ;
            }
        }
        // генеруємо першi значення на полі
        spawnCell() ;
        spawnCell() ;
    }

    // відображення значень та підбір стилів у відповідності до значень
    private void showField() {
        Resources resources = getResources() ;
        for( int i = 0; i < 4; ++i ) {
            for( int j = 0; j < 4; ++j ) {
                tvCells[i][j].setText( String.valueOf( cells[i][j] ) ) ;
                tvCells[i][j].setTextAppearance(                    // застосування стилю (ресурсу)
                        // R.style.GameCell_2 ) ;                   // але змінюється не все
                        resources.getIdentifier(                    // textSize   +
                                "GameCell_" + cells[i][j],          // textColor  +
                                "style",                            // background -
                                getPackageName()
                        ) ) ;

                // інші стильові властивості доводиться застосовувати окремо
                tvCells[i][j].setBackgroundColor(
                    resources.getColor(
                        resources.getIdentifier(
                                "game_bg_" + cells[i][j],
                                "color",
                                getPackageName()
                        ),
                        getTheme() ) ) ;
            }
        }
        // відображаємо рахунок гри
        tvScore.setText( getString( R.string.tv_score_pattern, score ) ) ;
        // перевіряємо рекорд
        if( score > bestScore ) {
            bestScore = score ;
            if( saveBestScore() ) {
                tvBestScore.setText( getString( R.string.tv_best_score_pattern, bestScore ) ) ;
            }
        }
        // перевіряємо умову перемоги
        if( ! isContinuePlaying ) {   // якщо раніше вибрано продовжувати гру, то перевірку не проводимо
            if( isWin() ) {
                showWinDialog() ;
            }
        }
    }

    // поява нового числа у випадковій порожній комірці (з ймовірностями 0.9->2; 0.1->4)
    private boolean spawnCell() {
        // збираємо дані про порожні комірки
        List<Integer> freeCellIndexes = new ArrayList<>() ;
        for( int i = 0; i < 4; ++i ) {
            for( int j = 0; j < 4; ++j ) {
                if( cells[i][j] == 0 ) {
                    freeCellIndexes.add( i * 10 + j ) ;  // два індекси в одному числі
                }
                // cells[i][j] = random.nextInt( 8 ) ;   // 0...7
                // if( cells[i][j] != 0 ) cells[i][j] = (int) Math.pow( 2, cells[i][j] ) ;  // 2^(1..7)
            }
        }
        // перевіряємо чи взагалі є порожні
        int cnt = freeCellIndexes.size() ;
        if( cnt == 0 ) return false ;
        // генеруємо випадковий індекс
        int randIndex = random.nextInt( cnt ) ;
        // розділяємо на два збережені дані під цим індексом
        int x = freeCellIndexes.get( randIndex ) / 10 ;
        int y = freeCellIndexes.get( randIndex ) % 10 ;
        // генеруємо випадкове число для нової комірки (2/4)
        cells[x][y] = random.nextInt(10) < 9 ? 2 : 4 ;
        // призначаємо анімацію появи для комірки
        tvCells[x][y].startAnimation( spawnCellAnimation ) ;

        showField() ;
        return true ;
    }

    private boolean moveLeft() {
        boolean result = false ;
        boolean needRepeat ;
        // переміщуємо усі числа ліворуч
        for( int i = 0; i < 4; ++i ) {  // цикл по рядках (і - індекс рядка)
            do {
                needRepeat = false ;
                for( int j = 0; j < 3; ++j ) {   // цикл по елементах ряду (j - індекс комірки у ряді)
                    if( cells[i][j] == 0 ) {  // є порожня комірка - якщо праворуч є непорожні - переносимо
                        for( int k = j + 1; k < 4; ++k ) {
                            if( cells[i][k] != 0 ) {   // є непорожня
                                cells[i][j] = cells[i][k] ;
                                cells[i][k] = 0 ;
                                needRepeat = true ;
                                result = true ;
                                break ;
                            }
                        }
                    }
                }
            } while( needRepeat ) ;

            // колапс - з'єднання сусідніх однакових значень
            for( int j = 0; j < 3; ++j ) {  // рух зліва праворуч
                if( cells[i][j] != 0 && cells[i][j] == cells[i][j + 1] ) {   // [2288]
                    cells[i][j] *= 2 ;  // [4288]
                    for( int k = j + 1; k < 3; ++k ) {   // посуваємо праву частину на звільнене місце
                        cells[i][k] = cells[i][k + 1] ;  // [4888]
                    }
                    cells[i][3] = 0 ;    // [4880]
                    result = true ;
                    score += cells[i][j] ;
                }
            }
        }
        return result ;
    }
    private boolean moveRight() {
        boolean result = false ;
        boolean needRepeat ;
        // переміщуємо усі числа праворуч
        for( int i = 0; i < 4; ++i ) {  // цикл по рядках (і - індекс рядка)
            do {
                needRepeat = false ;
                for( int j = 3; j > 0; --j ) {
                    if( cells[i][j] == 0 ) {
                        for( int k = j - 1; k >= 0; --k ) {
                            if( cells[i][k] != 0 ) {
                                cells[i][j] = cells[i][k] ;
                                cells[i][k] = 0 ;
                                needRepeat = true ;
                                result = true ;
                                break ;
                            }
                        }
                    }
                }
            } while( needRepeat ) ;

            // колапс - з'єднання сусідніх однакових значень
            for( int j = 3; j > 0; --j ) {  // рух ліворуч
                if( cells[i][j] != 0 && cells[i][j] == cells[i][j - 1] ) {   // [2244]
                    cells[i][j] *= 2 ;  // [2248]
                    for( int k = j - 1; k > 0; --k ) {   // посуваємо праву частину на звільнене місце
                        cells[i][k] = cells[i][k - 1] ;  // [2228]
                    }
                    cells[i][0] = 0 ;    // [0228]
                    result = true ;
                    score += cells[i][j] ;
                }
            }
        }
        return result ;
    }
}
/*
Анімації
а) кадрові - зміна статичних картинок
б) (WPF~double, CSS-transition) - плавна зміна числової характеристики від початкового до кінцевого значення
Анімації є стандартним ресурсом, зберігаються у папці "anim" і доступні через R.anim
1. Утворюються XML файлом з одним з базових тегів
    alpha - прозорість (0-прозорий 1-видимий)
    rotate - поворот
    scale - масштаб
    translate - зсув
2. Завантажується анімація через spawnCellAnimation = AnimationUtils.loadAnimation(...)
    Рекомендується  spawnCellAnimation.reset() ;
3. Анімація належить до View (довільний View може програти анімацію цих типів)
    запускається через view.startAnimation(spawnCellAnimation)
    виконується асинхронно - не блокує подальшу роботу
 */
/*
Д.З. Реалізувати хід праворуч
* реалізувати колапс - злиття сусідніх комірок з однаковим значенням
   - зливаються попарно, НЕ рекурсивно ( 2222 --> 44 )
   - вирівнювання рекурсивне ( 2222 --> 4040 --> 4400 ) (2220 --> 4200) (2280 --> 4080 --> 4800)
 */
/*
Д.З. Завершити роботу над проєктом 2048 (на це Д.З. довільні 2 пункти з ТЗ)

    Т.З.
    Завершити дизайн для всіх елементів
    Реалізувати вертикальні ходи (вгору/вниз)
    Всі рядки перенести у ресурси
    Впровадити анімацію злиття (на базі scale від 1.1 до 1.0)
    Реалізувати негативний фінал (немає ходів)
     - перевірка стану
     - діалог-повідомлення "Вихід"/"Нова гра"/"Відмінити останній хід"
    Забезпечити роботи кнопки UNDO (відміна ходу) - тільки на один хід
     - на початку гри кнопку деактивувати
    Для кнопки NEW (нова гра) додати повідомлення-підтвердження

    * Реалізувати збереження гри (тільки однієї у поточному стані)
       та її відновлення
    * Додати дизайн ландшафтної орієнтації, забезпечити збережність
       даних при зміні орієнтації
 */