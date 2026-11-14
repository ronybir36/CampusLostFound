package com.example.lostfound.model;

/**
 * One Lost or Found post. Firebase Realtime Database maps each field
 * to/from the JSON key with the same name, so the getters and setters
 * must follow the standard naming pattern.
 */
public class Item {

    private String id;           // Firebase push key (set from the snapshot key)
    private String type;         // "LOST" or "FOUND"
    private String title;
    private String description;
    private String location;
    private String contact;      // phone number, used for ACTION_DIAL
    private String imageBase64;  // small compressed photo stored as text
    private String ownerUid;     // Firebase Auth uid of the person who posted it
    private long createdAt;      // epoch milliseconds, used for ordering

    public Item() {
        // Firebase needs an empty constructor to build objects from snapshots
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }

    public String getImageBase64() { return imageBase64; }
    public void setImageBase64(String imageBase64) { this.imageBase64 = imageBase64; }

    public String getOwnerUid() { return ownerUid; }
    public void setOwnerUid(String ownerUid) { this.ownerUid = ownerUid; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}
