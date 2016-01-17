package de.tudarmstadt.informatik.tk.assistance.util;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.squareup.picasso.Picasso;

import de.tudarmstadt.informatik.tk.assistance.R;
import de.tudarmstadt.informatik.tk.assistance.sdk.util.StringUtils;
import de.tudarmstadt.informatik.tk.assistance.model.client.feedback.content.enums.GroupAlignment;
import de.tudarmstadt.informatik.tk.assistance.model.client.feedback.content.enums.TextAlignment;
import de.tudarmstadt.informatik.tk.assistance.model.client.feedback.content.item.ButtonDto;
import de.tudarmstadt.informatik.tk.assistance.model.client.feedback.content.item.GroupDto;
import de.tudarmstadt.informatik.tk.assistance.model.client.feedback.content.item.ImageDto;
import de.tudarmstadt.informatik.tk.assistance.model.client.feedback.content.item.MapDto;
import de.tudarmstadt.informatik.tk.assistance.model.client.feedback.content.item.TextDto;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 11.01.2016
 */
public class UiUtils {

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
     * @return
     */
    @Nullable
    public TextView getText(TextDto textDto) {

        if (textDto == null) {
            return null;
        }

        String caption = textDto.getCaption();
        String alignment = textDto.getAlignment();
        Boolean isHighlighted = textDto.getHighlighted();
        String[] style = textDto.getStyle();

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

        return textView;
    }

    /**
     * Returns ImageView with according settings from feedback image DTO type
     *
     * @param imageDto
     * @return
     */
    @Nullable
    public ImageView getImage(ImageDto imageDto) {

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

        return imageView;
    }

    /**
     * Returns Button with according settings from feedback button DTO type
     *
     * @param buttonDto
     * @return
     */
    @Nullable
    public Button getButton(ButtonDto buttonDto) {

        if (buttonDto == null) {
            return null;
        }

        Button button = new Button(context);

        String caption = buttonDto.getCaption();

        button.setText(StringUtils.isNullOrEmpty(caption) ? "" : caption);

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

        return linearLayout;
    }
}