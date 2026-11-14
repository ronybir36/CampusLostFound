# Campus Lost and Found

An Android app where students post lost or found items on campus. Anyone who is logged in can see the list in real time, call the poster, share the post, or open the location in Maps.

- **Course:** Mobile Application and Development Lab (0613-410)
- **Student name:** <your name>
- **Student ID:** <your ID>

## Features

| # | Requirement | Where it is in the code |
|---|---|---|
| 1 | 5+ screens with navigation | `SplashActivity`, `LoginActivity`, `RegisterActivity`, `HomeActivity`, `AddEditActivity` (Intents, `putExtra` for the item ID) |
| 2 | Layouts and basic views | `ConstraintLayout`, `LinearLayout`, `TextView`, `ImageView`, `Button`, `CardView` in `res/layout/` |
| 3 | Input validation | `util/Validator.java` (`TextInputEditText` + `setError()`) |
| 4 | Model class | `model/Item.java` |
| 5 | Scrolling list | `RecyclerView` + `adapter/ItemAdapter.java` (ViewHolder) |
| 6 | CRUD with dialog and FAB | Create/Edit in `AddEditActivity`, FAB in `activity_home.xml`, `AlertDialog` for actions and delete confirm in `HomeActivity` |
| 7 | Firebase login, logout, stay logged in | `FirebaseAuth` in Login, Register, Splash and Home |
| 8 | Firebase database, real-time list | Realtime Database `items` node, `ValueEventListener` in `HomeActivity` |
| 9 | Save a setting on the phone | `SharedPreferences` in `util/PrefsManager.java` (remember me, last email) |
| 10 | Take or pick a photo | `TakePicturePreview` (camera) and `GetContent` (gallery) in `AddEditActivity`, stored as Base64 |
| 11 | Implicit intents | `ACTION_DIAL` (call), `ACTION_SEND` (share), `ACTION_VIEW` with `geo:` (map) in `HomeActivity` |

Also included: a custom app name, a Material 3 theme, and Snackbar/Toast messages.

## Firebase database structure

```
users/
  {uid}/
    name: "..."
    studentId: "..."
    email: "..."
items/
  {itemId}/
    type: "LOST" | "FOUND"
    title, description, location, contact: "..."
    imageBase64: "..."        (small compressed JPEG)
    ownerUid: "{uid}"
    createdAt: 1731600000000  (epoch ms, used for ordering)
```

Photos are stored in the database as Base64 text, not in Firebase Storage. Storage needs a paid plan for new projects, and the images are shrunk to about 480 px, so each record stays small.

## Setup

1. Create a Firebase project and add an Android app with the package name `com.example.lostfound` (or change it in `app/build.gradle` and the Java package names).
2. In **Authentication → Sign-in method**, enable **Email/Password**.
3. In **Realtime Database**, create a database and paste the contents of `database.rules.json` into the Rules tab.
4. Download `google-services.json` from Firebase and copy it into `app/`. It is not included in this repository.
5. Open the folder in Android Studio, let Gradle sync, then run the app. For a release APK, use **Build → Build APK(s)**.
6. Generate the launcher icon with **File → New → Image Asset**.

## Notes

- The Gemini agent in Android Studio was not used to write this code. Read each file before the viva.
- Shared posts are visible to every logged-in user. Only the owner can edit or delete a post, and the database rules enforce this.
