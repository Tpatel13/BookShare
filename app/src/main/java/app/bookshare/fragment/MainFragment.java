package app.bookshare.fragment;


import android.support.v4.app.Fragment;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import app.bookshare.model.BookDetailModel;

/**
 * A simple {@link Fragment} subclass.
 */
public class MainFragment extends BookListFragment {


    public MainFragment() {
        // Required empty public constructor
    }

    @Override
    FirebaseRecyclerOptions<BookDetailModel> getOptions() {
        Query query = FirebaseDatabase.getInstance()
                .getReference()
                .child("books")
                .limitToLast(50);

        return new FirebaseRecyclerOptions.Builder<BookDetailModel>()
                .setQuery(query, BookDetailModel.class)
                .build();
    }


}
