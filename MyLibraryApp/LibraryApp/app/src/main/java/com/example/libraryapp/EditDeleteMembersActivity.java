package com.example.libraryapp;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SearchView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.libraryapp.api.ApiService;
import com.example.libraryapp.api.Member;
import com.example.libraryapp.api.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

public class EditDeleteMembersActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private MemberAdapter adapter;
    private SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_delete_members);

        recyclerView = findViewById(R.id.member_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MemberAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        searchView = findViewById(R.id.search_view);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                new SearchMembersTask().execute(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) {
                    new GetAllMembersTask().execute();
                }
                return true;
            }
        });

        new GetAllMembersTask().execute();
    }

    private class GetAllMembersTask extends AsyncTask<Void, Void, List<Member>> {
        @Override
        protected List<Member> doInBackground(Void... voids) {
            try {
                return RetrofitClient.getApiService().getAllMembers().execute().body();
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<Member> members) {
            if (members != null) {
                adapter.updateMembers(members);
            }
        }
    }

    private class SearchMembersTask extends AsyncTask<String, Void, List<Member>> {
        @Override
        protected List<Member> doInBackground(String... queries) {
            try {
                List<Member> allMembers = RetrofitClient.getApiService().getAllMembers().execute().body();
                List<Member> filtered = new ArrayList<>();
                for (Member member : allMembers) {
                    if (member.username.toLowerCase().contains(queries[0].toLowerCase()) ||
                            member.email.toLowerCase().contains(queries[0].toLowerCase())) {
                        filtered.add(member);
                    }
                }
                return filtered;
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<Member> members) {
            if (members != null) {
                adapter.updateMembers(members);
            }
        }
    }

    private class MemberAdapter extends RecyclerView.Adapter<MemberAdapter.ViewHolder> {
        private List<Member> members;

        public MemberAdapter(List<Member> members) {
            this.members = members;
        }

        public void updateMembers(List<Member> members) {
            this.members = members;
            notifyDataSetChanged();
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_member, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Member member = members.get(position);
            holder.nameTextView.setText(member.firstname + " " + member.lastname);
            holder.emailTextView.setText(member.email);

            holder.editButton.setOnClickListener(v -> {
                View dialogView = LayoutInflater.from(EditDeleteMembersActivity.this).inflate(R.layout.dialog_edit_member, null);
                EditText firstNameEditText = dialogView.findViewById(R.id.first_name);
                EditText lastNameEditText = dialogView.findViewById(R.id.last_name);
                EditText emailEditText = dialogView.findViewById(R.id.email);
                EditText contactEditText = dialogView.findViewById(R.id.contact);
                EditText membershipEndDateEditText = dialogView.findViewById(R.id.membership_end_date);

                firstNameEditText.setText(member.firstname);
                lastNameEditText.setText(member.lastname);
                emailEditText.setText(member.email);
                contactEditText.setText(member.contact);
                membershipEndDateEditText.setText(member.membership_end_date);

                new AlertDialog.Builder(EditDeleteMembersActivity.this)
                        .setTitle("Edit Member")
                        .setView(dialogView)
                        .setPositiveButton("Save", (dialog, which) -> {
                            Member updatedMember = new Member();
                            updatedMember.firstname = firstNameEditText.getText().toString().trim();
                            updatedMember.lastname = lastNameEditText.getText().toString().trim();
                            updatedMember.email = emailEditText.getText().toString().trim();
                            updatedMember.contact = contactEditText.getText().toString().trim();
                            updatedMember.membership_end_date = membershipEndDateEditText.getText().toString().trim();
                            new UpdateMemberTask().execute(member.username, updatedMember);
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            });

            holder.deleteButton.setOnClickListener(v -> {
                new AlertDialog.Builder(EditDeleteMembersActivity.this)
                        .setTitle("Confirm Delete")
                        .setMessage("Delete " + member.username + "?")
                        .setPositiveButton("Yes", (dialog, which) -> new DeleteMemberTask().execute(member.username))
                        .setNegativeButton("No", null)
                        .show();
            });
        }

        @Override
        public int getItemCount() {
            return members.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView nameTextView, emailTextView;
            Button editButton, deleteButton;

            ViewHolder(View itemView) {
                super(itemView);
                nameTextView = itemView.findViewById(R.id.name);
                emailTextView = itemView.findViewById(R.id.email);
                editButton = itemView.findViewById(R.id.edit_button);
                deleteButton = itemView.findViewById(R.id.delete_button);
            }
        }
    }

    private class UpdateMemberTask extends AsyncTask<Object, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Object... params) {
            try {
                return RetrofitClient.getApiService().updateMember((String) params[0], (Member) params[1]).execute().isSuccessful();
            } catch (Exception e) {
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                Toast.makeText(EditDeleteMembersActivity.this, "Member updated", Toast.LENGTH_SHORT).show();
                new GetAllMembersTask().execute();
            } else {
                Toast.makeText(EditDeleteMembersActivity.this, "Failed to update member", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class DeleteMemberTask extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... usernames) {
            try {
                return RetrofitClient.getApiService().deleteMember(usernames[0]).execute().isSuccessful();
            } catch (Exception e) {
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                Toast.makeText(EditDeleteMembersActivity.this, "Member deleted", Toast.LENGTH_SHORT).show();
                new GetAllMembersTask().execute();
            } else {
                Toast.makeText(EditDeleteMembersActivity.this, "Failed to delete member", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
