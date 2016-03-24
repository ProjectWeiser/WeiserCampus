package map;

import com.google.android.gms.maps.model.LatLng;

/**
 * Object to hold the information of each place
 */
public class MapSearchListItemData {
    private String name, crowdStatus, nickname, description;
    private int timeWalk;
    private LatLng location;
    private boolean isHere;

    public MapSearchListItemData(String n, String nn, int tw, String cs, LatLng ll, boolean ih, String d) {
        name = n;
        timeWalk = tw;
        crowdStatus = cs;
        location = ll;
        nickname = nn;
        isHere = ih;
        description = d;
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
}
