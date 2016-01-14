package de.tudarmstadt.informatik.tk.android.assistance.event;

import com.google.android.gms.maps.model.LatLng;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 14.01.2016
 */
public class ShowGoogleMapEvent {

    private LatLng latLng;

    public ShowGoogleMapEvent(LatLng latLng) {
        this.latLng = latLng;
    }

    public LatLng getLatLng() {
        return this.latLng;
    }

    @Override
    public String toString() {
        return "ShowGoogleMapEvent{" +
                "latLng=" + latLng +
                '}';
    }
}