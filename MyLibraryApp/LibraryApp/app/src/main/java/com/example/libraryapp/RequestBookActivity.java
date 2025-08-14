package com.example.libraryapp;

import android.app.DatePickerDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.libraryapp.api.ApiService;
import com.example.libraryapp.api.BookIssue;
import com.example.libraryapp.api.RetrofitClient;

import java.util.Calendar;

public class RequestBookActivity extends AppCompatActivity {
    private EditText bookTitleEditText, issueDateEditText, returnDateEditText;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_book);

        username = getIntent().getStringExtra("username");
        bookTitleEditText = findViewById(R.id.book_title);
        issueDateEditText = findViewById(R.id.issue_date);
        returnDateEditText = findViewById(R.id.return_date);

        String bookTitle = getIntent().getStringExtra("book_title");
        if (bookTitle != null) {
            bookTitleEditText.setText(bookTitle);
        }

        issueDateEditText.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            new DatePickerDialog(this, (view, year, month, day) -> {
                issueDateEditText.setText(String.format("%d-%02d-%02d", year, month + 1, day));
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
        });

        returnDateEditText.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            new DatePickerDialog(this, (view, year, month, day) -> {
                returnDateEditText.setText(String.format("%d-%02d-%02d", year, month + 1, day));
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
        });

        Button submitButton = findViewById(R.id.submit_button);
        Button cancelButton = findViewById(R.id.cancel_button);

        submitButton.setOnClickListener(v -> {
            BookIssue bookIssue = new BookIssue();
            bookIssue.username = username;
            bookIssue.book_title = bookTitleEditText.getText().toString().trim();
            bookIssue.issue_date = issueDateEditText.getText().toString().trim();
            bookIssue.return_date = returnDateEditText.getText().toString().trim();

            if (bookIssue.book_title.isEmpty() || bookIssue.issue_date.isEmpty() || bookIssue.return_date.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            new RequestBookTask().execute(bookIssue);
        });

        cancelButton.setOnClickListener(v -> finish());
    }

    private class RequestBookTask extends AsyncTask<BookIssue, Void, Boolean> {
        @Override
        protected Boolean doInBackground(BookIssue... bookIssues) {
            ApiService api = RetrofitClient.getApiService();
            try {
                return api.issueBook(bookIssues[0]).execute().isSuccessful();
            } catch (Exception e) {
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                Toast.makeText(RequestBookActivity.this, "Book request submitted", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(RequestBookActivity.this, "Failed to submit request", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
