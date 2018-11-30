package app.bookshare.fragment;


import android.support.v4.app.Fragment;
import app.bookshare.model.BookDetailModel;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

/**
 * A simple {@link Fragment} subclass.
 */
public class MyBooksFragment extends BookListFragment {


    public MyBooksFragment() {
        // Required empty public constructor
    }

    @Override
    FirebaseRecyclerOptions<BookDetailModel> getOptions() {
        DatabaseReference bookRef = FirebaseDatabase.getInstance()
                .getReference()
                .child("books");
        FirebaseAuth auth = FirebaseAuth.getInstance();

        String uid = "";
        if (auth != null) {
            uid = auth.getUid();
        }

        Query keyQuery = FirebaseDatabase.getInstance()
                .getReference().child("users-books").child(uid);

        return new FirebaseRecyclerOptions.Builder<BookDetailModel>()
                .setIndexedQuery(keyQuery, bookRef, BookDetailModel.class)
                .build();
    }


}
