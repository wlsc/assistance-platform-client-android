package de.tu_darmstadt.tk.android.assistance.activities;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.tu_darmstadt.tk.android.assistance.R;
import de.tu_darmstadt.tk.android.assistance.activities.common.BaseActivity;
import de.tu_darmstadt.tk.android.assistance.callbacks.NavigationDrawerCallbacks;
import de.tu_darmstadt.tk.android.assistance.fragments.NavigationDrawerFragment;
import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardArrayAdapter;
import it.gmariotti.cardslib.library.internal.CardHeader;
import it.gmariotti.cardslib.library.internal.CardThumbnail;
import it.gmariotti.cardslib.library.view.CardListView;

public class ModuleListActivity extends BaseActivity implements NavigationDrawerCallbacks {

    @InjectView(R.id.module_list)
    protected CardListView listView;

    @InjectView(R.id.module_list_swipe_refresh_layout)
    protected SwipeRefreshLayout swipeRefreshLayout;

    private NavigationDrawerFragment mNavigationDrawerFragment;
    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_module_list);

        ButterKnife.inject(this);

        mToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        setSupportActionBar(mToolbar);

        mNavigationDrawerFragment = (NavigationDrawerFragment) getFragmentManager().findFragmentById(R.id.fragment_drawer);

        mNavigationDrawerFragment.setup(R.id.fragment_drawer, (DrawerLayout) findViewById(R.id.drawer), mToolbar);
        mNavigationDrawerFragment.setUserData("Wladimir Schmidt", "wlsc.dev@gmail.com", BitmapFactory.decodeResource(getResources(), R.drawable.avatar));

        swipeRefreshLayout.setColorSchemeResources(
                R.color.swipe_refresh_progress_1,
                R.color.swipe_refresh_progress_2,
                R.color.swipe_refresh_progress_3);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        int listImages[] = new int[]{R.drawable.avatar, R.drawable.avatar,
                R.drawable.avatar, R.drawable.avatar, R.drawable.avatar};

        ArrayList<Card> cards = new ArrayList<Card>();

        for (int i = 0; i < 2; i++) {
            // Create a Card
            Card card = new Card(this);
            // Create a CardHeader
            CardHeader header = new CardHeader(this);
            // Add Header to card
            header.setTitle("Angry bird: " + i);
            card.setTitle("sample title");
            card.addCardHeader(header);

            CardThumbnail thumb = new CardThumbnail(this);
            thumb.setDrawableResource(listImages[i]);
            card.addCardThumbnail(thumb);

            cards.add(card);
        }

        CardArrayAdapter mCardArrayAdapter = new CardArrayAdapter(this, cards);

        if (listView != null) {
            listView.setAdapter(mCardArrayAdapter);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_module_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();


        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // drawer item was select
    }
}
