package app.bookshare;

import android.annotation.SuppressLint;
import android.support.v7.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

import org.jetbrains.annotations.Nullable;

/**
 * Created by ADMIN-PC on 14-03-2017.
 */

@SuppressLint("Registered")
public class BaseActivity extends AppCompatActivity {


    @Nullable
    public String getUid() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

}
