package com.live2d.demo.schedule;


import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "calendar.db";  // 데이터베이스 이름
    private static final int DATABASE_VERSION = 1;  // 데이터베이스 버전
    private static final String TABLE_USER = "user";  // 사용자 테이블 이름
    private static final String TABLE_CALENDAR = "calendar";  // 일정 테이블 이름

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // 데이터베이스가 처음 생성될 때 호출되는 메서드
    @Override
    public void onCreate(SQLiteDatabase db) {
        String createUserTable = "CREATE TABLE IF NOT EXISTS " + TABLE_USER + " (" +
                "user_id TEXT PRIMARY KEY, " +
                "password TEXT NOT NULL, " +
                "name TEXT NOT NULL, " +
                "birth TEXT NOT NULL" +
                ");";

        String createCalendarTable = "CREATE TABLE IF NOT EXISTS " + TABLE_CALENDAR + " (" +
                "cal_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id TEXT NOT NULL, " +
                "event_name TEXT NOT NULL, " +
                "event_date TEXT NOT NULL, " +
                "FOREIGN KEY (user_id) REFERENCES " + TABLE_USER + "(user_id)" +
                ");";

        // 테이블 생성 쿼리 실행
        db.execSQL(createUserTable);
        db.execSQL(createCalendarTable);
    }

    // 데이터베이스 버전이 변경되었을 때 호출되는 메서드
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CALENDAR);
        onCreate(db);
    }

    // 일정 추가
    public boolean addEvent(String userId, String eventName, String eventDate) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("user_id", userId);
        values.put("event_name", eventName);
        values.put("event_date", eventDate);

        long result = db.insert(TABLE_CALENDAR, null, values);
        db.close();

        return result != -1;
    }
}
