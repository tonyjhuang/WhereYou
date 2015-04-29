package com.tonyjhuang.whereyou;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.parse.ParseAnalytics;

import butterknife.InjectView;
import butterknife.OnClick;

/**
 * Created by tony on 4/29/15.
 */
public class SignupActivity extends WhereYouActivity {

    @InjectView(R.id.username_hint)
    TextView usernameHint;
    @InjectView(R.id.username)
    EditText usernameInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        ParseAnalytics.trackAppOpenedInBackground(getIntent());

        usernameInput.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                usernameHint.setVisibility(charSequence.length() == 0 ? View.INVISIBLE : View.VISIBLE);
            }
        });
    }

    @OnClick(R.id.signup_submit)
    public void onSignupSubmitClick(View view) {
        showToast("Nice try guy!");
    }

    private class SimpleTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void afterTextChanged(Editable editable) {
        }
    }
}
