package de.tu_darmstadt.tk.android.assistance.activities;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.tu_darmstadt.tk.android.assistance.R;
import de.tu_darmstadt.tk.android.assistance.activities.common.DrawerActivity;
import de.tu_darmstadt.tk.android.assistance.utils.Constants;
import de.tu_darmstadt.tk.android.assistance.utils.Toaster;


/**
 * Main user's place
 */
public class MainActivity extends DrawerActivity {

    private String TAG = MainActivity.class.getSimpleName();

    @Bind(R.id.toolbar_actionbar)
    protected Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View contentView = inflater.inflate(R.layout.activity_main, null, false);

        drawerLayout.addView(contentView, 0);

        ButterKnife.bind(this, contentView);

        setSupportActionBar(mToolbar);
        setTitle(R.string.main_activity_title);

        setupDrawer(mToolbar);
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // drawer item was select

    }

    @Override
    public void onBackPressed() {

        if (mBackButtonPressedOnce) {
            super.onBackPressed();
            return;
        }

        mBackButtonPressedOnce = true;

        Toaster.showLong(this, R.string.action_back_button_pressed_once);

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                mBackButtonPressedOnce = false;
            }
        }, Constants.BACK_BUTTON_DELAY_MILLIS);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(this);
        Log.d(TAG, "onDestroy -> unbound resources");
    }

    public Toolbar getToolbar() {
        return mToolbar;
    }
}
