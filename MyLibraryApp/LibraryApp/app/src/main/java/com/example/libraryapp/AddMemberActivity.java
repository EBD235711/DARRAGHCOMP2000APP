package com.example.libraryapp;

import android.app.DatePickerDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.libraryapp.api.ApiService;
import com.example.libraryapp.api.Member;
import com.example.libraryapp.api.RetrofitClient;

import java.util.Calendar;

public class AddMemberActivity extends AppCompatActivity {
    private EditText usernameEditText, firstNameEditText, lastNameEditText, emailEditText, contactEditText, membershipEndDateEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_member);

        usernameEditText = findViewById(R.id.username);
        firstNameEditText = findViewById(R.id.first_name);
        lastNameEditText = findViewById(R.id.last_name);
        emailEditText = findViewById(R.id.email);
        contactEditText = findViewById(R.id.contact);
        membershipEndDateEditText = findViewById(R.id.membership_end_date);

        membershipEndDateEditText.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            new DatePickerDialog(this, (view, year, month, day) -> {
                membershipEndDateEditText.setText(String.format("%d-%02d-%02d", year, month + 1, day));
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
        });

        Button submitButton = findViewById(R.id.submit_button);
        Button cancelButton = findViewById(R.id.cancel_button);

        submitButton.setOnClickListener(v -> {
            Member member = new Member();
            member.username = usernameEditText.getText().toString().trim();
            member.firstname = firstNameEditText.getText().toString().trim();
            member.lastname = lastNameEditText.getText().toString().trim();
            member.email = emailEditText.getText().toString().trim();
            member.contact = contactEditText.getText().toString().trim();
            member.membership_end_date = membershipEndDateEditText.getText().toString().trim();

            if (member.username.isEmpty() || member.firstname.isEmpty() || member.email.isEmpty() ||
                    !member.email.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
                Toast.makeText(this, "Please fill all fields correctly", Toast.LENGTH_SHORT).show();
                return;
            }

            new AddMemberTask().execute(member);
        });

        cancelButton.setOnClickListener(v -> finish());
    }

    private class AddMemberTask extends AsyncTask<Member, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Member... members) {
            ApiService api = RetrofitClient.getApiService();
            try {
                return api.addMember(members[0]).execute().isSuccessful();
            } catch (Exception e) {
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                Toast.makeText(AddMemberActivity.this, "Member added", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(AddMemberActivity.this, "Failed to add member", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
