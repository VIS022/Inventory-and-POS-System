// real real DBHelper

package com.example.posandinventorysystem;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Calendar;

public class DBHelper extends SQLiteOpenHelper {

    // Database info
    private static final String DATABASE_NAME = "ProductData.db";
    private static final int DATABASE_VERSION = 4; // Increment the version number

    // Table names
    private static final String TABLE_PRODUCT_DETAILS = "ProductDetails";
    private static final String TABLE_CATEGORIES = "Categories";

    // Common column names
    public static final String COLUMN_ID = "id";

    // ProductDetails table - column names
    private static final String COLUMN_PROD_NAME = "prodName";
    private static final String COLUMN_PROD_CATEGORY = "prodCate";
    public static final String COLUMN_PROD_PRICE = "prodPrice";
    public static final String COLUMN_PROD_QUANTITY = "prodQuantity";
    public static final String COLUMN_PROD_CAPITAL = "prodCapital";
    private static final String COLUMN_PROD_BARCODE = "prodBarcode";

    // Categories table - column names
    private static final String COLUMN_CATEGORY_NAME = "categoryName";
    public static final String COLUMN_PROD_PURCHASED = "prodPurchased";

    private static final String COLUMN_PROD_IMAGE_URI = "prodImageUri";

    private static final String TABLE_RECEIPTS = "Receipts";
    public static final String COLUMN_ITEM_QUANTITY = "itemQuantity";
    public static final String COLUMN_ITEM_PRICE = "itemPrice";
    private static final String COLUMN_PURCHASE_DATE = "purchaseDate";

    private static final String TABLE_EARNINGS_HISTORY = "EarningsHistory";
    public static final String COLUMN_EARNINGS = "earnings";
    public static final String COLUMN_CAPITAL = "capital";
    public static final String COLUMN_TOTAL = "total";
    public static final String COLUMN_MONTH = "month";
    public static final String COLUMN_YEAR = "year";


    // Define the CREATE_PRODUCT_DETAILS_TABLE query as a class member variable
    private static final String CREATE_PRODUCT_DETAILS_TABLE =
            "CREATE TABLE " + TABLE_PRODUCT_DETAILS + "(" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    COLUMN_PROD_NAME + " TEXT UNIQUE," +
                    COLUMN_PROD_CATEGORY + " TEXT," +
                    COLUMN_PROD_PRICE + " REAL," +
                    COLUMN_PROD_QUANTITY + " INTEGER," +
                    COLUMN_PROD_CAPITAL + " REAL," +
                    COLUMN_PROD_BARCODE + " TEXT," +
                    COLUMN_PROD_PURCHASED + " INTEGER," +
                    COLUMN_PROD_IMAGE_URI + " TEXT," +
                    "totalEarnings" + " REAL," +
                    "itemsPurchased" + " INTEGER" +
                    ")";


    // Define the CREATE_CATEGORIES_TABLE query
    private static final String CREATE_CATEGORIES_TABLE =
            "CREATE TABLE " + TABLE_CATEGORIES + "(" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    COLUMN_CATEGORY_NAME + " TEXT" +
                    ")";

    private static final String CREATE_RECEIPTS_TABLE =
            "CREATE TABLE " + TABLE_RECEIPTS + "(" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    COLUMN_ITEM_QUANTITY + " INTEGER," +
                    COLUMN_ITEM_PRICE + " INTEGER," +
                    COLUMN_PURCHASE_DATE + " TEXT" +
                    ")";

    private static final String CREATE_EARNINGS_HISTORY_TABLE =
            "CREATE TABLE " + TABLE_EARNINGS_HISTORY + "(" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    COLUMN_EARNINGS + " REAL," +
                    COLUMN_CAPITAL + " REAL," +
                    COLUMN_TOTAL + " REAL," +
                    COLUMN_MONTH + " INTEGER," +
                    COLUMN_YEAR + " INTEGER" +
                    ")";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Execute the CREATE_PRODUCT_DETAILS_TABLE query
        db.execSQL(CREATE_PRODUCT_DETAILS_TABLE);

        // Execute the CREATE_CATEGORIES_TABLE query
        db.execSQL(CREATE_CATEGORIES_TABLE);

        // Execute the CREATE_RECEIPTS_TABLE query
        db.execSQL(CREATE_RECEIPTS_TABLE);

