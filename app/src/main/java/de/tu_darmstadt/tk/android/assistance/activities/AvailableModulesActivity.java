package de.tu_darmstadt.tk.android.assistance.activities;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.Toolbar;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.tu_darmstadt.tk.android.assistance.R;
import de.tu_darmstadt.tk.android.assistance.activities.common.BaseActivity;
import de.tu_darmstadt.tk.android.assistance.callbacks.NavigationDrawerCallbacks;
import de.tu_darmstadt.tk.android.assistance.fragments.NavigationDrawerFragment;
import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardArrayAdapter;
import it.gmariotti.cardslib.library.internal.CardHeader;
import it.gmariotti.cardslib.library.internal.CardThumbnail;
import it.gmariotti.cardslib.library.view.CardListView;

public class AvailableModulesActivity extends BaseActivity implements NavigationDrawerCallbacks {

    @Bind(R.id.module_list)
    protected CardListView mModuleList;

    @Bind(R.id.module_list_swipe_refresh_layout)
    protected SwipeRefreshLayout mSwipeRefreshLayout;

    private NavigationDrawerFragment mNavigationDrawerFragment;
    private Toolbar mToolbar;
    private String mUserEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_available_modules);
        setTitle(R.string.module_list_activity_title);

        ButterKnife.bind(this);

        String userEmail = getUserEmail();

        mToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        setSupportActionBar(mToolbar);

        DrawerLayout drawerLayout = ButterKnife.findById(this, R.id.drawer);

        mNavigationDrawerFragment = (NavigationDrawerFragment) getFragmentManager().findFragmentById(R.id.fragment_drawer);

        mNavigationDrawerFragment.setup(R.id.fragment_drawer, drawerLayout, mToolbar);
        mNavigationDrawerFragment.setUserData("Wladimir Schmidt", userEmail, BitmapFactory.decodeResource(getResources(), R.drawable.no_user_pic));

        mSwipeRefreshLayout.setColorSchemeResources(
                R.color.swipe_refresh_progress_1,
                R.color.swipe_refresh_progress_2,
                R.color.swipe_refresh_progress_3);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });

        int listImages[] = new int[]{R.drawable.no_user_pic, R.drawable.no_user_pic};

        ArrayList<Card> cards = new ArrayList<Card>();

        for (int i = 0; i < 2; i++) {
            Card card = new Card(this);

            CardHeader header = new CardHeader(this);

            header.setTitle("Module " + i);
            card.setTitle("Sample module title");
            card.addCardHeader(header);

            CardThumbnail thumb = new CardThumbnail(this);
            thumb.setDrawableResource(listImages[i]);
            card.addCardThumbnail(thumb);

            cards.add(card);
        }

        CardArrayAdapter mCardArrayAdapter = new CardArrayAdapter(this, cards);

        if (mModuleList != null) {
            mModuleList.setAdapter(mCardArrayAdapter);
        }

    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // drawer item was select
    }

    @Override
    protected void onStop() {
        super.onStop();
        ButterKnife.unbind(this);
    }
}
