package cse5236.pinpoint;

public class Thread {
    public String id;
    public String createdAt;
    public String updatedAt;
    public String subject;
    public String userId;
    public String address;
    public double lat;
    public double lng;

    public Thread() {

    }

    public Thread(String id, String createdAt, String subject, String userId, String address, double lat, double lng) {
        this.id = id;
        this.createdAt = createdAt;
        this.updatedAt = createdAt;
        this.subject = subject;
        this.userId = userId;
        this.address = address;
        this.lat = lat;
        this.lng = lng;
    }
}
