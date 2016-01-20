package de.tudarmstadt.informatik.tk.assistance.adapter;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.common.collect.Lists;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;
import de.tudarmstadt.informatik.tk.assistance.R;
import de.tudarmstadt.informatik.tk.assistance.event.ShowGoogleMapEvent;
import de.tudarmstadt.informatik.tk.assistance.model.client.feedback.ContentFactory;
import de.tudarmstadt.informatik.tk.assistance.model.client.feedback.content.ClientFeedbackDto;
import de.tudarmstadt.informatik.tk.assistance.model.client.feedback.content.ContentDto;
import de.tudarmstadt.informatik.tk.assistance.model.client.feedback.content.enums.FeedbackItemType;
import de.tudarmstadt.informatik.tk.assistance.model.client.feedback.content.item.ButtonDto;
import de.tudarmstadt.informatik.tk.assistance.model.client.feedback.content.item.GroupDto;
import de.tudarmstadt.informatik.tk.assistance.model.client.feedback.content.item.ImageDto;
import de.tudarmstadt.informatik.tk.assistance.model.client.feedback.content.item.MapDto;
import de.tudarmstadt.informatik.tk.assistance.model.client.feedback.content.item.TextDto;
import de.tudarmstadt.informatik.tk.assistance.model.image.ScaledDownTransformation;
import de.tudarmstadt.informatik.tk.assistance.notification.Toaster;
import de.tudarmstadt.informatik.tk.assistance.sdk.event.OpenBrowserUrlEvent;
import de.tudarmstadt.informatik.tk.assistance.sdk.provider.ModuleProvider;
import de.tudarmstadt.informatik.tk.assistance.sdk.util.AppUtils;
import de.tudarmstadt.informatik.tk.assistance.sdk.util.UrlUtils;
import de.tudarmstadt.informatik.tk.assistance.sdk.util.logger.Log;
import de.tudarmstadt.informatik.tk.assistance.util.UiUtils;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 24.10.2015
 */
