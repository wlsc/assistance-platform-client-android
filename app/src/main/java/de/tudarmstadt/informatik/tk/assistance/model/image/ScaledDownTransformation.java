package de.tudarmstadt.informatik.tk.assistance.model.image;

import android.graphics.Bitmap;

import com.squareup.picasso.Transformation;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 14.01.2016
 */
public class ScaledDownTransformation implements Transformation {

    private int maxWidth;
    private int maxHeight;

    public ScaledDownTransformation(int maxWidth, int maxHeight) {
        this.maxWidth = maxWidth;
        this.maxHeight = maxHeight;
    }

    @Override
    public Bitmap transform(Bitmap source) {

        double targetScalingFactor;
        int targetWidth, targetHeight;

        if (source.getWidth() > source.getHeight()) {

            targetWidth = maxWidth;
            targetScalingFactor = (double) source.getHeight() / (double) source.getWidth();
            targetHeight = (int) (targetWidth * targetScalingFactor);

        } else {

            targetHeight = maxHeight;
            targetScalingFactor = (double) source.getWidth() / (double) source.getHeight();
            targetWidth = (int) (targetHeight * targetScalingFactor);
        }

        final Bitmap scaledBitmap = Bitmap.createScaledBitmap(source, targetWidth, targetHeight, false);

        if (scaledBitmap != source) {
            source.recycle();
        }

        return scaledBitmap;
    }

    @Override
    public String key() {
        return this.maxWidth + "x" + this.maxHeight;
    }
}
