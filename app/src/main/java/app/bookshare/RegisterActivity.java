package app.bookshare;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

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

import java.util.HashMap;
import java.util.Map;

import app.bookshare.model.UserModel;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;
import pub.devrel.easypermissions.EasyPermissions;

public class RegisterActivity extends AppCompatActivity {

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
}
