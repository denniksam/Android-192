package step.learning.basics;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;

// детектор свайпів
public class OnSwipeListener implements View.OnTouchListener {
    private final GestureDetector gestureDetector ;

    public void onSwipeLeft()   { }   // методи для перевантаження
    public void onSwipeRight()  { }   // у інших класах (для вжитку)
    public void onSwipeTop()    { }   // Даний клас має запускати ці
    public void onSwipeBottom() { }   // методи за відповідними жестами

    public OnSwipeListener( Context context ) {
        this.gestureDetector = new GestureDetector( context, new GestureListener() ) ;
    }
    @Override
    public boolean onTouch( View view, MotionEvent motionEvent ) {
        return this.gestureDetector.onTouchEvent( motionEvent ) ;
    }

    private final class GestureListener extends GestureDetector.SimpleOnGestureListener {
        private final static float MIN_SWIPE_DISTANCE = 100 ;     // мінімальна довжина руху, яка вважатиметься свайпом
        private final static float MIN_SWIPE_VELOCITY = 100 ;     // мінімальна швидкість, повільніші рухи ігноруватимуться
        @Override
        public boolean onFling( @NonNull MotionEvent e1,   // подія проведення по екрану
                                @NonNull MotionEvent e2,   // е1 - точка початку проведення,
                                float velocityX,           // е2 - точка кінця
                                float velocityY ) {        // + швидкості руху по координатах

            float dx = e2.getX() - e1.getX() ;  // відстань по Х проведення (руху по екрану)
            float dy = e2.getY() - e1.getY() ;
            // чутливі екрани часто сприймають дотик як флінг (на невелику відстань)
            // для більш певного детектування свайпів слід задати обмеження по відстані і швидкості рухів
            boolean result = false ;   // не будь-який флінг буде свайпом

            if( Math.abs( dx ) > Math.abs( dy ) ) {   // зміщення по горизонталі більше - горизонтальний свайп
                if( Math.abs( dx ) >= MIN_SWIPE_DISTANCE
                 && Math.abs( velocityX ) >= MIN_SWIPE_VELOCITY ) {
                    if( dx < 0 ) {   // координата початку більша за коорд. кінця - рух ліворуч
                        onSwipeLeft() ;
                    }
                    else {
                        onSwipeRight() ;
                    }
                    result = true ;
                }
            }
            else {  // вертикальний свайп
                if( Math.abs( dy ) >= MIN_SWIPE_DISTANCE
                 && Math.abs( velocityY ) >= MIN_SWIPE_VELOCITY ) {
                    if( dy < 0 ) {
                        onSwipeTop() ;
                    }
                    else {
                        onSwipeBottom() ;
                    }
                    result = true ;
                }
            }
            return result ;
        }

        @Override
        public boolean onDown( @NonNull MotionEvent e ) {
            return true ;   // ознака того, що наш обробник закінчив цю подію
        }
    }
}
