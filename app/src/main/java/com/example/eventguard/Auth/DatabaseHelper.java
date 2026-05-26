package com.example.eventguard.Auth;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.eventguard.models.User;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "EventGuard.db";
    private static final int DATABASE_VERSION = 2;

    private static final String TABLE_USER = "users";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_UID = "uid";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_EMAIL = "email";
    private static final String COLUMN_ROLE = "role";
    private static final String COLUMN_BIO = "bio";
    private static final String COLUMN_PHONE = "phone";
    private static final String COLUMN_COUNTRY = "country";
    private static final String COLUMN_PROFILE_PIC = "profile_pic";
    private static final String COLUMN_JOINED_DATE = "joined_date";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_USER_TABLE = "CREATE TABLE " + TABLE_USER + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_UID + " TEXT UNIQUE,"
                + COLUMN_NAME + " TEXT,"
                + COLUMN_EMAIL + " TEXT,"
                + COLUMN_ROLE + " TEXT,"
                + COLUMN_BIO + " TEXT,"
                + COLUMN_PHONE + " TEXT,"
                + COLUMN_COUNTRY + " TEXT,"
                + COLUMN_PROFILE_PIC + " TEXT,"
                + COLUMN_JOINED_DATE + " INTEGER" + ")";
        db.execSQL(CREATE_USER_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE " + TABLE_USER + " ADD COLUMN " + COLUMN_JOINED_DATE + " INTEGER DEFAULT 0");
        }
    }

    public void saveUser(User user, String uid) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_UID, uid);
        values.put(COLUMN_NAME, user.name);
        values.put(COLUMN_EMAIL, user.email);
        values.put(COLUMN_ROLE, user.role);
        values.put(COLUMN_BIO, user.bio);
        values.put(COLUMN_PHONE, user.phone);
        values.put(COLUMN_COUNTRY, user.country);
        values.put(COLUMN_PROFILE_PIC, user.profilePic);
        values.put(COLUMN_JOINED_DATE, user.joinedDate);

        db.insertWithOnConflict(TABLE_USER, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
    }

    public User getUser(String uid) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USER, null, COLUMN_UID + "=?", new String[]{uid}, null, null, null);
        
        if (cursor != null && cursor.moveToFirst()) {
            User user = new User();
            user.name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME));
            user.email = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL));
            user.role = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ROLE));
            user.bio = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BIO));
            user.phone = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PHONE));
            user.country = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COUNTRY));
            user.profilePic = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PROFILE_PIC));
            user.joinedDate = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_JOINED_DATE));
            cursor.close();
            return user;
        }
        if (cursor != null) cursor.close();
        return null;
    }
}
