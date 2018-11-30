package app.bookshare;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
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
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        ButterKnife.bind(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @OnClick(R.id.btnWithText)
    public void startMainActivity() {

        mAuth.createUserWithEmailAndPassword(etEmail.getText().toString().trim(), password.getText().toString())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        updateUI(user);
                    }
                });


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
        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

        databaseReference.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.hasChild(user.getUid())) {
                    Map<String, Object> userValues = user.toMap();

                    Map<String, Object> childUpdates = new HashMap<>();
                    childUpdates.put("/users/" + user.getUid(), userValues);

                    databaseReference.updateChildren(childUpdates);
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
}
