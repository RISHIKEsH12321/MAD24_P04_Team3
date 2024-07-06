package sg.edu.np.mad.travelhub;

import static androidx.core.content.ContextCompat.getSystemService;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.List;
import java.util.Map;

public class PostEdit extends AppCompatActivity implements ChildMainAdapter.OnChildMainInteractionListener {

    private String postId;
    private FirebaseViewModel firebaseViewModel;
    private RecyclerView childMainRecyclerView;
    private ChildMainAdapter childMainAdapter;
    private FloatingActionButton btnAddChild, btnCreate;

    private AppCompatTextView postName;
    private AppCompatImageView postImage;
    //private RecyclerView postRecyclerView;
    private ParentItem parentItem;
    private ParentItem childMain;


    private EditText etName, etDescription;
    private TextView tvName, tvDescription, tvUser;


    // merge
    private DatabaseReference databaseReference;
    private AppCompatTextView actvName;


    //image
    private String downloadUrl;
    private Uri imageUri;
    private ActivityResultLauncher<Intent> getResult;
    private final Loading_Dialog loadingDialog = new Loading_Dialog(PostEdit.this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_post_edit);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Register the activity result launcher
        getResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    // Handle the Intent result here
                    imageUri = data.getData();
                    postImage.setImageURI(imageUri);
                    Log.d("IMAGEURI", String.valueOf(imageUri));
                    // Now that you have the image URI, you can proceed with uploading
                    loadingDialog.startLoadingDialog();
                    uploadToFirebase(postId, imageUri);
                }
            }
        });

        //popup menu
        AppCompatButton menu = findViewById(R.id.PObtnMenu);
        registerForContextMenu(menu);

        //PostId intent from postlist
        Intent intentFromPost = getIntent();
        postId = intentFromPost.getStringExtra("postId");

        tvName = findViewById(R.id.POtvName);
        postImage = findViewById(R.id.POacivPostImage);

        childMainRecyclerView = findViewById(R.id.POrvChildMainRecyclerView);
//        childMainRecyclerView = findViewById(R.id.childMainRecyclerView);

        //Recyclerview
        childMainRecyclerView.setHasFixedSize(true);
        childMainRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        //Adapter
        childMainAdapter = new ChildMainAdapter(2);
        childMainAdapter.setParentKey(postId);
        childMainAdapter.setOnChildMainInteractionListener(this);

        childMainRecyclerView.setAdapter(childMainAdapter);

        //childMainButton.setVisibility(View.INVISIBLE);
        //Firebase
        firebaseViewModel = new ViewModelProvider(this).get(FirebaseViewModel.class);
        firebaseViewModel.getChildMainMutableLiveData().observe(this, new Observer<List<ChildMain>>() {
            @Override
            public void onChanged(List<ChildMain> childMainList) {
                childMainAdapter.setChildMainList(childMainList);
                //Log.d("CHILDMAINADAPTER_SIZE", String.valueOf(childMainAdapter.getChildMainList().get(0).getChildMainName()));
                childMainAdapter.notifyDataSetChanged();
            }
        });
        firebaseViewModel.getDatabaseErrorMutableLiveData().observe(this, new Observer<DatabaseError>() {
            @Override
            public void onChanged(DatabaseError error) {
                Toast.makeText(PostEdit.this, error.toString(), Toast.LENGTH_SHORT).show();
            }
        });

        firebaseViewModel.getAllChildMainData(postId);

        //Get post name, etc...
        databaseReference = FirebaseDatabase.getInstance().getReference()
                .child("Posts");
        databaseReference.child(postId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    ParentItem parentItem = snapshot.getValue(ParentItem.class);
                    tvName.setText(parentItem.getParentName());
                    if (postImage != null) {
                        Glide.with(getApplicationContext())
                                .load(parentItem.getParentImage())
                                .into(postImage);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //Initialise Button
        btnAddChild = findViewById(R.id.POfabAddChild);
        btnAddChild.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                childMainAdapter.addChildMain();
                // Scroll to the newly added item
                childMainRecyclerView.scrollToPosition(childMainAdapter.getItemCount() - 1);
            }
        });
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_post, menu);;
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.uploadImage) {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            getResult.launch(intent);
        }
        if (item.getItemId() == R.id.delete) {
            deletePost();
            Intent intent = new Intent(this, PostList.class);
            startActivity(intent);
            finish();
        }
        return true;
    }

    public void onSaveButtonClick(ChildMain childMain) {
        saveChildMainData(childMain);
    }

    private void saveChildMainData(ChildMain childMain) {

        //update childmainname


        //update childitem
        DatabaseReference childMainRef = databaseReference.child(postId).child("childData").child(childMain.getKey());
        List<ChildItem> childData = childMain.getChildItemList();
        childMainRef.setValue(childMain).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    childMainRef.child("childItemList").setValue(childData).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.d("TAG", "ChildData updated successfully");
                            } else {
                                Log.e("TAG", "Error updated ChildData", task.getException());
                            }
                        }
                    });
                } else {
                    Log.e("TAG", "Error updating childdata", task.getException());
                }
            }
        });
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int) event.getRawX(), (int) event.getRawY())) {
                    v.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        }
        return super.dispatchTouchEvent(event);
    }

    private void uploadToFirebase(String uid, Uri imUri) {
        DatabaseReference postRef = FirebaseDatabase.getInstance().getReference().child("Posts").child(postId);

        // Fetch the existing image URL from the database
        postRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    ParentItem parentItem = snapshot.getValue(ParentItem.class);
                    String existingImageUrl = parentItem.getParentImage();

                    // If an existing image URL is found, delete the existing image from Firebase Storage
                    if (existingImageUrl != null && !existingImageUrl.isEmpty()) {
                        StorageReference existingImageRef = FirebaseStorage.getInstance().getReferenceFromUrl(existingImageUrl);
                        existingImageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                // Existing image deleted, proceed to upload the new image
                                Log.d("Upload", "Existing image deleted");
                                performUpload(uid, imUri);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Handle failure in deleting the existing image
                                Log.e("Upload", "Failed to delete existing image", e);
                            }
                        });
                    } else {
                        // No existing image found, proceed to upload the new image
                        performUpload(uid, imUri);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle database read error
                Log.e("Upload", "Database error: " + error.getMessage());
            }
        });
    }

    private void performUpload(String uid, Uri imUri) {
        StorageReference fileRef = FirebaseStorage.getInstance().getReference().child(postId).child(postId + "." + getFileExtension(imUri));
        fileRef.putFile(imUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                downloadUrl = uri.toString();
                                Log.d("IMAGEURL", downloadUrl);

                                // Update the image URL in the database
                                DatabaseReference postRef = FirebaseDatabase.getInstance().getReference().child("Posts").child(postId);

                                postRef.child("parentImage").setValue(downloadUrl);

                                // Dismiss the loading dialog
                                loadingDialog.dismissDialog();
                                Toast.makeText(getApplicationContext(), "Image successfully uploaded", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle image upload failure
                        Log.e("Upload", "Failed to upload image", e);
                        Toast.makeText(getApplicationContext(), "Failed to upload image", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void deletePost() {
        DatabaseReference postRef = FirebaseDatabase.getInstance().getReference("Posts");
    }

    private String getFileExtension(Uri imUri){
        ContentResolver cr = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cr.getType(imUri));
    }
}

//where is updating of childitem list