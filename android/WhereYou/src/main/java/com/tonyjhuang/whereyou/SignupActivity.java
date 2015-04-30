package com.tonyjhuang.whereyou;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.parse.FunctionCallback;
import com.parse.ParseAnalytics;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseInstallation;

import org.json.JSONArray;

import java.util.HashMap;
import java.util.Map;

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
    @InjectView(R.id.username_status)
    TextView usernameStatus;

    // Colors for username status text.
    private int green, red;

    private Debouncer debouncer = new Debouncer(150);

    // Have we vetted the user's current username against the server?
    private boolean usernameIsDirty = false;
    private boolean usernameAvailable = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        ParseAnalytics.trackAppOpenedInBackground(getIntent());

        green = getResources().getColor(R.color.green);
        red = getResources().getColor(R.color.red);

        usernameInput.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() == 0) {
                    usernameHint.setVisibility(View.INVISIBLE);
                    setUsernameStatus(UsernameStatus.TOO_SHORT);
                } else {
                    usernameHint.setVisibility(View.VISIBLE);
                    usernameIsDirty = true;
                    String error = getError(charSequence.toString());

                    if (error != null) {
                        debouncer.cancel();
                        if (error.equals(getString(R.string.signup_error_length))) {
                            setUsernameStatus(UsernameStatus.TOO_SHORT);
                        } else if (error.equals(getString(R.string.signup_error_spaces))) {
                            setUsernameStatus(UsernameStatus.ERROR);
                        }
                    } else {
                        setUsernameStatus(UsernameStatus.CHECKING);
                        checkServerIfAvailable(charSequence.toString());
                    }
                }
            }
        });
    }

    private void setUsernameStatus(UsernameStatus status) {
        int color = red;
        int visibility;
        switch(status) {
            case TAKEN:
                usernameStatus.setText(getString(R.string.signup_status_taken));
                color = red;
                visibility = View.VISIBLE;
                break;
            case ERROR:
                usernameStatus.setText(getString(R.string.signup_status_error));
                color = red;
                visibility = View.VISIBLE;
                break;
            case TOO_SHORT:
                visibility = View.INVISIBLE;
                break;
            case AVAILABLE:
                usernameStatus.setText(getString(R.string.signup_status_available));
                color = green;
                visibility= View.VISIBLE;
                break;
            case CHECKING:
                visibility = View.INVISIBLE;
                break;
            default:
                visibility = View.INVISIBLE;
        }

        usernameStatus.setVisibility(visibility);
        usernameStatus.setTextColor(color);
    }

    private void checkServerIfAvailable(final String name) {
        if(name.length() < 3) {
            debouncer.cancel();
        } else {
            debouncer.debounce(new Runnable() {
                @Override
                public void run() {
                    checkName(name, new FunctionCallback<Boolean>() {
                        @Override
                        public void done(Boolean nameExists, ParseException e) {
                            if (e == null) {
                                usernameIsDirty = false;
                                if (nameExists) {
                                    usernameAvailable = false;
                                    setUsernameStatus(UsernameStatus.TAKEN);
                                } else {
                                    usernameAvailable = true;
                                    setUsernameStatus(UsernameStatus.AVAILABLE);
                                }
                            } else {
                                Log.e("Main", e.getMessage());
                            }
                        }
                    });
                }
            });
        }
    }

    private void checkName(String name, FunctionCallback<Boolean> callback) {
        Map<String, Object> params = new HashMap<>();
        params.put("name", name);
        ParseCloud.callFunctionInBackground("checkName", params, callback);
    }

    @OnClick(R.id.signup_submit)
    public void onSignupSubmitClick(View view) {
        saveUsername();
    }

    private String getError(String name) {
        String errorMessage = null;

        if(name.contains(" ")) {
            errorMessage = getString(R.string.signup_error_spaces);
        } else if(name.length() < 3) {
            errorMessage = getString(R.string.signup_error_length);
        } else if(!usernameIsDirty && !usernameAvailable) {
            errorMessage = getString(R.string.signup_error_taken);
        }

        return errorMessage;
    }

    private void saveUsername() {
        // Validate
        String username = usernameInput.getText().toString();
        String errorMessage = getError(username);

        if(errorMessage != null) {
            showToast(errorMessage);
        } else {
            if(usernameIsDirty) {
                checkServerIfAvailable(username);
            } else {
                ParseInstallation currentInstallation = ParseInstallation.getCurrentInstallation();
                currentInstallation.put("name", username);
                currentInstallation.put("nameLowercase", username.toLowerCase());
                currentInstallation.saveInBackground();
                redirectToMain();
            }
        }
    }

    private void redirectToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    enum UsernameStatus {
        TAKEN, AVAILABLE, TOO_SHORT, ERROR, CHECKING
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

    private class Debouncer {
        private long timeout;
        private Handler handler = new Handler();
        private Runnable currentRunnable;

        public Debouncer(long timeout) {
            this.timeout = timeout;
        }

        public void debounce(Runnable runnable) {
            cancel();
            currentRunnable = runnable;
            handler.postDelayed(currentRunnable, timeout);
        }

        public void cancel() {
            if(currentRunnable != null) handler.removeCallbacks(currentRunnable);
        }
    }
}
