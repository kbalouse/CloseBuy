package fourpointoh.closebuy;

import java.util.ArrayList;

/**
 * Created by Kyle on 11/8/2015.
 */
public class Place {
    private String placeId;
    private String name;
    private String vicinity;
    private Double latitude;
    private Double longitude;
    private ArrayList<String> types = new ArrayList<>();
    private boolean openNow;

    public String getPlaceId() { return placeId; }
    public String getName() { return name; }
    public String getVicinity() { return vicinity; }
    public Double getLatitude() { return latitude; }
    public Double getLongitude() { return longitude; }
    public ArrayList<String> getTypes() { return types; }

    public void setPlaceId(String placeId) { this.placeId = placeId; }
    public void setName(String name) { this.name = name; }
    public void setVicinity(String vicinity) { this.vicinity = vicinity; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    public void setTypes(ArrayList<String> types) { this.types = types; }
    public void setIsOpen(boolean isOpen) { this.openNow = isOpen; }

    public boolean isOpenNow() { return openNow; }
}
