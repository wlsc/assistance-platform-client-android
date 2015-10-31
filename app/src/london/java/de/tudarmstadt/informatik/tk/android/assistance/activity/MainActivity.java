package de.tudarmstadt.informatik.tk.android.assistance.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.ButterKnife;
import de.tudarmstadt.informatik.tk.android.assistance.R;
import de.tudarmstadt.informatik.tk.android.assistance.util.Constants;
import de.tudarmstadt.informatik.tk.android.assistance.util.UserUtils;
import de.tudarmstadt.informatik.tk.android.kraken.db.DbModule;
import de.tudarmstadt.informatik.tk.android.kraken.db.DbModuleCapability;
import de.tudarmstadt.informatik.tk.android.kraken.db.DbModuleInstallation;
import de.tudarmstadt.informatik.tk.android.kraken.provider.DbProvider;
import de.tudarmstadt.informatik.tk.android.kraken.provider.HarvesterServiceProvider;
import de.tudarmstadt.informatik.tk.android.kraken.provider.PreferenceProvider;
import de.tudarmstadt.informatik.tk.android.kraken.service.GcmRegistrationIntentService;
import de.tudarmstadt.informatik.tk.android.kraken.util.DateUtils;
import de.tudarmstadt.informatik.tk.android.kraken.util.GcmUtils;


/**
 * Module information dashboard
 *
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 28.06.2015
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private DbProvider dbProvider;

    private Toolbar mToolbar;

    private List<DbModuleInstallation> dbModuleInstallations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        boolean accessibilityServiceActivated = PreferenceProvider.getInstance(getApplicationContext()).getActivated();

        if (accessibilityServiceActivated) {
            initView();
        } else {

            Log.d(TAG, "Accessibility Service is NOT active! Showing tutorial...");

            Intent intent = new Intent(this, AccessibilityTutorialActivity.class);
            startActivityForResult(intent, Constants.INTENT_ACCESSIBILITY_SERVICE_IGNORED_RESULT);
        }
    }

    /**
     * Initializes this activity
     */
    private void initView() {

        if (dbProvider == null) {
            dbProvider = DbProvider.getInstance(getApplicationContext());
        }

        registerForPush();

        long userId = UserUtils.getCurrentUserId(getApplicationContext());

        Log.d(TAG, "UserId: " + userId);

        if (dbModuleInstallations == null) {

            Log.d(TAG, "dbModuleInstallations cached list IS empty");

            dbModuleInstallations = dbProvider.getModuleInstallationDao().getModuleInstallationsByUserId(userId);

            // user has got some active modules -> activate module menu
            if (dbModuleInstallations != null && !dbModuleInstallations.isEmpty()) {

                Log.d(TAG, "dbModuleInstallations found entries in db!");

                setContentView(R.layout.activity_main);
                setTitle(dbModuleInstallations.get(0).getDbModule().getTitle());

            } else {

                Log.d(TAG, "dbModuleInstallations NO data in db");

                // demo data
                installDemoData(userId);

                setContentView(R.layout.activity_main);
                setTitle(R.string.main_activity_title);
            }

        } else {

            Log.d(TAG, "dbModuleInstallations not empty");

            setContentView(R.layout.activity_main);
            setTitle(R.string.main_activity_title);
        }

        mToolbar = ButterKnife.findById(this, R.id.toolbar);
        setSupportActionBar(mToolbar);

        HarvesterServiceProvider.getInstance(getApplicationContext()).startSensingService();
    }

    /**
     * Installs demo data
     *
     * @param userId
     */
    private void installDemoData(long userId) {

        Log.d(TAG, "Installing example data...");

        String currentDate = DateUtils.dateToISO8601String(new Date(), Locale.getDefault());

        DbModule dbModule = new DbModule();

        dbModule.setUserId(userId);
        dbModule.setDescriptionShort("Short Description (255 chars)");
        dbModule.setDescriptionFull("Long (2048 chars)");
        dbModule.setLogoUrl("https://cdn1.iconfinder.com/data/icons/basic-ui-elements-round/700/09_location_pin-2-512.png");
        dbModule.setTitle("Quantified Self");
        dbModule.setPackageName("de.tudarmstadt.tk.assistance.quantifiedself");
        dbModule.setSupportEmail("developer@kraken.me");
        dbModule.setCopyright("TK Informtik TU Darmstadt");
        dbModule.setCreated(currentDate);

        long moduleId = dbProvider.getModuleDao().insertModule(dbModule);

        DbModuleCapability dbModuleCapability1 = new DbModuleCapability();

        dbModuleCapability1.setModuleId(moduleId);
        dbModuleCapability1.setType("position");
        dbModuleCapability1.setMinRequiredReadingsOnUpdate(1);
        dbModuleCapability1.setCollectionFrequency(0.2);
        dbModuleCapability1.setRequiredUpdateFrequency(2.0);
        dbModuleCapability1.setRequired(true);
        dbModuleCapability1.setCreated(currentDate);

        dbProvider.getModuleCapabilityDao().insertModuleCapability(dbModuleCapability1);

        DbModuleInstallation dbModuleInstallation = new DbModuleInstallation();

        dbModuleInstallation.setUserId(userId);
        dbModuleInstallation.setModuleId(moduleId);
        dbModuleInstallation.setActive(true);
        dbModuleInstallation.setCreated(currentDate);

        dbProvider.getModuleInstallationDao().insertModuleInstallation(dbModuleInstallation);

        dbModuleInstallations.add(dbModuleInstallation);

        Log.d(TAG, "Finished installing example data!");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // if we have no modules installed -> no menu will be visible
        if (dbModuleInstallations != null && !dbModuleInstallations.isEmpty()) {

            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.news_menu, menu);

            return true;
        }

        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Registers for GCM push notifications
     */
    private void registerForPush() {

        boolean isTokenWasSent = UserUtils.isGcmTokenWasSent(getApplicationContext());

        if (isTokenWasSent) {
            return;
        }

        // check for play services installation
        if (GcmUtils.isPlayServicesInstalled(this)) {

            Log.d(TAG, "Google Play Services are installed.");

            // starting registration GCM service
            Intent intent = new Intent(this, GcmRegistrationIntentService.class);
            startService(intent);

            UserUtils.saveGcmTokenWasSent(getApplicationContext(), true);

        } else {
            Log.d(TAG, "Google Play Services NOT installed.");

            UserUtils.saveGcmTokenWasSent(getApplicationContext(), false);
        }
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy -> unbound resources");
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {
            case Constants.INTENT_ACCESSIBILITY_SERVICE_IGNORED_RESULT:
                initView();
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
