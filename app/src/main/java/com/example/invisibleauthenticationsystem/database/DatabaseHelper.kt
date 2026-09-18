package com.example.invisibleauthenticationsystem.database
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.invisibleauthenticationsystem.models.Document
import com.example.invisibleauthenticationsystem.models.User

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "InvisibleAuth.db"
        private const val DATABASE_VERSION = 5

        const val TABLE_USERS = "users"
        const val COLUMN_USER_ID = "id"
        const val COLUMN_FULL_NAME = "full_name"
        const val COLUMN_EMAIL = "email"
        const val COLUMN_PHONE = "phone"
        const val COLUMN_PASSWORD = "password"
        const val COLUMN_CALCULATOR_PIN = "calculator_pin"
        const val COLUMN_DWELL_TIME = "dwell_time"
        const val COLUMN_FLIGHT_TIME = "flight_time"
        const val COLUMN_TOUCH_PRESSURE = "touch_pressure"
        const val COLUMN_BIOMETRIC_ENABLED = "biometric_enabled"

        const val TABLE_DOCUMENTS = "documents"
        const val COLUMN_DOC_ID = "doc_id"
        const val COLUMN_DOC_USER_EMAIL = "user_email"
        const val COLUMN_DOC_TITLE = "title"
        const val COLUMN_DOC_URI = "uri"
        const val COLUMN_DOC_MIME = "mime_type"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createUsersTable = ("CREATE TABLE " + TABLE_USERS + "("
                + COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_FULL_NAME + " TEXT,"
                + COLUMN_EMAIL + " TEXT UNIQUE,"
                + COLUMN_PHONE + " TEXT UNIQUE,"
                + COLUMN_PASSWORD + " TEXT,"
                + COLUMN_CALCULATOR_PIN + " TEXT,"
                + COLUMN_DWELL_TIME + " REAL,"
                + COLUMN_FLIGHT_TIME + " REAL,"
                + COLUMN_TOUCH_PRESSURE + " REAL,"
                + COLUMN_BIOMETRIC_ENABLED + " INTEGER DEFAULT 0" + ")")

                
        val createDocumentsTable = ("CREATE TABLE " + TABLE_DOCUMENTS + "("
                + COLUMN_DOC_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_DOC_USER_EMAIL + " TEXT,"
                + COLUMN_DOC_TITLE + " TEXT,"
                + COLUMN_DOC_URI + " TEXT,"
                + COLUMN_DOC_MIME + " TEXT" + ")")
                
        db.execSQL(createUsersTable)
        db.execSQL(createDocumentsTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS)
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DOCUMENTS)
        onCreate(db)
    }

    fun registerUser(user: User, passwordHash: String): Boolean {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(COLUMN_FULL_NAME, user.fullName)
        values.put(COLUMN_EMAIL, user.email)
        values.put(COLUMN_PHONE, user.phone)
        values.put(COLUMN_PASSWORD, passwordHash)
        values.put(COLUMN_CALCULATOR_PIN, user.calculatorPin)
        values.put(COLUMN_DWELL_TIME, user.avgDwellTime)
        values.put(COLUMN_FLIGHT_TIME, user.avgFlightTime)
        values.put(COLUMN_TOUCH_PRESSURE, user.touchPressure)
        values.put(COLUMN_BIOMETRIC_ENABLED, if (user.isBiometricEnabled) 1 else 0)


        val success = db.insert(TABLE_USERS, null, values)
        db.close()
        return (Integer.parseInt("$success") != -1)
    }

    fun getUser(email: String): User? {
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_USERS,
            arrayOf(COLUMN_USER_ID, COLUMN_FULL_NAME, COLUMN_EMAIL, COLUMN_PHONE, COLUMN_CALCULATOR_PIN, COLUMN_DWELL_TIME, COLUMN_FLIGHT_TIME, COLUMN_TOUCH_PRESSURE, COLUMN_BIOMETRIC_ENABLED),

            "$COLUMN_EMAIL=?",
            arrayOf(email), null, null, null, null
        )

        var user: User? = null
        if (cursor != null && cursor.moveToFirst()) {
            user = User(
                id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_USER_ID)),
                fullName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FULL_NAME)),
                email = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL)),
                phone = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PHONE)),
                calculatorPin = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CALCULATOR_PIN)),
                avgDwellTime = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_DWELL_TIME)),
                avgFlightTime = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_FLIGHT_TIME)),
                touchPressure = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_TOUCH_PRESSURE)),
                isBiometricEnabled = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_BIOMETRIC_ENABLED)) == 1
            )

        }
        cursor?.close()
        return user
    }
    
    fun checkUserExists(email: String, phone: String): Boolean {
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_USERS,
            arrayOf(COLUMN_USER_ID),
            "$COLUMN_EMAIL=? OR $COLUMN_PHONE=?",
            arrayOf(email, phone), null, null, null, null
        )
        val exists = (cursor != null && cursor.count > 0)
        cursor?.close()
        return exists
    }

    fun checkPinExists(pin: String): Boolean {
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_USERS,
            arrayOf(COLUMN_USER_ID),
            "$COLUMN_CALCULATOR_PIN=?",
            arrayOf(pin), null, null, null, null
        )
        val exists = (cursor != null && cursor.count > 0)
        cursor?.close()
        return exists
    }

    fun insertDocument(userEmail: String, document: Document): Boolean {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(COLUMN_DOC_USER_EMAIL, userEmail)
        values.put(COLUMN_DOC_TITLE, document.title)
        values.put(COLUMN_DOC_URI, document.uriString)
        values.put(COLUMN_DOC_MIME, document.mimeType)

        val success = db.insert(TABLE_DOCUMENTS, null, values)
        db.close()
        return (Integer.parseInt("$success") != -1)
    }

    fun getDocumentsForUser(userEmail: String): List<Document> {
        val docList = mutableListOf<Document>()
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_DOCUMENTS,
            arrayOf(COLUMN_DOC_ID, COLUMN_DOC_TITLE, COLUMN_DOC_URI, COLUMN_DOC_MIME),
            "$COLUMN_DOC_USER_EMAIL=?",
            arrayOf(userEmail), null, null, null, null
        )

        if (cursor != null && cursor.moveToFirst()) {
            do {
                val doc = Document(
                    id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_DOC_ID)),
                    title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DOC_TITLE)),
                    uriString = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DOC_URI)),
                    mimeType = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DOC_MIME))
                )
                docList.add(doc)
            } while (cursor.moveToNext())
        }
        cursor?.close()
        return docList
    }

    fun deleteDocument(docId: Long): Boolean {
        val db = this.writableDatabase
        val result = db.delete(TABLE_DOCUMENTS, "$COLUMN_DOC_ID=?", arrayOf(docId.toString()))
        db.close()
        return result > 0
    }
}
