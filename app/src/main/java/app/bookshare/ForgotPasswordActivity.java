package app.bookshare;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import app.bookshare.util.Common;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import es.dmoral.toasty.Toasty;

public class ForgotPasswordActivity extends AppCompatActivity {

    private static final String TAG = ForgotPasswordActivity.class.getSimpleName();
    @BindView(R.id.etEmail)
    EditText etEmail;
    @BindView(R.id.btn_submit)
    Button btnSubmit;
    @BindView(R.id.tlEmail)
    TextInputLayout tlEmail;
    @BindView(R.id.pbForgotPassword)
    ProgressBar pbForgotPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        ButterKnife.bind(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @OnClick(R.id.btn_submit)
    public void forgotPassword() {
        if (validate()) {
            pbForgotPassword.setVisibility(View.VISIBLE);
            FirebaseAuth.getInstance().sendPasswordResetEmail(etEmail.getText().toString())
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            pbForgotPassword.setVisibility(View.GONE);
                            if (task.isSuccessful()) {
                                Log.d(TAG, "Email sent.");
                                Toasty.info(ForgotPasswordActivity.this,
                                        "Please check your email", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        }

    }

    private boolean validate() {
        return Common.checkEmpty(etEmail, tlEmail, "Please enter email")
                && isEmailValid(etEmail.getText().toString());
    }

    private boolean isEmailValid(String email) {
        if (email.contains("@")) {
            tlEmail.setError(null);
            return true;
        } else {
            tlEmail.setError("Please enter valid email");
            return false;
        }
    }
}
