package com.example.libraryapp;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.libraryapp.api.ApiService;
import com.example.libraryapp.api.BookIssue;
import com.example.libraryapp.api.BookIssueRequest;
import com.example.libraryapp.api.RetrofitClient;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.List;

public class ViewRequestsActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private RequestAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_requests);

        recyclerView = findViewById(R.id.request_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RequestAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        new GetAllRequestsTask().execute();
    }

    private class GetAllRequestsTask extends AsyncTask<Void, Void, List<BookIssue>> {
        @Override
        protected List<BookIssue> doInBackground(Void... voids) {
            List<BookIssue> allRequests = new ArrayList<>();
            try {
                List<Member> members = RetrofitClient.getApiService().getAllMembers().execute().body();
                for (Member member : members) {
                    List<BookIssue> requests = RetrofitClient.getApiService().getBooksIssued(member.username).execute().body();
                    allRequests.addAll(requests);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return allRequests;
        }

        @Override
        protected void onPostExecute(List<BookIssue> requests) {
            adapter.updateRequests(requests);
        }
    }

    private class ApproveRequestTask extends AsyncTask<BookIssue, Void, Boolean> {
        @Override
        protected Boolean doInBackground(BookIssue... requests) {
            // Simulate approval (update book status in SQLite)
            Book book = new Book(requests[0].book_title, "", "", "Borrowed");
            new ManageBooksActivity.UpdateBookTask().execute(book);
            // Send Firebase notification
            FirebaseMessaging.getInstance().subscribeToTopic("requests");
            return true;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                Toast.makeText(ViewRequestsActivity.this, "Request approved, notification sent", Toast.LENGTH_SHORT).show();
                new GetAllRequestsTask().execute();
            }
        }
    }

    private class DenyRequestTask extends AsyncTask<BookIssue, Void, Boolean> {
        @Override
        protected Boolean doInBackground(BookIssue... requests) {
            try {
                return RetrofitClient.getApiService().removeBookIssue(new BookIssueRequest(requests[0].username, requests[0].book_title)).execute().isSuccessful();
            } catch (Exception e) {
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                Toast.makeText(ViewRequestsActivity.this, "Request denied, notification sent", Toast.LENGTH_SHORT).show();
                new GetAllRequestsTask().execute();
            }
        }
    }

    private class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.ViewHolder> {
        private List<BookIssue> requests;

        public RequestAdapter(List<BookIssue> requests) {
            this.requests = requests;
        }

        public void updateRequests(List<BookIssue> requests) {
            this.requests = requests;
            notifyDataSetChanged();
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_request, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            BookIssue request = requests.get(position);
            holder.memberTextView.setText(request.username);
            holder.bookTextView.setText(request.book_title);
            holder.dateTextView.setText(request.issue_date);

            holder.approveButton.setOnClickListener(v -> {
                new AlertDialog.Builder(ViewRequestsActivity.this)
                        .setTitle("Confirm Approve")
                        .setMessage("Approve request for " + request.book_title + "?")
                        .setPositiveButton("Yes", (dialog, which) -> new ApproveRequestTask().execute(request))
                        .setNegativeButton("No", null)
                        .show();
            });

            holder.denyButton.setOnClickListener(v -> {
                new AlertDialog.Builder(ViewRequestsActivity.this)
                        .setTitle("Confirm Deny")
                        .setMessage("Deny request for " + request.book_title + "?")
                        .setPositiveButton("Yes", (dialog, which) -> new DenyRequestTask().execute(request))
                        .setNegativeButton("No", null)
                        .show();
            });
        }

        @Override
        public int getItemCount() {
            return requests.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView memberTextView, bookTextView, dateTextView;
            Button approveButton, denyButton;

            ViewHolder(View itemView) {
                super(itemView);
                memberTextView = itemView.findViewById(R.id.member);
                bookTextView = itemView.findViewById(R.id.book);
                dateTextView = itemView.findViewById(R.id.date);
                approveButton = itemView.findViewById(R.id.approve_button);
                denyButton = itemView.findViewById(R.id.deny_button);
            }
        }
    }
}
