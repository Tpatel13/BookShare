package app.bookshare.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerOptions;

import app.bookshare.R;
import app.bookshare.adapter.BookListingAdapter;
import app.bookshare.model.BookDetailModel;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;


/**
 * A simple {@link Fragment} subclass.
 */
public abstract class BookListFragment extends Fragment {


    @BindView(R.id.rvBookList)
    RecyclerView rvBookList;
    @BindView(R.id.pbBookList)
    ProgressBar pbBookList;
    Unbinder unbinder;
    @BindView(R.id.tv_empty_recyclerView)
    TextView tvEmptyRecyclerView;


    public BookListFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_book_list, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;

    }

    @Override
    public void onStart() {
        super.onStart();
        initAdapter();
    }

    private void initAdapter() {
        rvBookList.setVisibility(View.VISIBLE);
        BookListingAdapter bookListingAdapter = new BookListingAdapter(getOptions(),
                getActivity(), pbBookList);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);
        rvBookList.setLayoutManager(linearLayoutManager);

        bookListingAdapter.startListening();
        rvBookList.setAdapter(bookListingAdapter);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (rvBookList.getAdapter() instanceof BookListingAdapter) {
            ((BookListingAdapter) rvBookList.getAdapter()).stopListening();
        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    abstract FirebaseRecyclerOptions<BookDetailModel> getOptions();
}
