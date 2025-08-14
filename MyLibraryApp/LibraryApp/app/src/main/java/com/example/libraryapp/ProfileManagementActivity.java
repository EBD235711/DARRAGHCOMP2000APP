package com.example.libraryapp;

import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.libraryapp.api.ApiService;
import com.example.libraryapp.api.Member;
import com.example.libraryapp.api.RetrofitClient;
import com.google.firebase.messaging.FirebaseMessaging;

public class ProfileManagementActivity extends AppCompatActivity {
    private EditText firstNameEditText, lastNameEditText, emailEditText, contactEditText;
    private CheckBox emailNotifications, smsNotifications, pushNotifications;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_management);

        username = getIntent().getStringExtra("username");
        firstNameEditText = findViewById(R.id.first_name);
        lastNameEditText = findViewById(R.id.last_name);
        emailEditText = findViewById(R.id.email);
        contactEditText = findViewById(R.id.contact);
        emailNotifications = findViewById(R.id.email_notifications);
        smsNotifications = findViewById(R.id.sms_notifications);
        pushNotifications = findViewById(R.id.push_notifications);

        Button saveButton = findViewById(R.id.save_button);
        Button cancelButton = findViewById(R.id.cancel_button);

        new GetMemberTask().execute(username);

        saveButton.setOnClickListener(v -> {
            Member member = new Member();
            member.firstname = firstNameEditText.getText().toString().trim();
            member.lastname = lastNameEditText.getText().toString().trim();
            member.email = emailEditText.getText().toString().trim();
            member.contact = contactEditText.getText().toString().trim();

            if (member.firstname.isEmpty() || member.email.isEmpty() ||
                    !member.email.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
                Toast.makeText(this, "Please fill all fields correctly", Toast.LENGTH_SHORT).show();
                return;
            }

            if (pushNotifications.isChecked()) {
                FirebaseMessaging.getInstance().subscribeToTopic("notifications");
            } else {
                FirebaseMessaging.getInstance().unsubscribeFromTopic("notifications");
            }

            new UpdateMemberTask().execute(member);
        });

        cancelButton.setOnClickListener(v -> finish());
    }

    private class GetMemberTask extends AsyncTask<String, Void, Member> {
        @Override
        protected Member doInBackground(String... usernames) {
            try {
                return RetrofitClient.getApiService().getMember(usernames[0]).execute().body();
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(Member member) {
            if (member != null) {
                firstNameEditText.setText(member.firstname);
                lastNameEditText.setText(member.lastname);
                emailEditText.setText(member.email);
                contactEditText.setText(member.contact);
            }
        }
    }

    private class UpdateMemberTask extends AsyncTask<Member, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Member... members) {
            try {
                return RetrofitClient.getApiService().updateMember(username, members[0]).execute().isSuccessful();
            } catch (Exception e) {
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                Toast.makeText(ProfileManagementActivity.this, "Profile updated", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(ProfileManagementActivity.this, "Failed to update profile", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
