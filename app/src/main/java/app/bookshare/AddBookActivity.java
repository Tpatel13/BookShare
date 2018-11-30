package app.bookshare;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.engine.impl.GlideEngine;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import app.bookshare.model.BookDetailModel;
import app.bookshare.model.UserBookAndGenreModel;
import app.bookshare.model.UserModel;
import app.bookshare.util.Common;
import app.bookshare.util.MultiSelectSpinner;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import es.dmoral.toasty.Toasty;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class AddBookActivity extends BaseActivity {

    public static final String EXTRA_CIRCULAR_REVEAL_X = "EXTRA_CIRCULAR_REVEAL_X";
    public static final String EXTRA_CIRCULAR_REVEAL_Y = "EXTRA_CIRCULAR_REVEAL_Y";

    private static final int RC_CAMERA_STORAGE = 103;
    private static final int REQUEST_CODE_CHOOSE = 102;
    private static final int REQUEST_CAMERA = 101;

    String mCurrentPhotoPath;
    Uri mCurrentPhotoUri;

    View rootLayout;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.ivAddBookPhoto)
    ImageView ivAddBookPhoto;
    @BindView(R.id.btnAddBookPhoto)
    Button btnAddBookPhoto;
    @BindView(R.id.etBoookName)
    EditText etBoookName;
    @BindView(R.id.etBookAuthor)
    EditText etBookAuthor;
    @BindView(R.id.etBookPublisher)
    EditText etBookPublisher;
    @BindView(R.id.spGenre)
    MultiSelectSpinner spBookGenre;
    @BindView(R.id.llRoot)
    LinearLayout llRoot;
    @BindView(R.id.etBookDescription)
    EditText etBookDescription;
    @BindView(R.id.tlBookName)
    TextInputLayout tlBookName;
    @BindView(R.id.tlBookAuthor)
    TextInputLayout tlBookAuthor;
    @BindView(R.id.tlBookPublisher)
    TextInputLayout tlBookPublisher;
    @BindView(R.id.tlBookDescription)
    TextInputLayout tlBookDescription;

    private int revealX;
    private int revealY;

    List<Uri> mSelected = new ArrayList<>();
    //Notification progress
    NotificationManager mNotifyManager;
    int notificationId = 1;
    private DatabaseReference database = FirebaseDatabase.getInstance().getReference();
    private FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
    private StorageReference storageRef;
    private StorageReference imageUploadRef;
    private NotificationCompat.Builder notificationBuilder;
    private String downloadImageUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_book);
        ButterKnife.bind(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final Intent intent = getIntent();

        initNotification();

        storageRef = firebaseStorage.getReferenceFromUrl(
                "gs://" + getResources().getString(R.string.google_storage_bucket));

        rootLayout = findViewById(R.id.root_layout);

        if (savedInstanceState == null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP &&
                intent.hasExtra(EXTRA_CIRCULAR_REVEAL_X) &&
                intent.hasExtra(EXTRA_CIRCULAR_REVEAL_Y)) {
            rootLayout.setVisibility(View.INVISIBLE);

            revealX = intent.getIntExtra(EXTRA_CIRCULAR_REVEAL_X, 0);
            revealY = intent.getIntExtra(EXTRA_CIRCULAR_REVEAL_Y, 0);


            ViewTreeObserver viewTreeObserver = rootLayout.getViewTreeObserver();
            if (viewTreeObserver.isAlive()) {
                viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        revealActivity(revealX, revealY);
                        rootLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                });
            }
        } else {
            rootLayout.setVisibility(View.VISIBLE);
        }

        setSpinner();
    }

    private void initNotification() {
        mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationBuilder = new NotificationCompat.Builder(this,
                "Upload Progress");
    }

    private void setSpinner() {
        final String[] selected = {"Book Genre"};
        spBookGenre.setItemsChecked(selected);
    }

    protected void revealActivity(int x, int y) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            float finalRadius = (float) (Math.max(rootLayout.getWidth(), rootLayout.getHeight()) * 1.1);

            // create the animator for this view (the start radius is zero)
            Animator circularReveal = ViewAnimationUtils.createCircularReveal(rootLayout, x, y, 0, finalRadius);
            circularReveal.setDuration(400);
            circularReveal.setInterpolator(new AccelerateInterpolator());
            // make the view visible and start the animation
            rootLayout.setVisibility(View.VISIBLE);
            circularReveal.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animator) {
                    rootLayout.setBackgroundColor(ContextCompat.getColor(AddBookActivity.this,
                            R.color.colorAccent));
                    llRoot.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    rootLayout.setBackgroundColor(ContextCompat.getColor(AddBookActivity.this,
                            android.R.color.background_light));
                    llRoot.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationCancel(Animator animator) {

                }

                @Override
                public void onAnimationRepeat(Animator animator) {

                }
            });
            circularReveal.start();

        } else {
            finish();
        }
    }

    protected void unRevealActivity() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            finish();
        } else {
            float finalRadius = (float) (Math.max(rootLayout.getWidth(), rootLayout.getHeight()) * 1.1);
            Animator circularReveal = ViewAnimationUtils.createCircularReveal(
                    rootLayout, revealX, revealY, finalRadius, 0);

            circularReveal.setDuration(400);
            circularReveal.addListener(new AnimatorListenerAdapter() {

                @Override
                public void onAnimationStart(Animator animation) {
                    rootLayout.setBackgroundColor(ContextCompat.getColor(AddBookActivity.this,
                            R.color.colorAccent));
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    rootLayout.setVisibility(View.INVISIBLE);
                    finish();
                }
            });


            circularReveal.start();
        }
    }

    @Override
    public void onBackPressed() {
        unRevealActivity();
        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_add_book_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.action_done) {
            if (validate()) {
                readUserFromDatabase();
                Toasty.success(this, getString(R.string.messge_thank_you), Toast.LENGTH_SHORT, true).show();
                finish();
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void readUserFromDatabase() {
        if (getUid() != null) {
            database.child("users").child(getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    UserModel userModel = dataSnapshot.getValue(UserModel.class);
                    putBookIntoDatabase(userModel);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    private boolean validate() {
        boolean isValidate;

        isValidate = Common.checkEmpty(etBookAuthor, tlBookAuthor,
                getString(R.string.error_author_name))
                && Common.checkEmpty(etBoookName, tlBookName, getString(R.string.error_book_name))
                && Common.checkEmpty(etBookPublisher, tlBookPublisher, getString(R.string.error_publisher_name))
                && Common.checkEmpty(etBookDescription, tlBookDescription, "Please enter description");

        if (mSelected.isEmpty()) {
            isValidate = false;
            Toast.makeText(this, "Please select image cover for book", Toast.LENGTH_SHORT).show();
        }
        return isValidate;
    }

    private void putBookIntoDatabase(UserModel userModel) {


        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {

            FirebaseUser user = auth.getCurrentUser();
            String bookAuthor = etBookAuthor.getText().toString().trim();
            String bookPublisher = etBookPublisher.getText().toString().trim();
            List<String> selectedGenre = spBookGenre.getSelectedStrings();
            String bookName = etBoookName.getText().toString().trim();
            String bookDescription = etBookDescription.getText().toString().trim();

            String userName = userModel.getFirst_name() + " " + userModel.getLast_name();

            BookDetailModel bookDetailModel = new BookDetailModel(user.getUid(),
                    bookAuthor, bookPublisher, selectedGenre, bookName, userName
                    , bookDescription, "", userModel.getEmail(), userModel.getPhoneNo(),
                    userModel.getProfileImage());

            String key = database.child("books").push().getKey();
            Map<String, Object> bookValues = bookDetailModel.toMap();

            Map<String, Object> childUpdates = new HashMap<>();
            if (key != null) {
                childUpdates.put("/books/" + key, bookValues);
                for (String genre : selectedGenre) {
                    UserBookAndGenreModel userBookAndGenreModel = new UserBookAndGenreModel(key, true);
                    childUpdates.put("/genres/" + "/" + genre + "/" + key, true);
                }
                UserBookAndGenreModel userBookAndGenreModel = new UserBookAndGenreModel(key, true);
                childUpdates.put("/users-books/" + "/" + user.getUid() + "/" + key, true);
                database.updateChildren(childUpdates);
                uploadImage(key);
            }
        }
    }

    private void uploadImage(final String key) {
        Long time = System.currentTimeMillis();
        if (!mSelected.isEmpty() && getUid() != null) {
            imageUploadRef = storageRef.child(getUid()).child("images")
                    .child(time.toString()).child(mSelected.get(0).getLastPathSegment());

            int notifyID = 1;
            String CHANNEL_ID = "my_channel_01";// The id of the channel.
            CharSequence name = "Upload Image";// The user-visible name of the channel.
            int importance = 0;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                importance = NotificationManager.IMPORTANCE_HIGH;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
                    mNotifyManager.createNotificationChannel(mChannel);
                }
            }

            notificationBuilder.setContentTitle(getString(R.string.upload_video))
                    .setContentText(getString(R.string.upload_in_progress))
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(false)
                    .setSmallIcon(android.R.drawable.stat_sys_upload);

            mNotifyManager.notify(notificationId, notificationBuilder.build());

            Toast.makeText(this, R.string.upload_in_progress, Toast.LENGTH_LONG).show();

            final UploadTask uploadTaskImage = imageUploadRef.putFile(mSelected.get(0));
            Task<Uri> urlTask = uploadTaskImage.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    // Continue with the task to get the download URL
                    return imageUploadRef.getDownloadUrl();
                }

            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        downloadImageUrl = task.getResult().toString();
                        Map<String, Object> childUpdates = new HashMap<>();
                        childUpdates.put("/books/" + key + "/imageUrl", downloadImageUrl);
                        database.updateChildren(childUpdates);
                        notificationBuilder.setContentText(getString(R.string.upload_complete));
                        // Removes the progress bar
                        notificationBuilder.setProgress(0, 0, false);
                        notificationBuilder.setSmallIcon(android.R.drawable.stat_sys_upload_done);
                        mNotifyManager.notify(notificationId, notificationBuilder.build());
                        notificationBuilder.setAutoCancel(true);
                    } else {
                        // Handle failures
                        // ...
                    }
                }
            });
            uploadTaskImage.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                    notificationBuilder.setProgress(100, (int) progress, false);
                    mNotifyManager.notify(notificationId, notificationBuilder.build());
                }
            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @OnClick(R.id.btnAddBookPhoto)
    @AfterPermissionGranted(RC_CAMERA_STORAGE)
    public void addBookPhoto() {
        String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA};

        if (EasyPermissions.hasPermissions(this, permissions)) {
            selectImage();
        } else {
            EasyPermissions.requestPermissions(this, getString(R.string.camera_and_storage_rationale),
                    RC_CAMERA_STORAGE, permissions);
        }
    }

    public void selectImage() {
        final CharSequence[] items = {"Take Photo", "Choose from Library",
                "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(AddBookActivity.this);
        builder.setTitle("Add Photo!");
        builder.setCancelable(true);
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals("Take Photo")) {
                    cameraIntent();
                } else if (items[item].equals("Choose from Library")) {
                    openGallery();
                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    private void cameraIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                mCurrentPhotoUri = FileProvider.getUriForFile(this,
                        "app.bookshare.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mCurrentPhotoUri);
                startActivityForResult(takePictureIntent, REQUEST_CAMERA);
            }
        }
    }

    private void openGallery() {
        Matisse.from(this)
                .choose(MimeType.ofImage())
                .countable(true)
                .maxSelectable(1)
                .gridExpectedSize(getResources().getDimensionPixelSize(R.dimen.grid_expected_size))
                .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                .thumbnailScale(0.85f)
                .imageEngine(new GlideEngine())
                .forResult(REQUEST_CODE_CHOOSE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_CHOOSE && resultCode == RESULT_OK) {
            mSelected = Matisse.obtainResult(data);
            if (!mSelected.isEmpty()) {
                Glide.with(this).load(mSelected.get(0)).into(ivAddBookPhoto);
                ivAddBookPhoto.setVisibility(View.VISIBLE);
            }
            Log.d("Matisse", "mSelected: " + mSelected);
        } else if (requestCode == REQUEST_CAMERA && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            ivAddBookPhoto.setImageBitmap(imageBitmap);
            mSelected = new ArrayList<>();
            ivAddBookPhoto.setVisibility(View.VISIBLE);
            mSelected.add(mCurrentPhotoUri);
        }
    }


    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }
}
