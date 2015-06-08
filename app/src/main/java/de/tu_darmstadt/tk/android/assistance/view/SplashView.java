package de.tu_darmstadt.tk.android.assistance.view;

import android.content.Context;
import android.graphics.Canvas;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import de.tu_darmstadt.tk.android.assistance.R;

/**
 * Created by Wladimir Schmidt (wlsc.dev@gmail.com) on 06.06.2015.
 */
public class SplashView extends LinearLayout {

    private SplashScreenEvent splashScreenEvent = null;

    public SplashView(Context context) {
        super(context);

        setWillNotDraw(true);
        setDrawingCacheEnabled(false);
        setGravity(Gravity.CENTER);

        LayoutInflater.from(context).inflate(R.layout.splashscreen, this, true);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);

        fireSplashDrawCompleteEvent();
    }

    private void fireSplashDrawCompleteEvent() {
        if (this.splashScreenEvent != null) {
            this.splashScreenEvent.onSplashDrawComplete();
        }
    }

    public void setSplashScreenEvent(SplashScreenEvent event) {
        splashScreenEvent = event;
    }

    public interface SplashScreenEvent {
        void onSplashDrawComplete();
    }
}
