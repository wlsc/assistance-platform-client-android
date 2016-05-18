package de.tudarmstadt.informatik.tk.assistance.util;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.greenrobot.event.EventBus;
import de.tudarmstadt.informatik.tk.assistance.Config;
import de.tudarmstadt.informatik.tk.assistance.R;
import de.tudarmstadt.informatik.tk.assistance.model.client.feedback.ContentFactory;
import de.tudarmstadt.informatik.tk.assistance.model.client.feedback.content.ContentDto;
import de.tudarmstadt.informatik.tk.assistance.model.client.feedback.content.enums.FeedbackItemType;
import de.tudarmstadt.informatik.tk.assistance.model.client.feedback.content.enums.GroupAlignment;
import de.tudarmstadt.informatik.tk.assistance.model.client.feedback.content.enums.TextAlignment;
import de.tudarmstadt.informatik.tk.assistance.model.client.feedback.content.item.ButtonDto;
import de.tudarmstadt.informatik.tk.assistance.model.client.feedback.content.item.GroupDto;
import de.tudarmstadt.informatik.tk.assistance.model.client.feedback.content.item.ImageDto;
import de.tudarmstadt.informatik.tk.assistance.model.client.feedback.content.item.MapDto;
import de.tudarmstadt.informatik.tk.assistance.model.client.feedback.content.item.TextDto;
import de.tudarmstadt.informatik.tk.assistance.sdk.event.OpenBrowserUrlEvent;
import de.tudarmstadt.informatik.tk.assistance.sdk.util.AppUtils;
import de.tudarmstadt.informatik.tk.assistance.sdk.util.StringUtils;
import de.tudarmstadt.informatik.tk.assistance.sdk.util.UrlUtils;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 11.01.2016
 */
public final class UiUtils {

    private static UiUtils INSTANCE;

    private final Context context;

    private UiUtils(Context context) {
        this.context = context;
    }

    public static UiUtils getInstance(Context context) {

        if (INSTANCE == null) {
            INSTANCE = new UiUtils(context);
        }

        return INSTANCE;
    }

    /**
     * Returns TextView with according settings from feedback text DTO type
     *
     * @param textDto
     * @param onClickListener
     * @return
     */
    @Nullable
    public TextView getText(TextDto textDto, View.OnClickListener onClickListener) {

        if (textDto == null) {
            return null;
        }

        String caption = textDto.getCaption();
        String alignment = textDto.getAlignment();
        Boolean isHighlighted = textDto.getHighlighted();
        String[] style = textDto.getStyle();
        String target = textDto.getTarget();

        TextView textView = new TextView(context);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.MATCH_PARENT);

        textView.setText(StringUtils.isNullOrEmpty(caption) ? "" : caption);

        if (style != null) {
            // TODO: do something with style of text
        }

        /*
         *  HIGHLIGHT
         */
        if (isHighlighted != null && isHighlighted) {
            textView.setTextColor(Color.RED);
        } else {
            textView.setTextColor(Color.BLACK);
        }

        /*
         *  ALIGNMENT
         */
        if (StringUtils.isNotNullAndEmpty(alignment)) {

            if (TextAlignment.LEFT.getValue().equalsIgnoreCase(alignment)) {

                params.weight = 1.0f;
                params.gravity = Gravity.LEFT;
                params.gravity = Gravity.START;
            }

            if (TextAlignment.CENTER.getValue().equalsIgnoreCase(alignment)) {

                params.weight = 1.0f;
                params.gravity = Gravity.CENTER;
            }

            if (TextAlignment.RIGHT.getValue().equalsIgnoreCase(alignment)) {

                params.weight = 1.0f;
                params.gravity = Gravity.RIGHT;
                params.gravity = Gravity.END;
            }
        } else {
            // DEFAULT VALUES

            params.weight = 1.0f;
            params.gravity = Gravity.LEFT;
            params.gravity = Gravity.START;
        }

        textView.setLayoutParams(params);

        /**
         * TARGET
         */
        if (onClickListener != null && target != null) {
            textView.setOnClickListener(onClickListener);
        }

