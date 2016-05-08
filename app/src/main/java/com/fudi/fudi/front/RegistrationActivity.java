package com.fudi.fudi.front;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.fudi.fudi.R;
import com.fudi.fudi.back.Fud;
import com.fudi.fudi.back.FudiApp;

public class RegistrationActivity extends AppCompatActivity {

    private EditText phoneNumberInput;
    private EditText codeInput;
    private EditText usernameInput;
    private TextView usernameAvailText;
    private Button sendCode;
    private Button enterCode;
    private Button checkUsername;
    private Button register;
    private ProgressBar progressBar;
    private String code;
    private MinMaxTextWatcher<EditText> ummtw;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        if(FudiApp.getInstance().getThisUser() == null){
            Toast.makeText(this, "User profile has not loaded yet", Toast.LENGTH_SHORT).show();
            finish();
        }

        if(!FudiApp.hasNetworkConnection()){
            Toast.makeText(this, "No network connection", Toast.LENGTH_SHORT).show();
            finish();
        }

        if(FudiApp.getInstance().getThisUser().isRegistered()
                || getSharedPreferences(getString(R.string.preference_file_key),
                MODE_PRIVATE).getString("registered", "true").equals("true")){
            finish();
        }


        progressBar = (ProgressBar) findViewById(R.id.register_progress_bar);
        progressBar.setIndeterminate(true);

        phoneNumberInput =  (EditText) findViewById(R.id.register_phone_number);
        codeInput = (EditText) findViewById(R.id.register_code_input);
        usernameInput = (EditText) findViewById(R.id.register_username_input);
        ummtw = new MinMaxTextWatcher<EditText>(usernameInput,3,16,this);
        usernameInput.addTextChangedListener(ummtw);
        usernameAvailText = (TextView) findViewById(R.id.register_username_availability_text);
        register = (Button) findViewById(R.id.register_register_button);

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                FudiApp.hideSoftKeyboard(RegistrationActivity.this);

                String username = usernameInput.getText().toString();
                String phoneNumber = phoneNumberInput.getText().toString();

                if(!ummtw.isGood()){
                    Toast.makeText(RegistrationActivity.this,
                            "Username must be between 3 and 16 characters",Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.INVISIBLE);
                    return;
                }

                if (!FudiApp.getInstance().checkUsernameAvailability(username)) {
                    usernameAvailText.setTextColor(Color.RED);
                    usernameAvailText.setText("Not Available");
                    Toast.makeText(RegistrationActivity.this,
                            "Username now unavailable",Toast.LENGTH_LONG).show();
                    progressBar.setVisibility(View.INVISIBLE);
                    return;
                }

                if(!FudiApp.hasNetworkConnection()){
                    Toast.makeText(RegistrationActivity.this,
                            "No network connection detected",Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.INVISIBLE);
                    return;
                }

                FudiApp.getInstance().registerThisUser(username, phoneNumber);
                SharedPreferences sharedPref = getSharedPreferences(
                        getString(R.string.preference_file_key), MODE_PRIVATE);
                sharedPref.edit().putString("firstTime", "false").apply();
                sharedPref.edit().putString("registered", "true").apply();
                Intent i = new Intent(RegistrationActivity.this, MainActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
                Toast.makeText(RegistrationActivity.this,
                        "Success!",Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.INVISIBLE);

            }
        });

        checkUsername = (Button) findViewById(R.id.register_username_button);
        checkUsername.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FudiApp.hideSoftKeyboard(RegistrationActivity.this);
                String username = usernameInput.getText().toString();
                if(!ummtw.isGood()){
                    Toast.makeText(RegistrationActivity.this,
                            "Username must be between 3 and 16 characters",Toast.LENGTH_SHORT).show();
                }

                progressBar.setVisibility(View.VISIBLE);
                if(!FudiApp.getInstance().checkUsernameAvailability(username)){
                    usernameAvailText.setTextColor(Color.RED);
                    usernameAvailText.setText("Not Available");
                } else {
                    usernameAvailText.setTextColor(Color.GREEN);
                    usernameAvailText.setText("Available!");
                    register.setEnabled(true);
                }
                progressBar.setVisibility(View.INVISIBLE);
            }
        });

        enterCode = (Button) findViewById(R.id.register_enter_code_button);
        enterCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(progressBar.VISIBLE);
                String enteredCode = codeInput.getText().toString();
                if(enteredCode.equals(code) && FudiApp.getInstance().verifyCode(enteredCode)){
                    usernameInput.setEnabled(true);
                    checkUsername.setEnabled(true);
                    phoneNumberInput.setEnabled(false);
                    sendCode.setEnabled(false);
                    enterCode.setEnabled(false);
                    codeInput.setEnabled(false);
                } else {
                    Toast.makeText(RegistrationActivity.this,
                            "Entered code did not match sent code", Toast.LENGTH_SHORT).show();
                }
                progressBar.setVisibility(progressBar.INVISIBLE);
            }
        });

        sendCode = (Button) findViewById(R.id.register_send_code_button);
        sendCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FudiApp.hideSoftKeyboard(RegistrationActivity.this);
                String phoneNumber = phoneNumberInput.getText().toString();
                progressBar.setVisibility(View.VISIBLE);

                if(FudiApp.getInstance().checkPhoneNumberInUse(phoneNumber)){
                    Toast.makeText(RegistrationActivity.this,
                            "Phone number is already in use or invalid.", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.INVISIBLE);
                    return;
                }

                sendCode.setText("Resend Code");
                code = FudiApp.getInstance().generateAndSendCode(phoneNumber);
                enterCode.setEnabled(true);
                codeInput.setEnabled(true);

                progressBar.setVisibility(View.INVISIBLE);
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_registration, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
