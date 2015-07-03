package de.tu_darmstadt.tk.android.assistance.fragments.settings;

import android.app.Fragment;
import android.app.FragmentManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.tu_darmstadt.tk.android.assistance.R;
import de.tu_darmstadt.tk.android.assistance.activities.SettingsActivity;
import de.tu_darmstadt.tk.android.assistance.models.RoundImage;

/**
 * Created by Wladimir Schmidt on 29.06.2015.
 */
public class UserProfileSettingsFragment extends Fragment {

    @Bind(R.id.userPhoto)
    protected ImageView userPhoto;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ((SettingsActivity) getActivity()).getToolBar().setTitle(R.string.settings_header_user_profile_title);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_preference_user_profile, container, false);

        ButterKnife.bind(this, view);

        Bitmap userPhotoBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.no_user_pic);
        userPhoto.setImageDrawable(new RoundImage(userPhotoBitmap));

        return view;
    }

    @Override
    public void onDestroyView() {
        getFragmentManager().putFragment(getActivity().getIntent().getExtras(), "fuck", this);
        super.onDestroyView();
        ButterKnife.unbind(this);
    }
}
