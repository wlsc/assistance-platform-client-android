package de.tu_darmstadt.tk.android.assistance.views;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;
import de.tu_darmstadt.tk.android.assistance.R;
import de.tu_darmstadt.tk.android.assistance.events.ModuleInstallEvent;
import de.tu_darmstadt.tk.android.assistance.events.ModuleShowMoreInfoEvent;
import it.gmariotti.cardslib.library.internal.Card;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 19.07.2015
 */
public class CardView extends Card {

    private static final String TAG = CardView.class.getSimpleName();

    private String moduleId;

    @Bind(R.id.more_info_module)
    protected Button moreInfo;

    @Bind(R.id.install_module)
    protected Button install;

    public CardView(Context context) {
        super(context, R.layout.card_row);
        this.mContext = context;
    }

    public CardView(Context context, int innerLayout) {
        super(context, innerLayout);
        this.mContext = context;
    }

    @Override
    public void setupInnerViewElements(ViewGroup parent, View view) {
        ButterKnife.bind(this, view);
    }

    @OnClick(R.id.install_module)
    void onInstallClicked() {
        Log.d(TAG, "Module installation button clicked");

        // propagate event to activity
        EventBus.getDefault().post(new ModuleInstallEvent(getModuleId()));
    }

    @OnClick(R.id.more_info_module)
    void onMoreInfoClicked() {
        Log.d(TAG, "More information about a module clicked");

        // propagate event to activity
        EventBus.getDefault().post(new ModuleShowMoreInfoEvent(getModuleId()));
    }

    public String getModuleId() {
        return moduleId;
    }

    public void setModuleId(String moduleId) {
        this.moduleId = moduleId;
    }
}
