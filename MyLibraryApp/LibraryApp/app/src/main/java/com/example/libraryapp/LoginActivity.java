package com.example.libraryapp;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.libraryapp.api.ApiService;
import com.example.libraryapp.api.LoginRequest;
import com.example.libraryapp.api.LoginResponse;
import com.example.libraryapp.api.RetrofitClient;

public class LoginActivity extends AppCompatActivity {
    private EditText usernameEditText, passwordEditText;
    private RadioGroup userTypeRadioGroup;
    private Button loginButton;
    private TextView errorTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        usernameEditText = findViewById(R.id.username);
        passwordEditText = findViewById(R.id.password);
        userTypeRadioGroup = findViewById(R.id.user_type);
        loginButton = findViewById(R.id.login_button);
        errorTextView = findViewById(R.id.error_message);

        loginButton.setOnClickListener(v -> {
            String username = usernameEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();
            boolean isStaff = userTypeRadioGroup.getCheckedRadioButtonId() == R.id.staff_radio;

            if (username.isEmpty() || password.isEmpty()) {
                errorTextView.setText("Please fill in all fields");
                return;
            }

            new LoginTask().execute(username, password, String.valueOf(isStaff));
        });
    }

    private class LoginTask extends AsyncTask<String, Void, LoginResponse> {
        @Override
        protected LoginResponse doInBackground(String... params) {
            ApiService api = RetrofitClient.getApiService();
            Call<LoginResponse> call = api.login(new LoginRequest(params[0], params[1], Boolean.parseBoolean(params[2])));
            try {
                return call.execute().body();
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(LoginResponse response) {
            if (response != null && response.success) {
                Intent intent = response.isStaff ?
                        new Intent(LoginActivity.this, StaffDashboardActivity.class) :
                        new Intent(LoginActivity.this, MemberDashboardActivity.class);
                intent.putExtra("username", response.username);
                startActivity(intent);
                finish();
            } else {
                errorTextView.setText("Invalid credentials");
                Toast.makeText(LoginActivity.this, "Invalid login", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
