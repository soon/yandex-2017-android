package com.awesoon.thirdtask.web.rest;


import com.awesoon.thirdtask.web.rest.dto.NotesBackendInfoDto;
import com.awesoon.thirdtask.web.rest.dto.NotesBackendUserNote;
import com.awesoon.thirdtask.web.rest.dto.NotesBackendUserNoteDto;
import com.awesoon.thirdtask.web.rest.dto.NotesBackendUserNotesDto;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface NotesBackendService {
  @GET("info")
  Call<NotesBackendInfoDto> info();

  @GET("user/{userId}/notes")
  Call<NotesBackendUserNotesDto> getAllUserNotesByUserId(@Path("userId") Long userId);

  @GET("user/{userId}/note/{noteId}")
  Call<NotesBackendUserNoteDto> getUserNoteByUserIdAndNoteId(@Path("userId") Long userId, @Path("noteId") Long noteId);

  @POST("user/{userId}/notes")
  Call<NotesBackendUserNoteDto> createUserNote(@Path("userId") Long userId, @Body NotesBackendUserNote note);

  @POST("user/{userId}/note/{noteId}")
  Call<NotesBackendUserNoteDto> editUserNote(@Path("userId") Long userId, @Path("noteId") Long noteId,
                                             @Body NotesBackendUserNote note);

  @DELETE("user/{userId}/note/{noteId}")
  Call<NotesBackendUserNoteDto> deleteUserNote(@Path("userId") Long userId, @Path("noteId") Long noteId);
}
