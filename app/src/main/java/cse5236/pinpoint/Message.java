package cse5236.pinpoint;

public class Message {
    public String id;
    public String userId;
    public String userName;
    public String content;
    public String createdAt;

    public Message() {

    }

    public Message(String id, String userId, String userName, String content, String createdAt) {
        this.id = id;
        this.userId = userId;
        this.userName = userName;
        this.content = content;
        this.createdAt = createdAt;
    }
}
