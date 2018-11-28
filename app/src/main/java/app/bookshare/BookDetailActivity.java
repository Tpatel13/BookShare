package app.bookshare;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import app.bookshare.model.BookDetailModel;
import app.bookshare.util.Common;
import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class BookDetailActivity extends AppCompatActivity {

    @BindView(R.id.ivBookCover)
    ImageView ivBookCover;
    @BindView(R.id.tvBookName)
    TextView tvBookName;
    @BindView(R.id.tvGenre)
    TextView tvGenre;
    @BindView(R.id.tvBookDescription)
    TextView tvBookDescription;
    @BindView(R.id.tvBookAuthor)
    TextView tvBookAuthor;
    @BindView(R.id.profile_image)
    CircleImageView profileImage;
    @BindView(R.id.tvBookOwner)
    TextView tvBookOwner;
    @BindView(R.id.tvOwnerEmail)
    TextView tvOwnerEmail;
    @BindView(R.id.tvPhoneNo)
    TextView tvPhoneNo;
    @BindView(R.id.nsScroll)
    NestedScrollView nsScroll;
    @BindView(R.id.appbar)
    AppBarLayout appbar;
    @BindView(R.id.collapsingToolbarLayout)
    CollapsingToolbarLayout collapsingToolbarLayout;
    private BookDetailModel mBookDetailModel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_detail);
        ButterKnife.bind(this);
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (getIntent() != null && getIntent().hasExtra(Common.KeyIntents.ARG_BOOK)) {
            mBookDetailModel = getIntent().getParcelableExtra(Common.KeyIntents.ARG_BOOK);
        } else {
            finish();
        }

        setCoverImage();
        setBookName();
        setBookDescription();
        setBookGenre();
        setAuthor();
        setContactInfo();

        setNonScrollBehaviour();
        getSupportActionBar().setTitle("");
        appbar.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean isShow = true;
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                }
                if (scrollRange + verticalOffset == 0) {
                    collapsingToolbarLayout.setTitle(mBookDetailModel.name);
                    getSupportActionBar().setTitle(mBookDetailModel.name);
                    isShow = true;
                } else if (isShow) {
                    collapsingToolbarLayout.setTitle(" ");
                    getSupportActionBar().setTitle("");//carefull there should a space between double quote otherwise it wont work
                    isShow = false;
                }
            }
        });
    }

    private void setNonScrollBehaviour() {
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) appbar.getLayoutParams();
        if (params.getBehavior() == null) {
            params.setBehavior(new AppBarLayout.Behavior());
        }
        AppBarLayout.Behavior behavior = (AppBarLayout.Behavior) params.getBehavior();
        behavior.setDragCallback(new AppBarLayout.Behavior.DragCallback() {
            @Override
            public boolean canDrag(@NonNull AppBarLayout appBarLayout) {
                return false;
            }
        });
    }

    private void setContactInfo() {
        tvBookOwner.setText(mBookDetailModel.ownerName);
        tvOwnerEmail.setText(mBookDetailModel.ownerEmail);
        tvPhoneNo.setText(mBookDetailModel.ownerPhone);

        if (!TextUtils.isEmpty(mBookDetailModel.ownerProfileImageUrl)) {
            Glide.with(this)
                    .load(mBookDetailModel.ownerProfileImageUrl)
                    .into(profileImage);
        }
    }

    private void setAuthor() {
        tvBookAuthor.setText(mBookDetailModel.author);
    }

    private void setBookGenre() {
        StringBuilder sbGenre = new StringBuilder();
        for (int i = 0; i < mBookDetailModel.genre.size(); i++) {
            if (i == mBookDetailModel.genre.size() - 1) {
                sbGenre.append(mBookDetailModel.genre.get(i));
            } else {
                sbGenre.append(mBookDetailModel.genre.get(i)).append(", ");
            }
        }

        tvGenre.setText(sbGenre);
    }

    private void setCoverImage() {
        Glide.with(this).load(mBookDetailModel.getImageUrl()).into(ivBookCover);
    }

    private void setBookName() {
        tvBookName.setText(mBookDetailModel.name);
    }

    private void setBookDescription() {
        tvBookDescription.setText(mBookDetailModel.body);
    }
}
