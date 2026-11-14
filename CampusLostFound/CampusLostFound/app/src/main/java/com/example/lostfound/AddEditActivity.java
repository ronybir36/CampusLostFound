package com.example.lostfound;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.lostfound.model.Item;
import com.example.lostfound.util.ImageUtils;
import com.example.lostfound.util.Ui;
import com.example.lostfound.util.Validator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Used for both Add and Edit. If EXTRA_ITEM_ID is passed, the screen loads
 * that post and saves changes to it. Otherwise it creates a new post.
 */
public class AddEditActivity extends AppCompatActivity {

    public static final String EXTRA_ITEM_ID = "item_id";

    private RadioGroup typeGroup;
    private TextInputEditText titleEt;
    private TextInputEditText descriptionEt;
    private TextInputEditText locationEt;
    private TextInputEditText contactEt;
    private ImageView preview;
    private Button saveBtn;
    private ProgressBar progress;
    private TextView headingTv;

    private FirebaseAuth auth;
    private DatabaseReference itemsRef;
    private String editingId;     // null when creating a new post
    private String imageBase64 = "";

    // Gallery picker: returns a content Uri
    private final ActivityResultLauncher<String> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), this::onGalleryResult);

    // Camera: returns a small thumbnail Bitmap, so no FileProvider or camera permission is needed
    private final ActivityResultLauncher<Void> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicturePreview(), this::onCameraResult);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit);

        typeGroup = findViewById(R.id.typeGroup);
        titleEt = findViewById(R.id.titleEt);
        descriptionEt = findViewById(R.id.descriptionEt);
        locationEt = findViewById(R.id.locationEt);
        contactEt = findViewById(R.id.contactEt);
        preview = findViewById(R.id.preview);
        saveBtn = findViewById(R.id.saveBtn);
        progress = findViewById(R.id.progress);
        headingTv = findViewById(R.id.headingTv);
        Button pickPhotoBtn = findViewById(R.id.pickPhotoBtn);
        Button takePhotoBtn = findViewById(R.id.takePhotoBtn);
        Button cancelBtn = findViewById(R.id.cancelBtn);

        auth = FirebaseAuth.getInstance();
        itemsRef = FirebaseDatabase.getInstance().getReference("items");

        editingId = getIntent().getStringExtra(EXTRA_ITEM_ID);
        if (editingId != null) {
            headingTv.setText(R.string.add_edit_edit_title);
            saveBtn.setText(R.string.update_item);
            loadItem(editingId);
        }

        pickPhotoBtn.setOnClickListener(v -> galleryLauncher.launch("image/*"));
        takePhotoBtn.setOnClickListener(v -> cameraLauncher.launch(null));
        saveBtn.setOnClickListener(v -> saveItem());
        cancelBtn.setOnClickListener(v -> finish());
    }

    private void onGalleryResult(Uri uri) {
        if (uri == null) {
            return;
        }
        try {
            Bitmap bitmap = ImageUtils.fromUri(this, uri);
            if (bitmap == null) {
                throw new IOException("Could not decode the image");
            }
            showPhoto(bitmap);
        } catch (IOException e) {
            Ui.snack(this, "Could not load photo: " + e.getMessage());
        }
    }

    private void onCameraResult(Bitmap bitmap) {
        if (bitmap != null) {
            showPhoto(bitmap);
        }
    }

    private void showPhoto(Bitmap bitmap) {
        imageBase64 = ImageUtils.toBase64(bitmap);
        preview.setImageBitmap(bitmap);
    }

    private void loadItem(String id) {
        setLoading(true);
        itemsRef.child(id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                setLoading(false);
                Item item = snapshot.getValue(Item.class);
                if (item == null) {
                    Toast.makeText(AddEditActivity.this, "This post no longer exists", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                String uid = auth.getUid();
                if (uid == null || !uid.equals(item.getOwnerUid())) {
                    Toast.makeText(AddEditActivity.this, "You can only edit your own posts", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                populate(item);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                setLoading(false);
                Ui.snack(AddEditActivity.this, "Could not load item: " + error.getMessage());
            }
        });
    }

    private void populate(Item item) {
        typeGroup.check("FOUND".equals(item.getType()) ? R.id.radioFound : R.id.radioLost);
        titleEt.setText(item.getTitle());
        descriptionEt.setText(item.getDescription());
        locationEt.setText(item.getLocation());
        contactEt.setText(item.getContact());

        imageBase64 = item.getImageBase64() == null ? "" : item.getImageBase64();
        Bitmap bitmap = ImageUtils.fromBase64(imageBase64);
        if (bitmap != null) {
            preview.setImageBitmap(bitmap);
        }
    }

    private void saveItem() {
        // Every check runs so all errors appear together
        boolean valid = Validator.requireText(titleEt, "Title is required");
        valid = Validator.requireText(descriptionEt, "Description is required") && valid;
        valid = Validator.requireText(locationEt, "Location is required") && valid;
        valid = Validator.requireText(contactEt, "Contact number is required") && valid;
        valid = valid && Validator.isValidPhone(contactEt);
        if (!valid) {
            return;
        }

        String uid = auth.getUid();
        if (uid == null) {
            Ui.snack(this, "You must be logged in to post");
            return;
        }

        String type = typeGroup.getCheckedRadioButtonId() == R.id.radioFound ? "FOUND" : "LOST";
        String title = Validator.text(titleEt);
        String description = Validator.text(descriptionEt);
        String location = Validator.text(locationEt);
        String contact = Validator.text(contactEt);

        setLoading(true);

        if (editingId == null) {
            // Create: push() generates a unique key for the new record
            DatabaseReference newRef = itemsRef.push();
            Item item = new Item();
            item.setId(newRef.getKey());
            item.setType(type);
            item.setTitle(title);
            item.setDescription(description);
            item.setLocation(location);
            item.setContact(contact);
            item.setImageBase64(imageBase64);
            item.setOwnerUid(uid);
            item.setCreatedAt(System.currentTimeMillis());

            newRef.setValue(item)
                    .addOnCompleteListener(this, task -> onSaved(task.isSuccessful()));
        } else {
            // Update: only the changed fields are sent, createdAt and ownerUid stay the same
            Map<String, Object> updates = new HashMap<>();
            updates.put("type", type);
            updates.put("title", title);
            updates.put("description", description);
            updates.put("location", location);
            updates.put("contact", contact);
            updates.put("imageBase64", imageBase64);

            itemsRef.child(editingId).updateChildren(updates)
                    .addOnCompleteListener(this, task -> onSaved(task.isSuccessful()));
        }
    }

    private void onSaved(boolean success) {
        setLoading(false);
        if (success) {
            Toast.makeText(this, "Saved successfully", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Ui.snack(this, "Could not save. Check your connection and try again.");
        }
    }

    private void setLoading(boolean loading) {
        progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        saveBtn.setEnabled(!loading);
    }
}
