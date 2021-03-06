package app.bookshare.util;

import android.support.design.widget.TextInputLayout;
import android.text.TextUtils;
import android.widget.EditText;

public class Common {

    public static boolean checkEmpty(EditText editText, TextInputLayout textInputLayout, String errorMessage) {
        if (!TextUtils.isEmpty(editText.getText().toString())) {
            textInputLayout.setErrorEnabled(false);
            textInputLayout.setError(null);
            return true;
        } else {
            textInputLayout.setError(errorMessage);
            return false;
        }
    }

    public static class KeyIntents {
        public static final String ARG_BOOK_KEY = "arg_book_key";
        public static String ARG_BOOK = "bookDetail";
    }
}
