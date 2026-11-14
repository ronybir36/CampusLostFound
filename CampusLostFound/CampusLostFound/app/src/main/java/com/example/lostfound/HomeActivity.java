package com.example.lostfound;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lostfound.adapter.ItemAdapter;
import com.example.lostfound.model.Item;
import com.example.lostfound.util.Ui;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Main screen. A live listener on the "items" node refreshes the list
 * whenever any user adds, edits or deletes a post. Tapping a card opens a
 * menu of actions: view, call, share, map, and edit/delete for the owner.
 */
public class HomeActivity extends AppCompatActivity {

    private static final String OPT_VIEW = "View details";
    private static final String OPT_CALL = "Call contact";
    private static final String OPT_SHARE = "Share";
    private static final String OPT_MAP = "Open location in Maps";
    private static final String OPT_EDIT = "Edit";
    private static final String OPT_DELETE = "Delete";

    private FirebaseAuth auth;
    private DatabaseReference itemsRef;
    private Query itemsQuery;
    private ValueEventListener itemsListener;
    private ItemAdapter adapter;
    private TextView emptyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            goToLogin();
            return;
        }

        itemsRef = FirebaseDatabase.getInstance().getReference("items");
        itemsQuery = itemsRef.orderByChild("createdAt");

        RecyclerView recycler = findViewById(R.id.recycler);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ItemAdapter(this::showItemOptions);
        recycler.setAdapter(adapter);

        emptyView = findViewById(R.id.emptyView);

        FloatingActionButton fab = findViewById(R.id.fabAdd);
        fab.setOnClickListener(v -> startActivity(new Intent(this, AddEditActivity.class)));

        itemsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Item> list = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Item item = child.getValue(Item.class);
                    if (item != null) {
                        item.setId(child.getKey());
                        list.add(item);
                    }
                }
                // The query sorts oldest first, so reverse it to show newest first
                Collections.reverse(list);
                adapter.setItems(list);
                emptyView.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Ui.snack(HomeActivity.this, "Could not load items: " + error.getMessage());
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (itemsQuery != null) {
            itemsQuery.addValueEventListener(itemsListener);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (itemsQuery != null) {
            itemsQuery.removeEventListener(itemsListener);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == R.id.action_logout) {
            auth.signOut();
            goToLogin();
            return true;
        }
        return super.onOptionsItemSelected(menuItem);
    }

    private void showItemOptions(Item item) {
        boolean isOwner = item.getOwnerUid() != null && item.getOwnerUid().equals(auth.getUid());

        List<String> options = new ArrayList<>();
        options.add(OPT_VIEW);
        if (!isBlank(item.getContact())) {
            options.add(OPT_CALL);
        }
        options.add(OPT_SHARE);
        if (!isBlank(item.getLocation())) {
            options.add(OPT_MAP);
        }
        if (isOwner) {
            options.add(OPT_EDIT);
            options.add(OPT_DELETE);
        }

        String[] labels = options.toArray(new String[0]);
        new AlertDialog.Builder(this)
                .setTitle(item.getTitle())
                .setItems(labels, (dialog, which) -> handleOption(labels[which], item))
                .setNegativeButton("Close", null)
                .show();
    }

    private void handleOption(String option, Item item) {
        switch (option) {
            case OPT_VIEW:
                showDetails(item);
                break;
            case OPT_CALL:
                Ui.startSafely(this, new Intent(Intent.ACTION_DIAL,
                        Uri.parse("tel:" + item.getContact())));
                break;
            case OPT_SHARE:
                shareItem(item);
                break;
            case OPT_MAP:
                Ui.startSafely(this, new Intent(Intent.ACTION_VIEW,
                        Uri.parse("geo:0,0?q=" + Uri.encode(item.getLocation()))));
                break;
            case OPT_EDIT:
                startActivity(new Intent(this, AddEditActivity.class)
                        .putExtra(AddEditActivity.EXTRA_ITEM_ID, item.getId()));
                break;
            case OPT_DELETE:
                confirmDelete(item);
                break;
            default:
                break;
        }
    }

    private void showDetails(Item item) {
        String message = "Type: " + ("LOST".equals(item.getType()) ? "Lost" : "Found")
                + "\nLocation: " + item.getLocation()
                + "\nContact: " + item.getContact()
                + "\n\n" + item.getDescription();

        new AlertDialog.Builder(this)
                .setTitle(item.getTitle())
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }

    private void shareItem(Item item) {
        String text = ("LOST".equals(item.getType()) ? "Lost" : "Found") + ": " + item.getTitle()
                + "\nWhere: " + item.getLocation()
                + "\nContact: " + item.getContact()
                + "\n\n" + item.getDescription();

        Intent send = new Intent(Intent.ACTION_SEND);
        send.setType("text/plain");
        send.putExtra(Intent.EXTRA_SUBJECT, item.getTitle());
        send.putExtra(Intent.EXTRA_TEXT, text);
        Ui.startSafely(this, Intent.createChooser(send, "Share via"));
    }

    private void confirmDelete(Item item) {
        new AlertDialog.Builder(this)
                .setTitle("Delete this post?")
                .setMessage("This cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) ->
                        itemsRef.child(item.getId()).removeValue()
                                .addOnCompleteListener(task -> Ui.snack(this,
                                        task.isSuccessful()
                                                ? "Post deleted"
                                                : "Delete failed. Please try again.")))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void goToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
