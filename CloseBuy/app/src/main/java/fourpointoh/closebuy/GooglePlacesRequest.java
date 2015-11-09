package fourpointoh.closebuy;

import java.util.ArrayList;

/**
 * Created by Kyle on 11/8/2015.
 */
public class GooglePlacesRequest {
    private Double latitude;
    private Double longitude;
    private Double radius;
    private ArrayList<String> types = new ArrayList<>();

    public Double getLatitude() { return latitude; }
    public Double getLongitude() { return longitude; }
    public Double getRadius() { return radius; }
    public ArrayList<String> getTypes() { return types; }

    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    public void setRadius(Double radius) { this.radius = radius; }
    public void addType(String type) { this.types.add(type); }
    public void clearTypes() { this.types.clear(); }
}
