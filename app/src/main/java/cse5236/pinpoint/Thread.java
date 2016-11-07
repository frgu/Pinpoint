package cse5236.pinpoint;

public class Thread {
    public String id;
    public String createdAt;
    public String updatedAt;
    public double lat;
    public double lng;

    public Thread() {

    }

    public Thread(String id, String createdAt, double lat, double lng) {
        this.id = id;
        this.createdAt = createdAt;
        this.updatedAt = createdAt;
        this.lat = lat;
        this.lng = lng;
    }
}
