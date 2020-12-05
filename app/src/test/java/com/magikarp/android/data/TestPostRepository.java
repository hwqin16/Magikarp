package com.magikarp.android.data;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.net.Uri;
import com.android.volley.RequestQueue;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.magikarp.android.data.PostRepository.FileNameGenerator;
import com.magikarp.android.data.PostRepository.UploadUriListener;
import com.magikarp.android.data.model.DeleteMessageRequest;
import com.magikarp.android.data.model.DeleteMessageResponse;
import com.magikarp.android.data.model.GetMessagesResponse;
import com.magikarp.android.data.model.NewMessageRequest;
import com.magikarp.android.data.model.NewMessageResponse;
import com.magikarp.android.data.model.UpdateMessageResponse;
import com.magikarp.android.network.GsonRequest;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Class for testing {@code MapsRepository}.
 */
@RunWith(MockitoJUnitRunner.class)
public class TestPostRepository {

  private final String urlNewMessages = "https://www.example.com/%s/new";

  private final String urlUpdateMessages = "https://www.example.com/%s/update/%s";

  private final String urlDeleteMessages = "https://www.example.com/%s/delete/%s";

  private FileNameGenerator fileNameGenerator;

  private RequestQueue requestQueue;

  private StorageReference storageReference;

  private PostRepository postRepository;

  @Before
  public void setup() {
    fileNameGenerator = mock(FileNameGenerator.class);
    requestQueue = mock(RequestQueue.class);
    storageReference = mock(StorageReference.class);
    postRepository =
        new PostRepository(
            requestQueue,
            storageReference,
            fileNameGenerator,
            urlNewMessages,
            urlUpdateMessages,
            urlDeleteMessages);
  }

  @Test
  @SuppressWarnings({"unchecked", "rawtypes"})
  public void testNewMessage() {
    final String idToken = "idToken";
    final String userId = "userId";
    final double latitude = 1.0d;
    final double longitude = -1.0d;
    final String imageUrl = "imageUrlNew";
    final String text = "textNew";
    final Listener<NewMessageResponse> listener = mock(Listener.class);
    final ErrorListener errorListener = mock(ErrorListener.class);

    final ArgumentCaptor<GsonRequest> captor = ArgumentCaptor.forClass(GsonRequest.class);

    postRepository
        .newMessage(idToken, userId, latitude, longitude, imageUrl, text, listener, errorListener);

    verify(requestQueue).add(captor.capture());
    final GsonRequest request = captor.getValue();

    final NewMessageRequest body =
        new NewMessageRequest(idToken, imageUrl, text, latitude, longitude);
    assertEquals(String.format(urlNewMessages, userId), request.getUrl());
    assertEquals(new String(request.getBody()), new Gson().toJson(body));
  }

  @Test
  @SuppressWarnings({"unchecked", "rawtypes"})
  public void testUpdateMessage() {
    final String idToken = "idToken";
    final String postId = "postId";
    final String userId = "userIdUpdate";
    final double latitude = 2.0d;
    final double longitude = -2.0d;
    final String imageUrl = "imageUrlUpdate";
    final String text = "textUpdate";
    final Listener<UpdateMessageResponse> listener = mock(Listener.class);
    final ErrorListener errorListener = mock(ErrorListener.class);

    final ArgumentCaptor<GsonRequest> captor = ArgumentCaptor.forClass(GsonRequest.class);

    postRepository
        .updateMessage(idToken, postId, userId, latitude, longitude, imageUrl, text, listener,
            errorListener);

    verify(requestQueue).add(captor.capture());
    final GsonRequest request = captor.getValue();

    final NewMessageRequest body =
        new NewMessageRequest(idToken, imageUrl, text, latitude, longitude);
    assertEquals(String.format(urlUpdateMessages, userId, postId), request.getUrl());
    assertEquals(new Gson().toJson(body), new String(request.getBody()));
  }

