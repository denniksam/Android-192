package step.learning.basics;

import androidx.appcompat.app.AppCompatActivity;

import android.content.res.Resources;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameActivity extends AppCompatActivity {
    private final int[][] cells = new int[4][4] ;  // числові значення комірок поля
    private final TextView[][] tvCells = new TextView[4][4] ;   // посилання на візуальні елементи поля
    private final Random random = new Random() ;
    private Animation spawnCellAnimation ;   // посилання на ресурс анімації

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

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
                        Toast.makeText(GameActivity.this, "Right", Toast.LENGTH_SHORT).show();
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

        // генеруємо перше значення на полі
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
        }

        // колапс - з'єднання сусідніх однакових значень
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