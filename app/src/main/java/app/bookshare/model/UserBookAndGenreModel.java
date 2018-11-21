package app.bookshare.model;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

public class UserBookAndGenreModel {
    public String bookKey;
    public boolean flgBook;

    public UserBookAndGenreModel() {
    }

    public UserBookAndGenreModel(String bookKey, boolean flgBook) {
        this.bookKey = bookKey;
        this.flgBook = flgBook;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put(bookKey, flgBook);
        return result;
    }
}
