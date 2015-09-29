package de.tudarmstadt.informatik.tk.android.assistance.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.ButterKnife;
import de.tudarmstadt.informatik.tk.android.assistance.R;
import de.tudarmstadt.informatik.tk.android.assistance.activity.common.DrawerActivity;
import de.tudarmstadt.informatik.tk.android.assistance.model.item.DrawerItem;
import de.tudarmstadt.informatik.tk.android.assistance.util.UserUtils;
import de.tudarmstadt.informatik.tk.android.kraken.db.DatabaseManager;
import de.tudarmstadt.informatik.tk.android.kraken.db.DbModule;
import de.tudarmstadt.informatik.tk.android.kraken.db.DbModuleCapability;
import de.tudarmstadt.informatik.tk.android.kraken.db.DbModuleCapabilityDao;
import de.tudarmstadt.informatik.tk.android.kraken.db.DbModuleDao;
import de.tudarmstadt.informatik.tk.android.kraken.db.DbModuleInstallation;
import de.tudarmstadt.informatik.tk.android.kraken.db.DbModuleInstallationDao;
import de.tudarmstadt.informatik.tk.android.kraken.service.KrakenServiceManager;
import de.tudarmstadt.informatik.tk.android.kraken.util.DateUtils;


/**
 * Module information dashboard
 *
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 28.06.2015
 */
public class MainActivity extends DrawerActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private Menu menu;

    private DbModuleDao moduleDao;

    private DbModuleCapabilityDao moduleCapabilityDao;

    private DbModuleInstallationDao moduleInstallationDao;

    private List<DbModuleInstallation> dbModuleInstallations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        startSensingService();

        long userId = UserUtils.getCurrentUserId(getApplicationContext());

        Log.d(TAG, "UserId: " + userId);

        if (moduleDao == null) {
            moduleDao = DatabaseManager.getInstance(getApplicationContext()).getDaoSession().getDbModuleDao();
        }

        if (moduleCapabilityDao == null) {
            moduleCapabilityDao = DatabaseManager.getInstance(getApplicationContext()).getDaoSession().getDbModuleCapabilityDao();
        }

        if (moduleInstallationDao == null) {
            moduleInstallationDao = DatabaseManager.getInstance(getApplicationContext()).getDaoSession().getDbModuleInstallationDao();
        }

        if (dbModuleInstallations == null) {

            Log.d(TAG, "dbModuleInstallations cached list IS empty");

            dbModuleInstallations = moduleInstallationDao
                    .queryBuilder()
                    .where(DbModuleInstallationDao.Properties.UserId.eq(userId))
                    .build()
                    .list();

            // user has got some active modules -> activate module menu
            if (dbModuleInstallations != null && !dbModuleInstallations.isEmpty()) {

                Log.d(TAG, "dbModuleInstallations found entries in db!");

                getLayoutInflater().inflate(R.layout.activity_main, mFrameLayout);
                setTitle(dbModuleInstallations.get(0).getDbModule().getTitle());

                mDrawerFragment.updateDrawerBody(getApplicationContext());

            } else {

                Log.d(TAG, "dbModuleInstallations NO data in db");

                // demo data
                installDemoData(userId);

                getLayoutInflater().inflate(R.layout.activity_main, mFrameLayout);
                setTitle(R.string.main_activity_title);

                mDrawerFragment.updateDrawerBody(getApplicationContext());
            }

        } else {

            Log.d(TAG, "dbModuleInstallations not empty");

            getLayoutInflater().inflate(R.layout.activity_main, mFrameLayout);
            setTitle(R.string.main_activity_title);

            mDrawerFragment.updateDrawerBody(getApplicationContext());
        }
    }

    /**
     * Installs demo data
     *
     * @param userId
     */
    private void installDemoData(long userId) {

        Log.d(TAG, "Installing example data...");

        DbModule dbModule = new DbModule();

        dbModule.setUserId(userId);
        dbModule.setDescriptionShort("Short Description (255 chars)");
        dbModule.setDescriptionFull("Long (2048 chars)");
        dbModule.setLogoUrl("https://cdn1.iconfinder.com/data/icons/basic-ui-elements-round/700/09_location_pin-2-512.png");
        dbModule.setTitle("Quantified Self");
        dbModule.setPackageName("de.tudarmstadt.tk.assistance.quantifiedself");
        dbModule.setSupportEmail("developer@kraken.me");
        dbModule.setCopyright("TK Informtik TU Darmstadt");
        dbModule.setCreated(DateUtils.dateToISO8601String(new Date(), Locale.getDefault()));

        long moduleId = moduleDao.insertOrReplace(dbModule);

        DbModuleCapability dbModuleCapability1 = new DbModuleCapability();

        dbModuleCapability1.setModuleId(moduleId);
        dbModuleCapability1.setType("position");
        dbModuleCapability1.setMinRequiredReadingsOnUpdate(1);
        dbModuleCapability1.setCollectionFrequency(0.2);
        dbModuleCapability1.setRequiredUpdateFrequency(2.0);
        dbModuleCapability1.setRequired(true);
        dbModuleCapability1.setCreated(DateUtils.dateToISO8601String(new Date(), Locale.getDefault()));

        moduleCapabilityDao.insertOrReplace(dbModuleCapability1);

        DbModuleInstallation dbModuleInstallation = new DbModuleInstallation();

        dbModuleInstallation.setUserId(userId);
        dbModuleInstallation.setModuleId(moduleId);
        dbModuleInstallation.setActive(true);
        dbModuleInstallation.setCreated(DateUtils.dateToISO8601String(new Date(), Locale.getDefault()));

        moduleInstallationDao.insertOrReplace(dbModuleInstallation);

        dbModuleInstallations.add(dbModuleInstallation);

        Log.d(TAG, "Finished installing example data!");
    }

    /**
     * Releases the Kraken.
     */
    private void startSensingService() {

        KrakenServiceManager service = KrakenServiceManager.getInstance(getApplicationContext());
        service.startKrakenService();
    }

    /**
     * Calms down the Kraken.
     */
    private void stopSensingService() {

        KrakenServiceManager service = KrakenServiceManager.getInstance(getApplicationContext());
        service.stopKrakenService();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // if we have no modules installed -> no menu will be visible
        if (dbModuleInstallations != null && !dbModuleInstallations.isEmpty()) {

            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.module_menu, menu);

            this.menu = menu;

            return true;
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menu_module_toggle_state:

                return true;

            case R.id.menu_module_uninstall:

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Gives current selected module by user via navigation drawer
     *
     * @return
     */
    private DbModuleInstallation getCurrentActiveModuleFromDrawer() {

        DrawerItem item = mDrawerFragment.getNavigationItems().get(mDrawerFragment.getCurrentSelectedPosition());
        return item.getModule();
    }

    @Override
    protected void onDestroy() {
        ButterKnife.unbind(this);

        moduleDao = null;
        moduleInstallationDao = null;

        Log.d(TAG, "onDestroy -> unbound resources");
        super.onDestroy();
    }

}