  @Test
  @SuppressWarnings({"unchecked", "rawtypes"})
  public void testDeleteMessage() {
    final String idToken = "idToken";
    final String postId = "postId";
    final String userId = "userId";
    final Listener<DeleteMessageResponse> listener = mock(Listener.class);
    final ErrorListener errorListener = mock(ErrorListener.class);
    final ArgumentCaptor<GsonRequest> captor = ArgumentCaptor.forClass(GsonRequest.class);

    postRepository.deleteMessage(idToken, postId, userId, listener, errorListener);

    verify(requestQueue).add(captor.capture());
    final GsonRequest<GetMessagesResponse> request = captor.getValue();

    final DeleteMessageRequest body = new DeleteMessageRequest(idToken);
    assertEquals(String.format(urlDeleteMessages, userId, postId), request.getUrl());
    assertEquals(new Gson().toJson(body), new String(request.getBody()));
  }

  @Test
  @SuppressWarnings({"unchecked", "rawtypes"})
  public void testUploadFileTaskSuccessful() {
    final Uri fileUri = Uri.parse("file://fileUri");
    final String fileExtension = "jpg";
    final UploadUriListener listener = mock(UploadUriListener.class);

    final UploadTask uploadTask = mock(UploadTask.class);
    final Task continuationTask = mock(Task.class);

    when(storageReference.child(anyString())).thenReturn(storageReference);
    when(storageReference.putFile(fileUri)).thenReturn(uploadTask);
    when(uploadTask.continueWithTask(any(Continuation.class))).thenReturn(continuationTask);

    postRepository.uploadFile(fileUri, fileExtension, listener);

    verify(continuationTask).addOnCompleteListener(any(OnCompleteListener.class));
  }

  @Test
  public void testGetDownloadUrlSuccessful() throws Exception {
    final Task<Uri> downloadUriTask = mock(Task.class);
    final Task<UploadTask.TaskSnapshot> task = mock(Task.class);
    when(task.isSuccessful()).thenReturn(true);

    when(storageReference.getDownloadUrl()).thenReturn(downloadUriTask);
    assertEquals(downloadUriTask, postRepository.getDownloadUrl(storageReference, task));
  }

  @Rule
  public ExpectedException thrownOnDownloadTaskFail = ExpectedException.none();

  @Test
  public void testGetDownloadUrlNotSuccessful() throws Exception {
    thrownOnDownloadTaskFail.expect(NullPointerException.class);

    final Task<UploadTask.TaskSnapshot> task = mock(Task.class);

    when(task.isSuccessful()).thenReturn(false);
    postRepository.getDownloadUrl(storageReference, task);
  }

  @Test
  public void testGetDownloadUrlNonNullListener() throws Exception {
    final Uri fileUri = mock(Uri.class);
    final Task<Uri> uriTask = mock(Task.class);
    final UploadUriListener listener = mock(UploadUriListener.class);

    when(uriTask.isSuccessful()).thenReturn(true);
    when(uriTask.getResult()).thenReturn(mock(Uri.class));

    postRepository.onUriReceived(fileUri, uriTask, listener);

    verify(listener).onUriReceived(any(Uri.class), any(Uri.class));
  }

  @Test
  public void testGetDownloadUrlNullListener() throws Exception {
    final Uri fileUri = mock(Uri.class);
    final Task<Uri> uriTask = mock(Task.class);
    final UploadUriListener listener = null;

    postRepository.onUriReceived(fileUri, uriTask, listener);
    verify(uriTask, never()).isSuccessful();
  }

  @Test
  public void testGetDownloadUrlUriTaskNotSuccessful() throws Exception {
    final Uri fileUri = mock(Uri.class);
    final Task<Uri> uriTask = mock(Task.class);
    final UploadUriListener listener = mock(UploadUriListener.class);

    when(uriTask.isSuccessful()).thenReturn(false);

    postRepository.onUriReceived(fileUri, uriTask, listener);
    verify(uriTask).isSuccessful();
    verify(listener).onUriReceived(any(Uri.class), isNull());
  }

}
