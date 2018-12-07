package app.bookshare;

import android.Manifest;
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
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import app.bookshare.model.BookDetailModel;
import app.bookshare.model.UserBookAndGenreModel;
import app.bookshare.model.UserModel;
import app.bookshare.util.Common;
import app.bookshare.util.MultiSelectSpinner;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.engine.impl.GlideEngine;
import es.dmoral.toasty.Toasty;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

public class AddEditBookActivity extends BaseActivity implements MultiSelectSpinner.OnMultipleItemsSelectedListener {


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

    String CHANNEL_ID = "my_channel_01";// The id of the channel.

    private BookDetailModel mBookDetailModel;

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
        final String[] stringsGameCat = getResources().getStringArray(R.array.genre_array);
        spBookGenre.setItems(stringsGameCat);
        spBookGenre.setListener(this);
        if (intent.hasExtra(Common.KeyIntents.ARG_BOOK)) {
            mBookDetailModel = intent.getParcelableExtra(Common.KeyIntents.ARG_BOOK);
            etBoookName.setText(mBookDetailModel.name);
            etBookAuthor.setText(mBookDetailModel.author);
            etBookDescription.setText(mBookDetailModel.body);
            etBookPublisher.setText(mBookDetailModel.publisher);
            spBookGenre.setSelection(mBookDetailModel.genre);
            ivAddBookPhoto.setVisibility(View.VISIBLE);

            Glide.with(this).load(mBookDetailModel.getImageUrl()).into(ivAddBookPhoto);

        }

    }

    private void initNotification() {
        mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationBuilder = new NotificationCompat.Builder(this,
                CHANNEL_ID);
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

        if (mSelected.isEmpty() && !getIntent().hasExtra(Common.KeyIntents.ARG_BOOK_KEY)) {
            isValidate = false;
            Toast.makeText(this, "Please select image cover for book", Toast.LENGTH_SHORT).show();
        }

        if (spBookGenre.getSelectedStrings().isEmpty()) {
            isValidate = false;
            Toast.makeText(this, "Please select genre", Toast.LENGTH_SHORT).show();
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

            String imageUrl = "";

            if (mBookDetailModel != null) {
                imageUrl = mBookDetailModel.imageUrl;
            }

            BookDetailModel bookDetailModel = new BookDetailModel(user.getUid(),
                    bookAuthor, bookPublisher, selectedGenre, bookName, userName
                    , bookDescription, imageUrl, userModel.getEmail(), userModel.getPhoneNo(),
                    userModel.getProfileImage());

            String key = database.child("books").push().getKey();
            if (getIntent().hasExtra(Common.KeyIntents.ARG_BOOK_KEY)) {
                key = getIntent().getStringExtra(Common.KeyIntents.ARG_BOOK_KEY);
            }
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

            String CHANNEL_ID = "my_channel_01";// The id of the channel.
            CharSequence name = getString(R.string.main_notification_channel_name);// The user-visible name of the channel.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel mChannel = new NotificationChannel(
                        CHANNEL_ID,
                        name,
                        NotificationManager.IMPORTANCE_HIGH);
                mNotifyManager.createNotificationChannel(mChannel);
            }

            notificationBuilder.setContentTitle(getString(R.string.upload_video))
                    .setContentText(getString(R.string.upload_in_progress))
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(false)
                    .setSmallIcon(android.R.drawable.stat_sys_upload);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN
                    && Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                notificationBuilder.setPriority(NotificationCompat.PRIORITY_HIGH);
            }

            mNotifyManager.notify(notificationId, notificationBuilder.build());
            Toast loToast = Toast.makeText(this, R.string.upload_in_progress, Toast.LENGTH_LONG);
            loToast.setGravity(Gravity.CENTER, 0, 0);
            loToast.show();

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
                    notificationBuilder.setContentText((int) progress + "% ");
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
        AlertDialog.Builder builder = new AlertDialog.Builder(AddEditBookActivity.this);
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
            if (extras != null) {
                Bitmap imageBitmap = (Bitmap) extras.get("data");
                ivAddBookPhoto.setImageBitmap(imageBitmap);
                mSelected = new ArrayList<>();
                ivAddBookPhoto.setVisibility(View.VISIBLE);
            }
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

    @Override
    public void selectedIndices(List<Integer> indices) {

    }

    @Override
    public void selectedStrings(List<String> strings) {

    }
}