        // Execute the CREATE_EARNINGS_HISTORY_TABLE query
        db.execSQL(CREATE_EARNINGS_HISTORY_TABLE);
    }



    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < newVersion) {
            // Drop existing tables and recreate them
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_PRODUCT_DETAILS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORIES);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECEIPTS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_EARNINGS_HISTORY);
            onCreate(db);
        }
    }

    // Method to insert product details
    public boolean insertProdDetails(String prodName, String prodCate, double prodPrice, int prodQuantity, String prodBar, double prodCapital, String imageUri) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_PROD_NAME, prodName);
        contentValues.put(COLUMN_PROD_CATEGORY, prodCate);
        contentValues.put(COLUMN_PROD_PRICE, prodPrice);
        contentValues.put(COLUMN_PROD_QUANTITY, prodQuantity);
        contentValues.put(COLUMN_PROD_BARCODE, prodBar);
        contentValues.put(COLUMN_PROD_CAPITAL, prodCapital);
        contentValues.put(COLUMN_PROD_IMAGE_URI, imageUri);
        contentValues.put(COLUMN_PROD_PURCHASED, 0); // Initialize purchased quantity to 0
        long result = db.insert(TABLE_PRODUCT_DETAILS, null, contentValues);
        db.close();
        return result != -1;
    }


    // Method to get all product details based on selected category, search query, sort order, and ascending flag
    public Cursor getAllProductDetails(String selectedCategory, String searchQuery, String sortBy, boolean ascending, boolean lowStockOnly, int stockValue) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] projection = {
                COLUMN_ID + " AS _id",
                COLUMN_PROD_NAME,
                COLUMN_PROD_CATEGORY,
                COLUMN_PROD_PRICE,
                COLUMN_PROD_QUANTITY,
                COLUMN_PROD_CAPITAL,
                COLUMN_PROD_BARCODE,
                COLUMN_PROD_PURCHASED, // Include the purchased quantity column
                COLUMN_PROD_IMAGE_URI,
                "(prodPurchased * prodPrice) - (prodPurchased * prodCapital) AS earnings"
        };
        String selection = "";
        ArrayList<String> selectionArgs = new ArrayList<>();

        if (!selectedCategory.isEmpty()) {
            selection += COLUMN_PROD_CATEGORY + "=?";
            selectionArgs.add(selectedCategory);
        }

        if (!searchQuery.isEmpty()) {
            if (!selection.isEmpty()) {
                selection += " AND ";
            }
            selection += COLUMN_PROD_NAME + " LIKE ?";
            selectionArgs.add("%" + searchQuery + "%");
        }

        if (lowStockOnly) {
            if (!selection.isEmpty()) {
                selection += " AND ";
            }
            selection += COLUMN_PROD_QUANTITY + " <= ?";
            selectionArgs.add(String.valueOf(stockValue));
        }

        String orderBy = "";
        if (sortBy != null && !sortBy.isEmpty()) {
            orderBy += sortBy;
            if (ascending) {
                orderBy += " ASC";
            } else {
                orderBy += " DESC";
            }
        }

        return db.query(TABLE_PRODUCT_DETAILS, projection, selection, selectionArgs.toArray(new String[0]), null, null, orderBy);
    }


    // Method to get product details by product name
    public ProductDetails getProductDetails(String productName) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_PRODUCT_DETAILS,
                new String[]{COLUMN_PROD_NAME, COLUMN_PROD_CATEGORY, COLUMN_PROD_PRICE, COLUMN_PROD_QUANTITY, COLUMN_PROD_CAPITAL, COLUMN_PROD_BARCODE, COLUMN_PROD_IMAGE_URI},
                COLUMN_PROD_NAME + "=?",
                new String[]{productName}, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            String prodName = cursor.getString(cursor.getColumnIndex(COLUMN_PROD_NAME));
            String prodCate = cursor.getString(cursor.getColumnIndex(COLUMN_PROD_CATEGORY));
            double prodPrice = cursor.getDouble(cursor.getColumnIndex(COLUMN_PROD_PRICE));
            int prodQuantity = cursor.getInt(cursor.getColumnIndex(COLUMN_PROD_QUANTITY));
            double prodCapital = cursor.getDouble(cursor.getColumnIndex(COLUMN_PROD_CAPITAL));
            String prodBarcode = cursor.getString(cursor.getColumnIndex(COLUMN_PROD_BARCODE));
            String imageUri = cursor.getString(cursor.getColumnIndex(COLUMN_PROD_IMAGE_URI));
            cursor.close();
            return new ProductDetails(prodName, prodCate, prodPrice, String.valueOf(prodQuantity), prodCapital, prodBarcode, imageUri);
        }
        return null;
    }



    public ArrayList<String> getCategories() {
        ArrayList<String> categories = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            // Query to retrieve unique product categories from the ProductDetails table
            String[] columns = {COLUMN_PROD_CATEGORY};
            cursor = db.query(true, TABLE_PRODUCT_DETAILS, columns, null, null, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    // Add each unique category to the ArrayList
                    String category = cursor.getString(cursor.getColumnIndex(COLUMN_PROD_CATEGORY));
                    categories.add(category);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("DBHelper", "Error retrieving categories: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
        return categories;
    }

    // Method to update product details in the database
    public boolean updateProduct(String oldProductName, String updatedName, String updatedCategory, double updatedPrice, int updatedQuantity, double updatedCapital, String updatedBarcode, String updatedImageUri) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_PROD_NAME, updatedName);
        values.put(COLUMN_PROD_CATEGORY, updatedCategory);
        values.put(COLUMN_PROD_PRICE, updatedPrice);
        values.put(COLUMN_PROD_QUANTITY, updatedQuantity);
        values.put(COLUMN_PROD_CAPITAL, updatedCapital);
        values.put(COLUMN_PROD_BARCODE, updatedBarcode);
        values.put(COLUMN_PROD_IMAGE_URI, updatedImageUri);
        int rowsAffected = db.update(TABLE_PRODUCT_DETAILS, values, COLUMN_PROD_NAME + "=?", new String[]{oldProductName});
        db.close();
        return rowsAffected > 0;
    }


    // Modify the insertCategory method to insert data into the "ProductDetails" table
    public boolean insertCategory(String categoryName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_CATEGORY_NAME, categoryName);
        long result = db.insert(TABLE_CATEGORIES, null, contentValues);
        db.close(); // Close the database connection
        return result != -1;
    }

    public ArrayList<String> getAllCategories() {
        ArrayList<String> categories = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            // Add "All Categories" as the first item in the list
            categories.add("All Categories");

            // Retrieve categories from the Categories table
            cursor = db.query(true, TABLE_CATEGORIES, new String[]{COLUMN_CATEGORY_NAME}, null, null, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                // Add each category to the ArrayList
                do {
                    String category = cursor.getString(cursor.getColumnIndex(COLUMN_CATEGORY_NAME));
                    categories.add(category);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("DBHelper", "Error retrieving categories: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
        return categories;
    }


    public boolean deleteCategory(String categoryName) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Check if any product is using this category
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_PRODUCT_DETAILS + " WHERE " + COLUMN_PROD_CATEGORY + " = ?", new String[]{categoryName});
        int count = cursor.getCount();
        cursor.close();

        // If category is not in use, delete it
        if (count == 0) {
            String whereClause = COLUMN_CATEGORY_NAME + "=?";
            String[] whereArgs = {categoryName};
            int rowsDeleted = db.delete(TABLE_CATEGORIES, whereClause, whereArgs);
            db.close();
            return rowsDeleted > 0;
        } else {
            // Category is still in use, do not delete
            db.close();
            return false;
        }
    }

    // Method to update category name
    public boolean updateCategory(String oldCategoryName, String newCategoryName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_CATEGORY_NAME, newCategoryName);

        // Specify the WHERE clause to update the specific category
        String selection = COLUMN_CATEGORY_NAME + "=?";
        String[] selectionArgs = {oldCategoryName};

        try {
            // Update the category in the database
            int rowsAffected = db.update(TABLE_CATEGORIES, values, selection, selectionArgs);
            return rowsAffected > 0;
        } catch (Exception e) {
            Log.e("DBHelper", "Error updating category: " + e.getMessage());
            return false;
        } finally {
            db.close();
        }
    }


    public boolean deleteProduct(String productName) {
        SQLiteDatabase db = this.getWritableDatabase();
        String whereClause = COLUMN_PROD_NAME + "=?";
        String[] whereArgs = {productName};
        int rowsDeleted = db.delete(TABLE_PRODUCT_DETAILS, whereClause, whereArgs);
        db.close();
        return rowsDeleted > 0;
    }
    public boolean updateProductQuantityAndPurchased(String productName, int purchasedQuantity) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.query(TABLE_PRODUCT_DETAILS,
                new String[]{COLUMN_PROD_QUANTITY, COLUMN_PROD_PURCHASED},
                COLUMN_PROD_NAME + "=?",
                new String[]{productName}, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            int currentQuantity = cursor.getInt(cursor.getColumnIndex(COLUMN_PROD_QUANTITY));
            int currentPurchased = cursor.getInt(cursor.getColumnIndex(COLUMN_PROD_PURCHASED));

            int newQuantity = currentQuantity - purchasedQuantity;
            int newPurchased = currentPurchased + purchasedQuantity;

            ContentValues contentValues = new ContentValues();
            contentValues.put(COLUMN_PROD_QUANTITY, newQuantity);
            contentValues.put(COLUMN_PROD_PURCHASED, newPurchased);

            int rowsAffected = db.update(TABLE_PRODUCT_DETAILS, contentValues, COLUMN_PROD_NAME + "=?", new String[]{productName});
            cursor.close();
            db.close();
            return rowsAffected > 0;
        }
        if (cursor != null) {
            cursor.close();
        }
        db.close();
        return false;
    }

    // DBHelper.java
    public int getProductQuantity(String productName) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] projection = {COLUMN_PROD_QUANTITY};
        String selection = COLUMN_PROD_NAME + " = ?";
        String[] selectionArgs = {productName};

        Cursor cursor = db.query(
                TABLE_PRODUCT_DETAILS,   // The table to query
                projection,              // The columns to return
                selection,               // The columns for the WHERE clause
                selectionArgs,           // The values for the WHERE clause
                null,                    // Don't group the rows
                null,                    // Don't filter by row groups
                null                     // The sort order
        );

        if (cursor != null && cursor.moveToFirst()) {
            int quantity = cursor.getInt(cursor.getColumnIndex(COLUMN_PROD_QUANTITY));
            cursor.close();
            return quantity;
        } else {
            return 0;
        }
    }

    public void resetTotalEarningsAndItemsPurchased() {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("totalEarnings", 0.0);
        values.put("itemsPurchased", 0);
        db.update(TABLE_PRODUCT_DETAILS, values, null, null);
        db.close();
    }


    public void resetEarnings() {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_PROD_PURCHASED, 0);
        db.update(TABLE_PRODUCT_DETAILS, values, null, null);
        db.close();
    }

    public void insertEarningsHistory(double earnings, double capital, double total) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Check if a record already exists for the current month and year
        Calendar calendar = Calendar.getInstance();
        int currentMonth = calendar.get(Calendar.MONTH);
        int currentYear = calendar.get(Calendar.YEAR);

        String[] projection = {COLUMN_ID};
        String selection = COLUMN_MONTH + "=? AND " + COLUMN_YEAR + "=?";
        String[] selectionArgs = {String.valueOf(currentMonth), String.valueOf(currentYear)};

        Cursor cursor = db.query(TABLE_EARNINGS_HISTORY, projection, selection, selectionArgs, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            // Record exists, update the existing record
            int id = cursor.getInt(cursor.getColumnIndex(COLUMN_ID));
            ContentValues values = new ContentValues();
            values.put(COLUMN_EARNINGS, earnings);
            values.put(COLUMN_CAPITAL, capital);
            values.put(COLUMN_TOTAL, total);
            db.update(TABLE_EARNINGS_HISTORY, values, COLUMN_ID + "=?", new String[]{String.valueOf(id)});
        } else {
            // Record doesn't exist, insert a new record
            ContentValues values = new ContentValues();
            values.put(COLUMN_EARNINGS, earnings);
            values.put(COLUMN_CAPITAL, capital);
            values.put(COLUMN_TOTAL, total);
            values.put(COLUMN_MONTH, currentMonth);
            values.put(COLUMN_YEAR, currentYear);
            db.insert(TABLE_EARNINGS_HISTORY, null, values);
        }

        if (cursor != null) {
            cursor.close();
        }
        db.close();
    }



    public Cursor getEarningsHistory() {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {
                COLUMN_ID,
                COLUMN_EARNINGS,
                COLUMN_CAPITAL,
                COLUMN_TOTAL,
                COLUMN_MONTH,
                COLUMN_YEAR
        };
        return db.query(TABLE_EARNINGS_HISTORY, columns, null, null, null, null, null);
    }

    public int deleteEarningsHistory(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_EARNINGS_HISTORY, COLUMN_ID + "=?", new String[]{String.valueOf(id)});
    }
    public boolean updateProductQuantityAndPurchased(String productName, int newQuantity, int newPurchased) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_PROD_QUANTITY, newQuantity);
        contentValues.put(COLUMN_PROD_PURCHASED, newPurchased);

        int rowsAffected = db.update(TABLE_PRODUCT_DETAILS, contentValues, COLUMN_PROD_NAME + "=?", new String[]{productName});
        db.close();
        return rowsAffected > 0;
    }

    public Cursor getLatestEarningsHistory() {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {
                COLUMN_EARNINGS,
                COLUMN_CAPITAL
        };
        return db.query(TABLE_EARNINGS_HISTORY, columns, null, null, null, null, COLUMN_ID + " DESC", "1");
    }


}