public class NewsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements
        OnMapReadyCallback, GoogleMap.OnMapClickListener {

    private static final String TAG = "NewsAdapter";

    public static final float GOOGLE_MAPS_ZOOM = 10.0f;

    public static final float CARD_MAX_HEIGHT_IN_DP = 200.0f;

    private static final int ICON_SETTINGS_MAX_WIDTH = 100;
    private static final int ICON_SETTINGS_MAX_HEIGHT = 70;

    private static final int EMPTY_VIEW_TYPE = 10;

    private final Context context;

    private final ModuleProvider moduleProvider;

    private final UiUtils uiUtils;

    private List<ClientFeedbackDto> data;

    private GoogleMap googleMap;
    private MapView mapView;
    private LatLng[] mapPoints;
    private boolean showUserMapLocation;

    public NewsAdapter(List<ClientFeedbackDto> data, Context context) {

        if (data == null) {
            this.data = Collections.emptyList();
        } else {
            this.data = data;
        }

        this.context = context;

        moduleProvider = ModuleProvider.getInstance(context);
        uiUtils = UiUtils.getInstance(context);
    }

    /**
     * Converts to LatLng array of location points
     *
     * @param mapPoints
     */
    protected void setMapPoints(Double[][] mapPoints) {

        if (mapPoints == null) {
            this.mapPoints = new LatLng[0];
        }

        List<LatLng> tmpPointsList = new ArrayList<>(mapPoints.length);

        for (Double[] point : mapPoints) {
            tmpPointsList.add(new LatLng(point[0], point[1]));
        }

        this.mapPoints = tmpPointsList.toArray(new LatLng[tmpPointsList.size()]);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view;

        if (viewType == EMPTY_VIEW_TYPE) {
            // list is empty
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_empty_list_view, parent, false);
            EmptyViewHolder emptyView = new EmptyViewHolder(view);

            return emptyView;
        }

        // list has items
        view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_news_card, parent, false);
        NewsViewHolder newsHolder = new NewsViewHolder(view);

        return newsHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        if (holder instanceof NewsViewHolder) {

            final NewsViewHolder viewHolder = (NewsViewHolder) holder;
            viewHolder.itemView.setHapticFeedbackEnabled(true);

            final ClientFeedbackDto newsCard = getItem(position);
            final ContentDto cardContent = newsCard.getContent();

            // TEST !!!!!!!
//            List<Double[]> tmp = new ArrayList<>();
//            tmp.add(new Double[]{49.8752582, 8.6693696});
//            cardContent.setPoints(tmp.toArray(new Double[tmp.size()][]));
//            cardContent.setTarget("https://www.google.com/");
//            cardContent.setSource("http://www.cooleparts-garage.de/wp-content/uploads/2014/05/imagee.jpeg");

            // GROUP
//            cardContent.setAlignment(GroupAlignment.VERTICAL.getValue());
//            List<ContentDto> content = new ArrayList<>();
//
//            TextDto textDto1 = new TextDto();
//            textDto1.setHighlighted(true);
//            textDto1.setCaption("test str");
//            textDto1.setAlignment(TextAlignment.LEFT.getValue());
//            TextDto textDto2 = new TextDto();
//            textDto2.setHighlighted(true);
//            textDto2.setCaption("test str2");
//            textDto2.setAlignment(TextAlignment.RIGHT.getValue());
//            content.add(textDto1);
//            content.add(textDto2);
//            ImageDto imageDto1 = new ImageDto();
//            imageDto1.setSource("http://www.cooleparts-garage.de/wp-content/uploads/2014/05/imagee.jpeg");
//            imageDto1.setTarget("https://www.google.com/");
//            content.add(imageDto1);
//            ButtonDto buttonDto1 = new ButtonDto();
//            buttonDto1.setCaption("Example button");
//            buttonDto1.setTarget("com.google.android.deskclock");
//            content.add(buttonDto1);
//
//            GroupDto groupDto1 = new GroupDto();
//            groupDto1.setAlignment(GroupAlignment.HORIZONTAL.getValue());
//            List<ContentDto> group1Content = new ArrayList<>();
//            TextDto group1TextDto1 = new TextDto();
//            group1TextDto1.setCaption("Text1");
//            group1TextDto1.setAlignment(TextAlignment.LEFT.getValue());
//            group1Content.add(group1TextDto1);
//            ImageDto group1imageDto1 = new ImageDto();
//            group1imageDto1.setSource("http://www.cooleparts-garage.de/wp-content/uploads/2014/05/imagee.jpeg");
//            group1imageDto1.setTarget("https://www.google.com/");
//            group1Content.add(group1imageDto1);
//            groupDto1.setContent(group1Content);
//            content.add(groupDto1);
//
//            cardContent.setContent(content);
            // !!!!!!!!!!!!

            showUserMapLocation = false;

            viewHolder.title.setText(moduleProvider.getModuleTitle(newsCard.getModuleId()));

            int size = (int) Math.ceil(Math.sqrt(ICON_SETTINGS_MAX_WIDTH * ICON_SETTINGS_MAX_HEIGHT));

            Picasso.with(context)
                    .load(R.drawable.ic_more_vert_black_48dp)
                    .placeholder(R.drawable.no_image)
                    .transform(new ScaledDownTransformation(
                            ICON_SETTINGS_MAX_WIDTH,
                            ICON_SETTINGS_MAX_HEIGHT))
                    .resize(size, size)
                    .centerInside()
                    .into(viewHolder.cardSettings);

            viewHolder.cardSettings.setOnClickListener(v -> {
                Log.d(TAG, "User selected more for " + newsCard.getModuleId() + " module");

                Toaster.showShort(v.getContext(), R.string.feature_is_under_construction);
            });

            ViewGroup.LayoutParams currentLayoutParams = viewHolder.itemView.getLayoutParams();

            FeedbackItemType feedbackType = FeedbackItemType.getEnum(cardContent.getType());

            // TEST
//            feedbackType = FeedbackItemType.GROUP;
            // ------------------------------

            switch (feedbackType) {
                case TEXT:
                    TextDto textDto = ContentFactory.getText(cardContent);

                    TextView textView = uiUtils.getText(textDto, textDto.getTarget() == null ? null : v -> {

                        boolean isValidUrl = UrlUtils.isValidUrl(textDto.getTarget());

                        if (isValidUrl) {
                            // we have valid URL -> fire open url event
                            if (EventBus.getDefault().hasSubscriberForEvent(OpenBrowserUrlEvent.class)) {
                                EventBus.getDefault().post(new OpenBrowserUrlEvent(textDto.getTarget()));
                            }
                        } else {
                            // we have app package name
                            AppUtils.openApp(context, textDto.getTarget());
                        }
                    });

                    if (textView == null) {
                        break;
                    }

                    viewHolder.mContainer.addView(textView);
                    break;

                case BUTTON:
                    ButtonDto buttonDto = ContentFactory.getButton(cardContent);

                    Button button = uiUtils.getButton(buttonDto, v -> {

                        boolean isValidUrl = UrlUtils.isValidUrl(buttonDto.getTarget());

                        if (isValidUrl) {
                            // we have valid URL -> fire open url event
                            if (EventBus.getDefault().hasSubscriberForEvent(OpenBrowserUrlEvent.class)) {
                                EventBus.getDefault().post(new OpenBrowserUrlEvent(buttonDto.getTarget()));
                            }
                        } else {
                            // we have app package name
                            AppUtils.openApp(context, buttonDto.getTarget());
                        }
                    });

                    if (button == null) {
                        break;
                    }

                    viewHolder.mContainer.addView(button);
                    break;

                case IMAGE:
                    ImageDto imageDto = ContentFactory.getImage(cardContent);

                    ImageView imageView = uiUtils.getImage(imageDto, (View.OnClickListener) v -> {

                        if (EventBus.getDefault().hasSubscriberForEvent(OpenBrowserUrlEvent.class)) {
                            EventBus.getDefault().post(new OpenBrowserUrlEvent(imageDto.getTarget()));
                        }
                    });

                    if (imageView == null) {
                        break;
                    }

                    viewHolder.mContainer.addView(imageView);

                    currentLayoutParams.height = (int) TypedValue
                            .applyDimension(TypedValue.COMPLEX_UNIT_DIP, CARD_MAX_HEIGHT_IN_DP, context.getResources().getDisplayMetrics());
                    viewHolder.itemView.setLayoutParams(currentLayoutParams);

                    break;

                case MAP:
                    MapDto mapDto = ContentFactory.getMap(cardContent);

                    // show user location om map
                    if (mapDto.getShowUserLocation() != null && mapDto.getShowUserLocation()) {
                        showUserMapLocation = true;
                    }

                    setMapPoints(mapDto.getPoints());
                    mapView = uiUtils.getMap(mapDto, this);

                    if (mapView == null) {
                        break;
                    }

                    viewHolder.mContainer.addView(mapView);

                    currentLayoutParams.height = (int) TypedValue
                            .applyDimension(TypedValue.COMPLEX_UNIT_DIP, CARD_MAX_HEIGHT_IN_DP, context.getResources().getDisplayMetrics());
                    viewHolder.itemView.setLayoutParams(currentLayoutParams);

                    break;

                case GROUP:
                    GroupDto groupDto = ContentFactory.getGroup(cardContent);

                    LinearLayout groupView = uiUtils.getGroup(groupDto);

                    if (groupView == null) {
                        break;
                    }

                    viewHolder.mContainer.addView(groupView);
                    break;
            }
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    @Nullable
    public ClientFeedbackDto getItem(int position) {

        if (position < 0 || position >= data.size()) {
            return null;
        }

        return data.get(position);
    }

    @Override
    public int getItemViewType(int position) {

        if (getItemCount() == 0) {
            return EMPTY_VIEW_TYPE;
        } else {
            return super.getItemViewType(position);
        }
    }

    /**
     * Swaps out old data with new data in the adapter
     *
     * @param newList
     */
    public void swapData(List<ClientFeedbackDto> newList) {

        if (newList == null) {
            data = Collections.emptyList();
        } else {
            data = Lists.newArrayList(newList);
        }

        notifyDataSetChanged();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        this.googleMap = googleMap;

        if (showUserMapLocation) {
            try {
                this.googleMap.setMyLocationEnabled(true);
            } catch (SecurityException sx) {
                Log.d(TAG, "SecurityException: user disabled location permission!");
            }
        }

        this.googleMap.setOnMapClickListener(this);

        MapsInitializer.initialize(mapView.getContext());
        this.googleMap.getUiSettings().setMapToolbarEnabled(false);

        if (mapPoints != null) {
            updateMapPoint();
        } else {
            Log.d(TAG, "Map points are null");
        }
    }

    /**
     * Updated points/markers on Google Map
     */
    private void updateMapPoint() {

        // first clear it
        googleMap.clear();

        for (LatLng point : mapPoints) {
            googleMap.addMarker(new MarkerOptions().position(point));
        }

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(mapPoints[0], GOOGLE_MAPS_ZOOM);
        googleMap.moveCamera(cameraUpdate);
    }

    @Override
    public void onMapClick(LatLng latLng) {
        EventBus.getDefault().post(new ShowGoogleMapEvent(latLng));
    }

    /**
     * An empty view holder if no items available
     */
    protected static class EmptyViewHolder extends RecyclerView.ViewHolder {
        public EmptyViewHolder(View view) {
            super(view);
        }
    }

    /**
     * View holder for available module
     */
    protected static class NewsViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.title)
        protected TextView title;

        @Bind(R.id.cardSettings)
        protected ImageView cardSettings;

        @Bind(R.id.newsContainer)
        protected LinearLayout mContainer;

        public NewsViewHolder(View view) {
            super(view);

            ButterKnife.bind(this, view);
        }
    }
}