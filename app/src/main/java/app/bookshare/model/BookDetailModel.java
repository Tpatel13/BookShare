package app.bookshare.model;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.ServerValue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class BookDetailModel {
    public String uid;
    public String author;
    public List<String> genre;
    public String name;
    public String ownerName;
    public String body;
    public String imageUrl;
    public String publisher;
    public Map<String, Object> timestamp;

    public BookDetailModel() {
    }

    public BookDetailModel(String uid, String author, String publisher,
                           List<String> genre,
                           String name, String ownerName, String body,
                           String imageUrl) {
        this.uid = uid;
        this.author = author;
        this.publisher = publisher;
        this.genre = genre;
        this.name = name;
        this.body = body;
        this.imageUrl = imageUrl;
        this.ownerName = ownerName;
        //Date last changed will always be set to ServerValue.TIMESTAMP
        HashMap<String, Object> timestamp = new HashMap<String, Object>();
        timestamp.put("date", ServerValue.TIMESTAMP);

        this.timestamp = timestamp;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public List<String> getGenre() {
        return genre;
    }

    public void setGenre(List<String> genre) {
        this.genre = genre;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public Map<String, Object> getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Map<String, Object> timestamp) {
        this.timestamp = timestamp;
    }

    public BookDetailModel(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("uid", uid);
        result.put("author", author);
        result.put("publisher", publisher);
        result.put("genre", genre);
        result.put("name", name);
        result.put("ownerName", ownerName);
        result.put("body", body);
        result.put("imageUrl", imageUrl);
        result.put("timestamp", timestamp);
        return result;
    }

    @Exclude
    public Map<String, Object> toDownloadUrlMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("imageUrl", imageUrl);
        return result;
    }
}
