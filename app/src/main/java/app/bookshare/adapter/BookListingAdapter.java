package app.bookshare.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import app.bookshare.R;
import app.bookshare.model.BookDetailModel;
import app.bookshare.util.BookViewHolder;

public class BookListingAdapter extends FirebaseRecyclerAdapter<BookDetailModel, BookViewHolder> {
    /**
     * Initialize a {@link RecyclerView.Adapter} that listens to a Firebase query. See
     * {@link FirebaseRecyclerOptions} for configuration options.
     *
     * @param options
     */

    private Context mContext;
    private ProgressBar pbBookList;

    public BookListingAdapter(@NonNull FirebaseRecyclerOptions<BookDetailModel> options,
                              Context context, ProgressBar pbBookList) {
        super(options);
        mContext = context;
        this.pbBookList = pbBookList;
    }

    @Override
    public void onDataChanged() {
        pbBookList.setVisibility(View.GONE);

        super.onDataChanged();
    }

    @Override
    protected void onBindViewHolder(@NonNull BookViewHolder holder, int position, @NonNull BookDetailModel model) {
        DatabaseReference bookRef = getRef(position);
        holder.bindToPost(model, mContext, bookRef);
    }

    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_book, parent, false);

        return new BookViewHolder(view);
    }

    @Override
    public void onError(@NonNull DatabaseError error) {
        Log.e("Adapter", error.getDetails());
        super.onError(error);
    }
}
