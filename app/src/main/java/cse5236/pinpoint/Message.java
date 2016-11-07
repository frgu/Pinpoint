package cse5236.pinpoint;

public class Message {
    public String id;
    public String userId;
    public String content;
    public String createdAt;

    public Message() {

    }

    public Message(String id, String userId, String content, String createdAt) {
        this.id = id;
        this.userId = userId;
        this.content = content;
        this.createdAt = createdAt;
    }
}
