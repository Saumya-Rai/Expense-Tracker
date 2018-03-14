package com.example.savss.expensetracker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class LocalDatabaseHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;

    private static final String DATABASE_NAME = "expensetrakerDB.db";
    private static final String TABLE_USERS = "users";
    private static final String TABLE_CATEGORY = "categories";
    private static final String TABLE_TRANSACTION = "transactions";

    public static final String USERS_ID = "user_id";
    public static final String USERS_NAME = "name";
    public static final String USERS_EMAIL = "email";
    public static final String USERS_PHONENUMBER = "phonenumber";
    public static final String USERS_PASSWORD = "password";

    public static final String CATEGORY_ID = "category_id";
    public static final String CATEGORY_NAME = "name";

    public static final String TRANSACTION_ID = "transaction_id";
    public static final String TRANSACTION_FKEY_USERS_ID = "user_id";
    public static final String TRANSACTION_DATE = "tdate";
    public static final String TRANSACTION_FKEY_CATEGORY_ID = "category_id";
    public static final String TRANSACTION_TYPE = "type";
    public static final String TRANSACTION_AMOUNT = "amount";
    public static final String TRANSACTION_DESCRIPTION = "description";

    public LocalDatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, DATABASE_NAME, factory, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String userTableCreationQuery = String.format("CREATE TABLE %s (%s INTEGER PRIMARY KEY AUTOINCREMENT, %s TEXT, %s TEXT, %s TEXT, %s TEXT);",
                TABLE_USERS, USERS_ID, USERS_NAME, USERS_EMAIL, USERS_PHONENUMBER, USERS_PASSWORD);
        String categoryTableCreationQuery = String.format("CREATE TABLE %s (%s INTEGER PRIMARY KEY AUTOINCREMENT, %s TEXT);",
                TABLE_CATEGORY, CATEGORY_ID, CATEGORY_NAME);

        String transactionTableCreationQuery = String.format("CREATE TABLE %s (%s INTEGER PRIMARY KEY AUTOINCREMENT, %s INTEGER, %s DATETIME, %s INTEGER, %s TEXT, %s INTEGER, %s TEXT, FOREIGN KEY(%s) REFERENCES %s(%s), FOREIGN KEY (%s) REFERENCES %s(%s));",
                TABLE_TRANSACTION, TRANSACTION_ID, TRANSACTION_FKEY_USERS_ID, TRANSACTION_DATE, TRANSACTION_FKEY_CATEGORY_ID, TRANSACTION_TYPE, TRANSACTION_AMOUNT, TRANSACTION_DESCRIPTION, TRANSACTION_FKEY_USERS_ID, TABLE_USERS, USERS_ID, TRANSACTION_FKEY_CATEGORY_ID, TABLE_CATEGORY, CATEGORY_ID);

        sqLiteDatabase.execSQL(userTableCreationQuery);
        sqLiteDatabase.execSQL(categoryTableCreationQuery);
        sqLiteDatabase.execSQL(transactionTableCreationQuery);
    }

    public boolean tryAddUser(String name, String email, String phoneNumber, String password) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(USERS_NAME, name);
        contentValues.put(USERS_EMAIL, email);
        contentValues.put(USERS_PHONENUMBER, phoneNumber);
        contentValues.put(USERS_PASSWORD, password);
        if (isExisting(phoneNumber)) {
            return false;
        }
        else {
            SQLiteDatabase sqLiteDatabase = getWritableDatabase();
            sqLiteDatabase.insert(TABLE_USERS, null, contentValues);
            sqLiteDatabase.close();
            return true;
        }
    }

    private boolean isExisting(String phoneNumber){
        String checkQuery = String.format("SELECT * FROM %s WHERE %s = '%s'", TABLE_USERS, USERS_PHONENUMBER, phoneNumber);
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery(checkQuery, null);
        if (cursor.getCount() == 0) {
            sqLiteDatabase.close();
            return false;
        }
        else {
            sqLiteDatabase.close();
            return true;
        }
    }

    public String getPassword(String loginID, IDType idType) {
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();

        String getPasswordQuery = "";

        if (idType == IDType.Email) {
            getPasswordQuery = String.format("SELECT %s FROM %s WHERE %s = '%s'", USERS_PASSWORD, TABLE_USERS, USERS_EMAIL, loginID);
        }
        else if (idType == IDType.PhoneNumber) {
            getPasswordQuery = String.format("SELECT %s FROM %s WHERE %s = '%s'", USERS_PASSWORD, TABLE_USERS, USERS_PHONENUMBER, loginID);
        }

        Cursor cursor = sqLiteDatabase.rawQuery(getPasswordQuery, null);
        String password = "";

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            if (cursor.getString(cursor.getColumnIndex(USERS_PASSWORD)) != null) {
                password = cursor.getString(cursor.getColumnIndex(USERS_PASSWORD));
            }
            cursor.moveToNext();
        }
        sqLiteDatabase.close();

        return password;
    }

    public int getUserID(String loginID) {
        return getUserID(loginID, IDType.Email);
    }

    public int getUserID(String loginID, IDType idType) {
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();

        String getUserIDQuery = "";

        if (idType == IDType.Email) {
            getUserIDQuery = String.format("SELECT %s FROM %s WHERE %s = '%s'", USERS_ID, TABLE_USERS, USERS_EMAIL, loginID);
        }
        else if (idType == IDType.PhoneNumber) {
            getUserIDQuery = String.format("SELECT %s FROM %s WHERE %s = '%s'", USERS_ID, TABLE_USERS, USERS_PHONENUMBER, loginID);
        }

        Cursor cursor = sqLiteDatabase.rawQuery(getUserIDQuery, null);
        String userID = "";

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            if (cursor.getString(cursor.getColumnIndex(USERS_ID)) != null) {
                userID = cursor.getString(cursor.getColumnIndex(USERS_ID));
            }
            cursor.moveToNext();
        }
        sqLiteDatabase.close();

        return Integer.parseInt(userID);
    }

    public ExpenseData getTodaysExpenses(int userID) {
        ExpenseData ed = new ExpenseData();
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        TransactionType tType = TransactionType.Income;
        String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        String fetchDataQuery = String.format("SELECT SUM(%s), %s FROM %s, %s WHERE %s != '%s' AND %s.%s = %s.%s AND %s = %s AND %s = '%s' GROUP BY (%s.%s);",
                                                TRANSACTION_AMOUNT, CATEGORY_NAME, TABLE_TRANSACTION, TABLE_CATEGORY, TRANSACTION_TYPE, tType.toString(),
                                                TABLE_CATEGORY, CATEGORY_ID, TABLE_TRANSACTION, TRANSACTION_FKEY_CATEGORY_ID, TRANSACTION_FKEY_USERS_ID,
                                                userID, TRANSACTION_DATE, "2018-02-22", TABLE_TRANSACTION, TRANSACTION_FKEY_CATEGORY_ID);
        System.out.println(fetchDataQuery);
        try {
            sqLiteDatabase.execSQL("insert into categories values (1, 'cat1');");
            sqLiteDatabase.execSQL("insert into categories values (2, 'cat2');");
            sqLiteDatabase.execSQL("insert into transactions values(1, 1, '2018-02-22', 1, 'expense', 1000, 'another');");
            sqLiteDatabase.execSQL("insert into transactions values(2, 1, '2018-02-22', 1, 'income', 1000, 'another');");
            sqLiteDatabase.execSQL("insert into transactions values(3, 1, '2018-02-22', 1, 'expense', 2000, 'asdf');");
            sqLiteDatabase.execSQL("insert into transactions values(4, 1, '2018-02-22', 2, 'expense', 4000, 'asdf');");
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        Cursor c = sqLiteDatabase.rawQuery(fetchDataQuery, null);
        c.moveToFirst();
        while (!c.isAfterLast()) {
            ed.add(c.getString(1), c.getInt(0));
            c.moveToNext();
        }
        sqLiteDatabase.close();
        getLastMonthExpenses(1);
        return ed;
    }

    public ExpenseData getLastMonthExpenses(int userID) {
        ExpenseData ed = new ExpenseData();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

        Calendar calendar = Calendar.getInstance();
        Date currentDate = calendar.getTime();
        calendar.add(Calendar.MONTH, -1);

        String strCurrentDate = simpleDateFormat.format(currentDate);
        strCurrentDate = strCurrentDate.substring(0, strCurrentDate.length() - 2) + "01";

        System.out.println(strCurrentDate);
        TransactionType tType = TransactionType.Expense;
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        String fetchQuery = String.format("select sum(%s), %s from %s, %s where %s = '%s' and %s.%s = %s.%s and %s = %s and %s > '%s' group by(%s.%s);",
                                            TRANSACTION_AMOUNT, CATEGORY_NAME, TABLE_TRANSACTION, TABLE_CATEGORY, TRANSACTION_TYPE, tType.toString(), TABLE_CATEGORY,
                                            CATEGORY_ID, TABLE_TRANSACTION, TRANSACTION_FKEY_CATEGORY_ID, TRANSACTION_FKEY_USERS_ID, userID, TRANSACTION_DATE,
                                            strCurrentDate, TABLE_TRANSACTION, TRANSACTION_FKEY_CATEGORY_ID);
        System.out.println(fetchQuery);
        Cursor c = sqLiteDatabase.rawQuery(fetchQuery, null);
        c.moveToFirst();
        while (!c.isAfterLast()) {
            ed.add(c.getString(1), c.getInt(0));
            c.moveToNext();
        }
        sqLiteDatabase.close();
        return ed;
    }

    public void setUserData(int userID) {
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        String tempAddr = "221 Baker Street";
        String DOB = "03-06-1997";
        String fetchQuery = String.format("select %s, %s, %s, %s from %s where %s = %s;",
                USERS_NAME, USERS_EMAIL, USERS_PHONENUMBER, USERS_PASSWORD, TABLE_USERS, USERS_ID, String.valueOf(userID));
        Cursor c = sqLiteDatabase.rawQuery(fetchQuery, null);
        c.moveToFirst();
        while(!c.isAfterLast()) {
            UserData.address = tempAddr;
            UserData.userID = userID;
            UserData.dateOfBirth = DOB;
            UserData.Name = c.getString(0);
            UserData.email = c.getString(1);
            UserData.phoneNumber = c.getString(2);
            UserData.password = c.getString(3);
            c.moveToNext();
        }

        fetchQuery = String.format("select * from %s", TABLE_CATEGORY);
        c = sqLiteDatabase.rawQuery(fetchQuery, null);
        c.moveToFirst();
        ArrayList<String> categories = new ArrayList<>();
        while (!c.isAfterLast()) {
            categories.add(c.getString(1));
            c.moveToNext();
        }
        UserData.categories = categories;
        sqLiteDatabase.close();
    }
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        String dropTableQuery = "DROP TABLE IF EXISTS " + TABLE_USERS;
        sqLiteDatabase.execSQL(dropTableQuery);
        onCreate(sqLiteDatabase);
    }

    public void addTransaction(String userID, int categoryID, String transactionType, String amount, String description, Date tdate) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String date = simpleDateFormat.format(tdate);
        ContentValues contentValues = new ContentValues();
        contentValues.put(TRANSACTION_FKEY_USERS_ID, userID);
        contentValues.put(TRANSACTION_DATE, date);
        contentValues.put(TRANSACTION_FKEY_CATEGORY_ID, String.valueOf(categoryID));
        contentValues.put(TRANSACTION_TYPE, transactionType);
        contentValues.put(TRANSACTION_AMOUNT, amount);
        contentValues.put(TRANSACTION_DESCRIPTION, description);
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        sqLiteDatabase.insert(TABLE_TRANSACTION, null, contentValues);
        sqLiteDatabase.close();

    }

    public ArrayList<TransactionData> getTransactionData(int id, Date fromDate, Date toDate) {
        //id, amount, dateTime, category, desc
        ArrayList<TransactionData> transactionData = new ArrayList<>();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String strFromDate = simpleDateFormat.format(fromDate);
        String strToDate = simpleDateFormat.format(toDate);
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        String fetchQuery = String.format("select %s.%s, %s.%s, %s.%s, %s.%s, %s.%s, %s.%s from %s, %s where %s.%s = %s.%s and %s.%s = %s and %s.%s between '%s' and '%s';",
                TABLE_TRANSACTION, TRANSACTION_ID, TABLE_TRANSACTION, TRANSACTION_AMOUNT, TABLE_TRANSACTION, TRANSACTION_DATE, TABLE_CATEGORY, CATEGORY_NAME, TABLE_TRANSACTION, TRANSACTION_DESCRIPTION,
                TABLE_TRANSACTION, TRANSACTION_TYPE, TABLE_TRANSACTION, TABLE_CATEGORY, TABLE_TRANSACTION, TRANSACTION_FKEY_CATEGORY_ID, TABLE_CATEGORY, CATEGORY_ID, TABLE_TRANSACTION,
                TRANSACTION_FKEY_USERS_ID, String.valueOf(id), TABLE_TRANSACTION, TRANSACTION_DATE, strFromDate, strToDate);
        System.out.println(fetchQuery);
        Cursor c = sqLiteDatabase.rawQuery(fetchQuery, null);
        c.moveToFirst();
        while (!c.isAfterLast()) {
            transactionData.add(new TransactionData(Integer.parseInt(c.getString(0)), c.getString(1), c.getString(2), c.getString(3), c.getString(4), c.getString(5)));
            c.moveToNext();
        }
        sqLiteDatabase.close();
        return transactionData;
    }
}
