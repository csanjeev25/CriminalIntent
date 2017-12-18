package com.insomniac.criminalintent.database;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.insomniac.criminalintent.database.CrimeDbSchema.ClassTable;


public class CrimeBaseHelper  extends SQLiteOpenHelper{

    private static final int VERSION = 1;
    private static final String DATABASE_NAME = "crime.db";

    public CrimeBaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public CrimeBaseHelper(Context context){
        super(context,DATABASE_NAME,null,VERSION);
    }

    public CrimeBaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version, DatabaseErrorHandler errorHandler) {
        super(context, name, factory, version, errorHandler);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("create table " + ClassTable.NAME + "(" +
                " _id integer primary key autoincrement, " +
                ClassTable.Cols.UUID + ", " +
                ClassTable.Cols.TITLE + ", " +
                ClassTable.Cols.DATE + ", " +
                ClassTable.Cols.SOLVED + ", " +
                ClassTable.Cols.SUSPECT +
                ")"
        );

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