        return textView;
    }

    /**
     * Returns ImageView with according settings from feedback image DTO type
     *
     * @param imageDto
     * @param onClickListener
     * @return
     */
    @Nullable
    public ImageView getImage(ImageDto imageDto, View.OnClickListener onClickListener) {

        if (imageDto == null) {
            return null;
        }

        ImageView imageView = new ImageView(context);

        String source = imageDto.getSource();

        if (source != null) {

            Picasso.with(context)
                    .load(source)
                    .placeholder(R.drawable.no_image)
                    .into(imageView);
        }

        String target = imageDto.getTarget();

        // only when we have a target
        if (target != null && onClickListener != null) {

            boolean isValidUrl = UrlUtils.isValidUrl(target);

            if (isValidUrl) {

                // bind only when valid URL
                imageView.setOnClickListener(onClickListener);
            }
        }

        return imageView;
    }

    /**
     * Returns Button with according settings from feedback button DTO type
     *
     * @param buttonDto
     * @param onClickListener
     * @return
     */
    @Nullable
    public Button getButton(ButtonDto buttonDto, View.OnClickListener onClickListener) {

        if (buttonDto == null) {
            return null;
        }

        Button button = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            button = new Button(context, null, 0, R.style.BtnDefault);
        } else {
            button = new Button(context, null, 0);
        }

        String caption = buttonDto.getCaption();

        button.setText(StringUtils.isNullOrEmpty(caption) ? "" : caption);

        /**
         * TARGET
         */
        String target = buttonDto.getTarget();

        if (target != null && onClickListener != null) {
            button.setOnClickListener(onClickListener);
        }

        return button;
    }

    @Nullable
    public MapView getMap(MapDto mapDto, OnMapReadyCallback callback) {

        if (mapDto == null) {
            return null;
        }

        MapView mapView = new MapView(context);

        mapView.onCreate(null);
        mapView.getMapAsync(callback);

        return mapView;
    }

    @Nullable
    public LinearLayout getGroup(GroupDto groupDto) {

        if (groupDto == null) {
            return null;
        }

        String alignment = groupDto.getAlignment();

        LinearLayout linearLayout = new LinearLayout(context);

        /**
         * ALIGNMENT
         */
        if (StringUtils.isNotNullAndEmpty(alignment)) {

            if (GroupAlignment.HORIZONTAL.getValue().equalsIgnoreCase(alignment)) {
                linearLayout.setOrientation(LinearLayout.HORIZONTAL);
            }

            if (GroupAlignment.VERTICAL.getValue().equalsIgnoreCase(alignment)) {
                linearLayout.setOrientation(LinearLayout.VERTICAL);
            }

        } else {
            // DEFAULT VALUES

            linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        }

        /**
         * CONTENT PROCESSING
         */
        List<ContentDto> moreContent = groupDto.getContent();

        if (moreContent != null && !moreContent.isEmpty()) {
            View contentLayout = generateMoreContent(moreContent, alignment);

            if (contentLayout != null) {
                linearLayout.addView(contentLayout);
            }
        }

        return linearLayout;
    }

    /**
     * Generates more content for a group
     *
     * @param moreContent
     * @param alignment
     * @return
     */
    private View generateMoreContent(List<ContentDto> moreContent, String alignment) {

        LinearLayout linearLayout = new LinearLayout(context);

        if (StringUtils.isNotNullAndEmpty(alignment)) {

            if (GroupAlignment.HORIZONTAL.getValue().equalsIgnoreCase(alignment)) {
                linearLayout.setOrientation(LinearLayout.HORIZONTAL);
            }

            if (GroupAlignment.VERTICAL.getValue().equalsIgnoreCase(alignment)) {
                linearLayout.setOrientation(LinearLayout.VERTICAL);
            }
        } else {
            // DEFAULT VALUES
            linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        }

        for (ContentDto content : moreContent) {

            FeedbackItemType feedbackType = FeedbackItemType.getEnum(content.getType());

            switch (feedbackType) {
                case TEXT:
                    TextDto textDto = ContentFactory.getText(content);
                    TextView textView = getText(textDto, textDto.getTarget() == null ? null : v -> {

                        boolean isValidUrl = UrlUtils.isValidUrl(textDto.getTarget());

                        if (isValidUrl) {
                            // we have valid URL -> fire open url event
                            if (EventBus.getDefault().hasSubscriberForEvent(OpenBrowserUrlEvent.class)) {
                                EventBus.getDefault().post(new OpenBrowserUrlEvent(textDto.getTarget()));
                            }
                        }
                    });

                    if (textView == null) {
                        break;
                    }

                    textView.setPadding(0,
                            getDpAsPixels(Config.FEEDBACK_CARD_PADDING),
                            0,
                            getDpAsPixels(Config.FEEDBACK_CARD_PADDING));

                    linearLayout.addView(textView);

                    break;

                case BUTTON:
                    ButtonDto buttonDto = ContentFactory.getButton(content);

                    Button button = getButton(buttonDto, v -> {

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

                    button.setPadding(0,
                            getDpAsPixels(Config.FEEDBACK_CARD_PADDING),
                            0,
                            getDpAsPixels(Config.FEEDBACK_CARD_PADDING));

                    linearLayout.addView(button);

                    break;

                case IMAGE:
                    ImageDto imageDto = ContentFactory.getImage(content);

                    ImageView imageView = getImage(imageDto, v -> {

                        if (EventBus.getDefault().hasSubscriberForEvent(OpenBrowserUrlEvent.class)) {
                            EventBus.getDefault().post(new OpenBrowserUrlEvent(imageDto.getTarget()));
                        }
                    });

                    if (imageView == null) {
                        break;
                    }

                    imageView.setPadding(0,
                            getDpAsPixels(Config.FEEDBACK_CARD_PADDING),
                            0,
                            getDpAsPixels(Config.FEEDBACK_CARD_PADDING));

                    linearLayout.addView(imageView);

                    break;

                case MAP:
                    MapDto mapDto = ContentFactory.getMap(content);

                    // show user location om map
                    if (mapDto.getShowUserLocation() != null && mapDto.getShowUserLocation()) {
//                        showUserMapLocation = true;
                    }

//                    setMapPoints(mapDto.getPoints());
                    MapView mapView = getMap(mapDto, null);

                    if (mapView == null) {
                        break;
                    }

                    mapView.setPadding(0,
                            getDpAsPixels(Config.FEEDBACK_CARD_PADDING),
                            0,
                            getDpAsPixels(Config.FEEDBACK_CARD_PADDING));

                    linearLayout.addView(mapView);

                    break;

                case GROUP:
                    linearLayout.addView(generateMoreContent(content.getContent(), content.getAlignment()));
                    break;
            }
        }

        return linearLayout;
    }

    /**
     * Converts dp value to pixel value
     *
     * @param dpValue
     * @return
     */
    public int getDpAsPixels(int dpValue) {

        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
}