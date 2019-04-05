package donggolf.android.base;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import donggolf.android.models.ImagesPath;
import donggolf.android.models.TmpContent;

public class DataBaseHelper extends SQLiteOpenHelper {

    private final Context myContext;

    // The Android's default system path of your application database.
    private static String DB_PATH =  "/data/data/tlg.MogaIT2/databases/";

    private static String DB_NAME = "moga";

    private SQLiteDatabase myDataBase;

    /**
     * Constructor
     * Takes and keeps a reference of the passed context in order to access to the application assets and resources.
     * 
     * @param context
     */
    public DataBaseHelper(Context context) {
        super(context, DB_NAME, null, 1);
        this.myContext = context;
    }

    /**
     * Creates a empty database on the system and rewrites it with your own database.
     * */
    public void createDataBase() throws IOException {
        boolean dbExist = checkDataBase();

        if (dbExist) {
            // do nothing - database already exist
        } else {

            // By calling this method and empty database will be created into the default system path
            // of your application so we are gonna be able to overwrite that database with our database.
            this.getReadableDatabase();

            try {

                copyDataBase();

            } catch (IOException e) {

                throw new Error("Error copying database");

            }
        }

    }

    /**
     * Check if the database already exist to avoid re-copying the file each time you open the application.
     * 
     * @return true if it exists, false if it doesn't
     */
    private boolean checkDataBase() {

        SQLiteDatabase checkDB = null;

        try {
            String myPath = DB_PATH + DB_NAME;
            checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);

        } catch (SQLiteException e) {

            // database does't exist yet.

        }

        if (checkDB != null) {

            checkDB.close();

        }

        return checkDB != null ? true : false;
    }

    /**
     * Copies your database from your local assets-folder to the just created empty database in the
     * system folder, from where it can be accessed and handled.
     * This is done by transfering bytestream.
     * */
    private void copyDataBase() throws IOException {

        // Open your local db as the input stream
        InputStream myInput = myContext.getAssets().open(DB_NAME);

        // Path to the just created empty db
        String outFileName = DB_PATH + DB_NAME;

        // Open the empty db as the output stream
        OutputStream myOutput = new FileOutputStream(outFileName);

        // transfer bytes from the inputfile to the outputfile
        byte[] buffer = new byte[1024];
        int length;
        while ((length = myInput.read(buffer)) > 0) {
            myOutput.write(buffer, 0, length);
        }

        // Close the streams
        myOutput.flush();
        myOutput.close();
        myInput.close();

    }

    public void openDataBase() throws SQLException {

        // Open the database
        String myPath = DB_PATH + DB_NAME;
        myDataBase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);

    }

    @Override
    public synchronized void close() {

        if (myDataBase != null) {
            myDataBase.close();
        }

        super.close();

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "create table if not exists ";
        query += "histories ( _id INTEGER PRIMARY KEY AUTOINCREMENT";
        query += ", member_id INTEGER";
        query += ", nickname STRING";
        query += ", created STRING";
        query += ");";
        db.execSQL(query);

        String tmppostquery = "create table if not exists ";
        tmppostquery += "tmpcontent ( id INTEGER PRIMARY KEY AUTOINCREMENT";
        tmppostquery += ", owner STRING";
        tmppostquery += ", title STRING";
        tmppostquery += ", texts STRING";
        tmppostquery += ");";
        db.execSQL(tmppostquery);

        String imagespathquery = "create table if not exists ";
        imagespathquery += "imagespath ( id INTEGER PRIMARY KEY AUTOINCREMENT";
        imagespathquery += ", owner STRING";
        imagespathquery += ", path STRING";
        imagespathquery += ", type Integer";
        imagespathquery += ");";
        db.execSQL(imagespathquery);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("drop table if exists histories");

        String query = "create table if not exists ";
        query += "histories ( _id INTEGER PRIMARY KEY AUTOINCREMENT";
        query += ", member_id INTEGER";
        query += ", nickname STRING";
        query += ", created STRING";
        query += ");";
        db.execSQL(query);

    }

    public void inserttmpcontent(TmpContent TmpContent){
        String query = "INSERT INTO tmpcontent (owner, title, texts)";

        query += " values (";
        query += " '" + TmpContent.getOwner() + "'";
        query += ", '" + TmpContent.getTitle() + "'";
        query += ", '" + TmpContent.getTexts() + "'";
        query += " ); ";

        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(query);
        db.close();
    }

    public void insertimagespath(ImagesPath imagespath){
        String query = "INSERT INTO imagespath (owner, path, type)";

        query += " values (";
        query += " '" + imagespath.getOwner() + "'";
        query += ", '" + imagespath.getPath() + "'";
        //System.out.println("image-------------------" + imagespath.getPath());
        query += ", '" + imagespath.getType() + "'";
        query += " ); ";

        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(query);
        db.close();
    }

    public TmpContent selectTmpContent(String query) {

        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.rawQuery(query, null);

        TmpContent tmpContent = new TmpContent();
        if (cursor != null) {
            if(cursor.moveToFirst()){
                tmpContent.setId(cursor.getInt(0));
                tmpContent.setOwner(cursor.getString(1));
                tmpContent.setTitle(cursor.getString(2));
                tmpContent.setTexts(cursor.getString(3));
            }
        }
        cursor.close();
        db.close();

        return tmpContent;
    }

    public ArrayList<ImagesPath> selectImagesPath(String query) {

        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.rawQuery(query, null);

        ArrayList<ImagesPath> Imagespaths = new ArrayList<ImagesPath>();

        if (cursor != null) {
            while(cursor.moveToNext()){

                Integer id = (cursor.getInt(0));
                String owner = (cursor.getString(1));
                String path = (cursor.getString(2));
                Integer type = (cursor.getInt(3));

                ImagesPath item = new ImagesPath(id,owner,path,type);

                Imagespaths.add(item);
            }
        }
        cursor.close();
        db.close();

        return Imagespaths;
    }

    public int deleteTmpContent(int id){

        String query = "DELETE FROM tmpcontent where id = '"+ id + "'";

        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(query);
        db.close();

        query = "SELECT count(*) FROM tmpcontent WHERE id = '"+ id + "'";

        db = getReadableDatabase();
        int count = 0;
        Cursor cursor = db.rawQuery(query, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
        }
        cursor.close();
        db.close();
        return count;
    }

    public int deleteImagePaths(String id){

        String query = "DELETE FROM imagespath where owner = '"+ id + "'";

        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(query);
        db.close();

        query = "SELECT count(*) FROM imagespath WHERE owner = '"+ id + "'";

        db = getReadableDatabase();
        int count = 0;
        Cursor cursor = db.rawQuery(query, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
        }
        cursor.close();
        db.close();
        return count;
    }










    // Add your public helper methods to access and get content from the database.
    // You could return cursors by doing "return myDataBase.query(....)" so it'd be easy
    // to you to create adapters for your views.

}
