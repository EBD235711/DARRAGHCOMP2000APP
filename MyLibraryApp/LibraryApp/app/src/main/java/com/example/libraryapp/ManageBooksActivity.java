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
import com.example.libraryapp.db.AppDatabase;
import com.example.libraryapp.db.Book;
import com.example.libraryapp.db.BookDao;

import java.util.ArrayList;
import java.util.List;

public class ManageBooksActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private BookAdapter adapter;
    private SearchView searchView;
    private BookDao bookDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_books);

        bookDao = AppDatabase.getInstance(this).bookDao();
        recyclerView = findViewById(R.id.book_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new BookAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        searchView = findViewById(R.id.search_view);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                new SearchBooksTask().execute(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) {
                    new GetAllBooksTask().execute();
                }
                return true;
            }
        });

        Button addBookButton = findViewById(R.id.add_book_button);
        addBookButton.setOnClickListener(v -> showAddBookDialog());

        new GetAllBooksTask().execute();
    }

    private void showAddBookDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_book, null);
        EditText titleEditText = dialogView.findViewById(R.id.title);
        EditText authorEditText = dialogView.findViewById(R.id.author);
        EditText isbnEditText = dialogView.findViewById(R.id.isbn);

        new AlertDialog.Builder(this)
                .setTitle("Add Book")
                .setView(dialogView)
                .setPositiveButton("Add", (dialog, which) -> {
                    Book book = new Book(
                            titleEditText.getText().toString().trim(),
                            authorEditText.getText().toString().trim(),
                            isbnEditText.getText().toString().trim(),
                            "Available"
                    );
                    if (book.title.isEmpty() || book.author.isEmpty()) {
                        Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    new AddBookTask().execute(book);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private class GetAllBooksTask extends AsyncTask<Void, Void, List<Book>> {
        @Override
        protected List<Book> doInBackground(Void... voids) {
            return bookDao.getAll();
        }

        @Override
        protected void onPostExecute(List<Book> books) {
            adapter.updateBooks(books);
        }
    }

    private class SearchBooksTask extends AsyncTask<String, Void, List<Book>> {
        @Override
        protected List<Book> doInBackground(String... queries) {
            return bookDao.searchBooks("%" + queries[0] + "%");
        }

        @Override
        protected void onPostExecute(List<Book> books) {
            adapter.updateBooks(books);
        }
    }

    private class AddBookTask extends AsyncTask<Book, Void, Void> {
        @Override
        protected Void doInBackground(Book... books) {
            bookDao.insert(books[0]);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Toast.makeText(ManageBooksActivity.this, "Book added", Toast.LENGTH_SHORT).show();
            new GetAllBooksTask().execute();
        }
    }

    private class UpdateBookTask extends AsyncTask<Book, Void, Void> {
        @Override
        protected Void doInBackground(Book... books) {
            bookDao.update(books[0]);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Toast.makeText(ManageBooksActivity.this, "Book updated", Toast.LENGTH_SHORT).show();
            new GetAllBooksTask().execute();
        }
    }

    private class DeleteBookTask extends AsyncTask<Book, Void, Void> {
        @Override
        protected Void doInBackground(Book... books) {
            bookDao.delete(books[0]);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Toast.makeText(ManageBooksActivity.this, "Book deleted", Toast.LENGTH_SHORT).show();
            new GetAllBooksTask().execute();
        }
    }

    private class BookAdapter extends RecyclerView.Adapter<BookAdapter.ViewHolder> {
        private List<Book> books;

        public BookAdapter(List<Book> books) {
            this.books = books;
        }

        public void updateBooks(List<Book> books) {
            this.books = books;
            notifyDataSetChanged();
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_book, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Book book = books.get(position);
            holder.titleTextView.setText(book.title);
            holder.authorTextView.setText(book.author);
            holder.statusTextView.setText(book.status);

            holder.editButton.setOnClickListener(v -> {
                View dialogView = LayoutInflater.from(ManageBooksActivity.this).inflate(R.layout.dialog_add_book, null);
                EditText titleEditText = dialogView.findViewById(R.id.title);
                EditText authorEditText = dialogView.findViewById(R.id.author);
                EditText isbnEditText = dialogView.findViewById(R.id.isbn);

                titleEditText.setText(book.title);
                authorEditText.setText(book.author);
                isbnEditText.setText(book.isbn);

                new AlertDialog.Builder(ManageBooksActivity.this)
                        .setTitle("Edit Book")
                        .setView(dialogView)
                        .setPositiveButton("Save", (dialog, which) -> {
                            book.title = titleEditText.getText().toString().trim();
                            book.author = authorEditText.getText().toString().trim();
                            book.isbn = isbnEditText.getText().toString().trim();
                            new UpdateBookTask().execute(book);
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            });

            holder.deleteButton.setOnClickListener(v -> {
                new AlertDialog.Builder(ManageBooksActivity.this)
                        .setTitle("Confirm Delete")
                        .setMessage("Delete " + book.title + "?")
                        .setPositiveButton("Yes", (dialog, which) -> new DeleteBookTask().execute(book))
                        .setNegativeButton("No", null)
                        .show();
            });
        }

        @Override
        public int getItemCount() {
            return books.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView titleTextView, authorTextView, statusTextView;
            Button editButton, deleteButton;

            ViewHolder(View itemView) {
                super(itemView);
                titleTextView = itemView.findViewById(R.id.title);
                authorTextView = itemView.findViewById(R.id.author);
                statusTextView = itemView.findViewById(R.id.status);
                editButton = itemView.findViewById(R.id.edit_button);
                deleteButton = itemView.findViewById(R.id.delete_button);
            }
        }
    }
}
