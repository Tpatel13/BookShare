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
    public String body;
    public String imageUrl;
    public String publisher;
    public Map<String, Object> timestamp;

    public BookDetailModel() {
    }

    public BookDetailModel(String uid, String author, String publisher,
                           List<String> genre,
                           String name, String body,
                           String imageUrl) {
        this.uid = uid;
        this.author = author;
        this.publisher = publisher;
        this.genre = genre;
        this.name = name;
        this.body = body;
        this.imageUrl = imageUrl;

        //Date last changed will always be set to ServerValue.TIMESTAMP
        HashMap<String, Object> timestamp = new HashMap<String, Object>();
        timestamp.put("date", ServerValue.TIMESTAMP);

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
