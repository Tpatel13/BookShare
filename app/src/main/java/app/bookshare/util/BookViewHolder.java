package app.bookshare.util;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DatabaseReference;

import app.bookshare.BookDetailActivity;
import app.bookshare.MyBooksActivity;
import app.bookshare.R;
import app.bookshare.model.BookDetailModel;
import butterknife.BindView;
import butterknife.ButterKnife;

public class BookViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.tvBookName)
    TextView tvBookName;
    @BindView(R.id.tvAuthorName)
    TextView tvAuthorName;
    @BindView(R.id.tvCategory)
    TextView tvCategory;
    @BindView(R.id.tvOwnerName)
    TextView tvOwnerName;
    @BindView(R.id.card_view)
    CardView cardView;
    @BindView(R.id.ivBookCover)
    ImageView ivBookCover;

    public BookViewHolder(@NonNull View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public void bindToPost(final BookDetailModel bookDetailModel, final Context context, final DatabaseReference bookRef) {
        tvAuthorName.setText(bookDetailModel.author);
        tvBookName.setText(bookDetailModel.name);
        if (bookDetailModel.genre != null) {
            for (String genre :
                    bookDetailModel.genre) {
                tvCategory.setText(tvCategory.getText().toString() + genre + " ");
            }
        }


        tvOwnerName.setText(bookDetailModel.ownerName);

        Glide.with(context).load(bookDetailModel.imageUrl).into(ivBookCover);

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, BookDetailActivity.class);
                intent.putExtra(Common.KeyIntents.ARG_BOOK, bookDetailModel);
                if (context instanceof MyBooksActivity) {
                    intent.putExtra(Common.KeyIntents.ARG_BOOK_KEY, bookRef.getKey());
                }
                context.startActivity(intent);
            }
        });
    }
}
