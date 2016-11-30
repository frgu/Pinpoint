package cse5236.pinpoint;

/**
 * Created by freddygu on 11/7/16.
 */

public class ThreadIndex {

    public double lat;
    public double lng;
    public String id;
    public String userId;

    public ThreadIndex() {

    }

    public ThreadIndex(double lat, double lng, String id, String userId) {
        this.lat = lat;
        this.lng = lng;
        this.id = id;
        this.userId = userId;
    }
}
