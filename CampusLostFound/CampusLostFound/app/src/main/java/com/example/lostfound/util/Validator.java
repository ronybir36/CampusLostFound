package com.example.lostfound.util;

import android.util.Patterns;
import android.widget.EditText;

/**
 * Input checks used by the forms. Each method sets an error message on the
 * field and returns false when the input is wrong, so the app never crashes
 * on empty or malformed input.
 */
public final class Validator {

    private static final int MIN_PASSWORD_LENGTH = 6;  // Firebase's own minimum
    private static final String PHONE_PATTERN = "^\\+?[0-9 \\-]{7,15}$";

    private Validator() { }

    /** Trimmed text of a field, never null. */
    public static String text(EditText field) {
        return field.getText() == null ? "" : field.getText().toString().trim();
    }

    /** Raw password text. Passwords are not trimmed. */
    public static String rawText(EditText field) {
        return field.getText() == null ? "" : field.getText().toString();
    }

    public static boolean requireText(EditText field, String message) {
        if (text(field).isEmpty()) {
            field.setError(message);
            field.requestFocus();
            return false;
        }
        return true;
    }

    public static boolean isValidEmail(EditText field) {
        if (!Patterns.EMAIL_ADDRESS.matcher(text(field)).matches()) {
            field.setError("Enter a valid email address");
            field.requestFocus();
            return false;
        }
        return true;
    }

    public static boolean isValidPassword(EditText field) {
        if (rawText(field).length() < MIN_PASSWORD_LENGTH) {
            field.setError("Password must be at least " + MIN_PASSWORD_LENGTH + " characters");
            field.requestFocus();
            return false;
        }
        return true;
    }

    public static boolean passwordsMatch(EditText password, EditText confirm) {
        if (!rawText(password).equals(rawText(confirm))) {
            confirm.setError("Passwords do not match");
            confirm.requestFocus();
            return false;
        }
        return true;
    }

    public static boolean isValidPhone(EditText field) {
        if (!text(field).matches(PHONE_PATTERN)) {
            field.setError("Enter a valid phone number");
            field.requestFocus();
            return false;
        }
        return true;
    }
}
