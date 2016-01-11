package de.tudarmstadt.informatik.tk.android.assistance.util;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

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
     * Returns TextView with according settings from feedback text type
     *
     * @param textDto
     * @return
     */
    public TextView getText(TextDto textDto) {

        if (textDto == null) {
            return null;
        }

        TextView textView = new TextView(context);

        textView.setText(textDto.getCaption());
        textView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        if (textDto.getHighlighted()) {
            textView.setTextColor(Color.RED);
        } else {
            textView.setTextColor(Color.BLACK);
        }

        if (textDto.getAlignment().equalsIgnoreCase("CENTER")) {

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.MATCH_PARENT);

            params.weight = 1.0f;
            params.gravity = Gravity.CENTER;

            textView.setLayoutParams(params);
        }

        return textView;
    }
}