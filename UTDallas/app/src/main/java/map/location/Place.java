package map.location;

import android.app.Fragment;
import android.content.Context;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.util.ArrayList;

import map.MapSearchFragment;

/**
 * Object to hold the information of each place
 */
public class Place {
    private String name, crowdStatus, nickname, description;
    private int timeWalk;
    private LatLng location;
    private boolean isHere, isSubPlace;
    private Marker mMarker;
    private ArrayList<Place> mSubPlaces;

    // Default constructor for Firebase
    public Place() {}

    public Place(String n, String nn, int tw, String cs, LatLng ll, boolean ih, String d, boolean subPlace) {
        name = n;
        timeWalk = tw;
        crowdStatus = cs;
        location = ll;
        nickname = nn;
        isHere = ih;
        description = d;
        mSubPlaces = new ArrayList<>();
        isSubPlace = subPlace;
    }

    public boolean isSubPlace() {
        return isSubPlace;
    }

    public void setMarkerIcon(int resID) {
        mMarker.setIcon(BitmapDescriptorFactory.fromResource(resID));
    }

    public void setSubPlacesVisible(boolean visible) {
        for(Place subPlace : mSubPlaces) {
            subPlace.setMarkerVisible(visible);
        }
    }

    public boolean isSubPlacesVisible() {
        for(Place subPlace : mSubPlaces) {
            return subPlace.getMarkerVisible();
        }

        return false;
    }

    public void setIsHere(boolean value) { isHere = value; }

    public void setDescription(String value) { description = value; }

    public boolean getIsHere() { return  isHere; }

    public String getNickname() { return nickname; }

    public LatLng getLocation() { return location; }

    public String getName() {
        return name;
    }

    public int getTimeWalk() {
        return timeWalk;
    }

    public String getCrowdStatus() {
        return crowdStatus;
    }

    public String getDescription() { return description; }

    public Marker getMarker() { return mMarker; }

    public void setMarker(Marker marker) { mMarker = marker; }

    public boolean getMarkerVisible() {
        return mMarker.isVisible();
    }

    public void setMarkerVisible(boolean visible) {
        mMarker.setVisible(visible);
    }

    public void setSubPlaces(ArrayList<Place> subPlaces) {
        mSubPlaces = subPlaces;
    }

    public ArrayList<Place> getSubPlaces() {
        return mSubPlaces;
    }
}
