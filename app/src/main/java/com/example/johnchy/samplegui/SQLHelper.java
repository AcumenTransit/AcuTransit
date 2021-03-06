package com.example.johnchy.samplegui;

import android.content.Context;
import android.content.res.AssetManager;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLDataException;

/**
 * Created by john.chy on 6/30/2015.
 */
public class SQLHelper extends SQLiteOpenHelper{
    private static String DB_PATH = "";
    private static String DB_NAME = "VTA.db";
    private SQLiteDatabase tempDatabase;
    private final Context testContext;

    public SQLHelper(Context context){
        super(context, DB_NAME, null, 1);
        if(Build.VERSION.SDK_INT >= 17){
            DB_PATH = context.getApplicationInfo().dataDir + "/databases/";
        }
        else{
            DB_PATH ="/data/data/" + context.getApplicationInfo().dataDir + "/databases";
        }
        this.testContext = context;
    }

    public void CreateDatabase() throws IOException{
        boolean exists = checkDatabase();
        if(exists){}
        else{
            this.getReadableDatabase();
            try{
                copyDataBase();
            }
            catch (IOException e){
                throw new Error("Copy database failed");
            }
        }
    }

    private boolean checkDatabase(){
        SQLiteDatabase checkDB = null;
        try{
            String myPath = DB_PATH + DB_NAME;
            checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
        }catch(SQLiteException e){
            //database does't exist yet.
        }
        if(checkDB != null){
            checkDB.close();
        }
        return checkDB != null ? true : false;
    }


    private void copyDataBase() throws IOException
    {
        //Open your local db as the input stream
        InputStream myInput = testContext.getAssets().open(DB_NAME);
        // Path to the just created empty db
        String outFileName = DB_PATH + DB_NAME;
        //Open the empty db as the output stream
        OutputStream myOutput = new FileOutputStream(outFileName);
        //transfer bytes from the inputfile to the outputfile
        byte[] buffer = new byte[1024];
        int length;
        while ((length = myInput.read(buffer))>0)
        {
            myOutput.write(buffer, 0, length);
        }
        //Close the streams
        myOutput.flush();
        myOutput.close();
        myInput.close();
    }
    public void openDataBase() throws SQLException
    {
        //Open the database
        String myPath = DB_PATH + DB_NAME;
        tempDatabase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.CREATE_IF_NECESSARY);
    }
    @Override
    public synchronized void close()
    {
        if(tempDatabase != null)
            tempDatabase.close();
        super.close();
    }
    @Override
    public void onCreate(SQLiteDatabase db)
    {

    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {

    }
}
