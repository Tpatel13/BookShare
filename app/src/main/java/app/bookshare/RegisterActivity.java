package app.bookshare;

import android.Manifest;
import android.app.Activity;
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
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
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

import app.bookshare.model.UserModel;
import app.bookshare.util.Common;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class RegisterActivity extends AppCompatActivity {

    private static final int REQUEST_CAMERA = 101;
    private static final int RC_CAMERA_STORAGE = 103;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.login_progress)
    ProgressBar loginProgress;
    @BindView(R.id.profile_image)
    CircleImageView profileImage;
    @BindView(R.id.iv_camera)
    CircleImageView ivCamera;
    @BindView(R.id.etFirstName)
    EditText etFirstName;
    @BindView(R.id.etLastName)
    EditText etLastName;
    @BindView(R.id.etEmail)
    EditText etEmail;
    @BindView(R.id.password)
    EditText password;
    @BindView(R.id.etPhoneNumber)
    EditText etPhoneNumber;
    @BindView(R.id.btnWithText)
    Button btnWithText;
    @BindView(R.id.email_login_form)
    LinearLayout emailLoginForm;
    @BindView(R.id.login_form)
    NestedScrollView loginForm;

    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    @BindView(R.id.etAddress)
    EditText etAddress;
    private static final int REQUEST_CODE_CHOOSE = 102;
    final CharSequence[] items = {"Take Photo", "Choose from Library",
            "Cancel"};
    List<Uri> mSelected = new ArrayList<>();

    String mCurrentPhotoPath;
    Uri mCurrentPhotoUri;
    final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
    @BindView(R.id.tlFirstName)
    TextInputLayout tlFirstName;
    @BindView(R.id.tlLastName)
    TextInputLayout tlLastName;
    @BindView(R.id.tlEmail)
    TextInputLayout tlEmail;
    @BindView(R.id.tlPassword)
    TextInputLayout tlPassword;
    @BindView(R.id.tlPhoneNo)
    TextInputLayout tlPhoneNo;
    @BindView(R.id.tlAddress)
    TextInputLayout tlAddress;
    @BindView(R.id.pbRegister)
    ProgressBar pbRegister;
    //Notification progress
    NotificationManager mNotifyManager;
    int notificationId = 2;
    String CHANNEL_ID = "my_channel_01";// The id of the channel.
    private StorageReference storageRef;
    private StorageReference imageUploadRef;
    private FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
    private NotificationCompat.Builder notificationBuilder;
    private String downloadImageUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        ButterKnife.bind(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        storageRef = firebaseStorage.getReferenceFromUrl(
                "gs://" + getResources().getString(R.string.google_storage_bucket));

        initNotification();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @OnClick(R.id.btnWithText)
    public void startMainActivity() {
        if (validate()) {
            btnWithText.setEnabled(false);
            pbRegister.setVisibility(View.VISIBLE);
            mAuth.createUserWithEmailAndPassword(etEmail.getText().toString().trim(), password.getText().toString())
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            pbRegister.setVisibility(View.GONE);
                            btnWithText.setEnabled(true);
                            if (task.isSuccessful()) {
                                FirebaseUser user = mAuth.getCurrentUser();
                                if (user != null) {
                                    updateUI(user);
                                }
                            } else {
                                if (task.getException() != null) {
                                    Toasty.error(RegisterActivity.this,
                                            task.getException().getMessage()).show();
                                } else {
                                    Toasty.error(RegisterActivity.this,
                                            "Please try again after sometime").show();
                                }

                            }
                        }
                    });

        }
    }

    private boolean validate() {


        if (!Common.checkEmpty(etFirstName, tlFirstName, getString(R.string.error_first_name))) {
            return false;
        }

        if (!Common.checkEmpty(etLastName, tlLastName, getString(R.string.error_first_name))) {
            return false;
        }

        if (!Common.checkEmpty(etEmail, tlEmail, getString(R.string.error_email))) {
            return false;
        } else if (!isEmailValid(etEmail.getText().toString())) {
            tlEmail.setError(getString(R.string.error_invalid_email));
            tlEmail.requestFocus();
            return false;
        } else {
            tlEmail.setError(null);
        }

        if (!Common.checkEmpty(password, tlPassword, "Please enter password")) {
            return false;
        }


        if (!Common.checkEmpty(etPhoneNumber, tlPhoneNo, getString(R.string.error_phone_number))) {
            return false;
        } else if (!Patterns.PHONE.matcher(etPhoneNumber.getText().toString().trim()).matches()) {
            tlPhoneNo.setError(getString(R.string.error_phone));
            tlPhoneNo.requestFocus();
            return false;
        } else {
            tlPhoneNo.setError(null);
        }

        return true;
    }

    private boolean isEmailValid(String email) {
        return email.contains("@");
    }

    private void updateUI(FirebaseUser user) {
        UserModel userModel = new UserModel();
        userModel.setEmail(user.getEmail());
        userModel.setFirst_name(etFirstName.getText().toString().trim());
        userModel.setLast_name(etLastName.getText().toString().trim());
        userModel.setUid(user.getUid());
        userModel.setAddress(etAddress.getText().toString().trim());
        userModel.setPhoneNo(etPhoneNumber.getText().toString().trim());
        putDataToUsers(userModel);
//        startActivity(new Intent(this, MainActivity.class));
    }

    public void putDataToUsers(final UserModel user) {


        databaseReference.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.hasChild(user.getUid())) {
                    Map<String, Object> userValues = user.toMap();

                    Map<String, Object> childUpdates = new HashMap<>();
                    childUpdates.put("/users/" + user.getUid(), userValues);

                    databaseReference.updateChildren(childUpdates);
                    uploadImage(user);
                    startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                    finishAffinity();
                } else {
                    Toast.makeText(RegisterActivity.this,
                            "User is already registered, please sign in", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    @OnClick(R.id.iv_camera)
    @AfterPermissionGranted(RC_CAMERA_STORAGE)
    public void selectImage() {

        String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA};

        if (EasyPermissions.hasPermissions(this, permissions)) {
            showDialog();
        } else {
            EasyPermissions.requestPermissions(this, getString(R.string.camera_and_storage_rationale),
                    RC_CAMERA_STORAGE, permissions);
        }


    }

    private void showDialog() {
        final CharSequence[] items = {"Take Photo", "Choose from Library",
                "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
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
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CODE_CHOOSE) {
                mSelected = Matisse.obtainResult(data);
                if (!mSelected.isEmpty()) {
                    Glide.with(this).load(mSelected.get(0)).into(profileImage);
                    profileImage.setVisibility(View.VISIBLE);
                }
            } else if (requestCode == REQUEST_CAMERA) {
                Bundle extras = data.getExtras();
                Bitmap imageBitmap = (Bitmap) extras.get("data");
                profileImage.setImageBitmap(imageBitmap);
                mSelected = new ArrayList<>();
                mSelected.add(mCurrentPhotoUri);
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
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

    private void initNotification() {
        mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationBuilder = new NotificationCompat.Builder(this,
                CHANNEL_ID);
    }

    private void uploadImage(final UserModel user) {
        Long time = System.currentTimeMillis();
        if (!mSelected.isEmpty() && user.getUid() != null) {
            imageUploadRef = storageRef.child(user.getUid()).child("profile_images")
                    .child(time.toString()).child(mSelected.get(0).getLastPathSegment());

            CharSequence name = getString(R.string.main_notification_channel_name);// The user-visible name of the channel.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel mChannel = new NotificationChannel(
                        CHANNEL_ID,
                        name,
                        NotificationManager.IMPORTANCE_LOW);
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
                        childUpdates.put("/users/" + user.getUid() + "/profileImage", downloadImageUrl);
                        databaseReference.updateChildren(childUpdates);
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
}
