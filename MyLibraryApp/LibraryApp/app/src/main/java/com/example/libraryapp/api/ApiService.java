package com.example.libraryapp.api;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

import java.util.List;

public interface ApiService {
    @POST("login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @GET("members")
    Call<List<Member>> getAllMembers();

    @GET("members/{username}")
    Call<Member> getMember(@Path("username") String username);

    @POST("members")
    Call<GenericResponse> addMember(@Body Member member);

    @PUT("members/{username}")
    Call<GenericResponse> updateMember(@Path("username") String username, @Body Member member);

    @DELETE("members/{username}")
    Call<GenericResponse> deleteMember(@Path("username") String username);

    @GET("books/{username}")
    Call<List<BookIssue>> getBooksIssued(@Path("username") String username);

    @POST("books")
    Call<GenericResponse> issueBook(@Body BookIssue bookIssue);

    @DELETE("books")
    Call<GenericResponse> removeBookIssue(@Body BookIssueRequest bookIssueRequest);
}

class LoginRequest {
    String username;
    String password;
    boolean isStaff;

    LoginRequest(String username, String password, boolean isStaff) {
        this.username = username;
        this.password = password;
        this.isStaff = isStaff;
    }
}

class LoginResponse {
    boolean success;
    String username;
    boolean isStaff;
}

class Member {
    String username;
    String firstname;
    String lastname;
    String email;
    String contact;
    String membership_end_date;
}

class BookIssue {
    int id;
    String username;
    String book_title;
    String issue_date;
    String return_date;
}

class BookIssueRequest {
    String username;
    String book_title;

    BookIssueRequest(String username, String book_title) {
        this.username = username;
        this.book_title = book_title;
    }
}

class GenericResponse {
    String message;
}
