package de.tudarmstadt.informatik.tk.android.assistance.presenter;

import android.content.Context;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 01.12.2015
 */
public class CommonPresenterImpl implements CommonPresenter {

    protected String TAG;

    private final Context context;

    public CommonPresenterImpl(Context context) {
        this.context = context;
    }

    @Override
    public void setLogTag(String tag) {
        this.TAG = tag;
    }

    @Override
    public Context getContext() {
        return this.context;
    }
}
