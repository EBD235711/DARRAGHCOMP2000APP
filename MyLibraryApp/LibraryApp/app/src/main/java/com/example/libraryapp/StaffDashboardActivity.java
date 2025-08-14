package com.example.libraryapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class StaffDashboardActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff_dashboard);

        String username = getIntent().getStringExtra("username");
        TextView welcomeText = findViewById(R.id.welcome_text);
        welcomeText.setText("Welcome, " + username);

        Button addMemberButton = findViewById(R.id.add_member_button);
        Button editDeleteMembersButton = findViewById(R.id.edit_delete_members_button);
        Button manageBooksButton = findViewById(R.id.manage_books_button);
        Button viewRequestsButton = findViewById(R.id.view_requests_button);
        Button logoutButton = findViewById(R.id.logout_button);

        addMemberButton.setOnClickListener(v -> startActivity(new Intent(this, AddMemberActivity.class)));
        editDeleteMembersButton.setOnClickListener(v -> startActivity(new Intent(this, EditDeleteMembersActivity.class)));
        manageBooksButton.setOnClickListener(v -> startActivity(new Intent(this, ManageBooksActivity.class)));
        viewRequestsButton.setOnClickListener(v -> startActivity(new Intent(this, ViewRequestsActivity.class)));
        logoutButton.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }
}
