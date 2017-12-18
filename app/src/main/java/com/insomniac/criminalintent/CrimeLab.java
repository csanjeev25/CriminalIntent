package com.insomniac.criminalintent;

/**
 * Created by Sanjeev on 4/2/2017.
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

import com.insomniac.criminalintent.database.CrimeBaseHelper;
import com.insomniac.criminalintent.database.CrimeCursorWrapper;
import com.insomniac.criminalintent.database.CrimeDbSchema;
import com.insomniac.criminalintent.database.CrimeDbSchema.ClassTable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static android.content.ContentValues.TAG;

public class CrimeLab {
    private static CrimeLab sCrimeLab;
    private Context mContext;
    private SQLiteDatabase mSQLiteDatabase;


    public static CrimeLab get(Context context) {
        if (sCrimeLab == null) {
            sCrimeLab = new CrimeLab(context);
        }
        return sCrimeLab;
    }

    public void addCrime(Crime crime) {
        ContentValues contentValues = getContectValues(crime);
        mSQLiteDatabase.insert(ClassTable.NAME,null,contentValues);
    }

    private CrimeLab(Context context) {
        mContext = context.getApplicationContext();
        mSQLiteDatabase = new CrimeBaseHelper(mContext).getWritableDatabase();

    }

    public void updateCrime(Crime crime){
        String uuidString = crime.getId().toString();
        ContentValues contentValues = getContectValues(crime);

        mSQLiteDatabase.update(ClassTable.NAME,contentValues,ClassTable.Cols.UUID + " = ?",new String[]{uuidString});
    }

    private CrimeCursorWrapper queryCrime(String whereClause, String[] whereArgs){
        Log.d(TAG,"qureryCrime()");
        Cursor cursor = mSQLiteDatabase.query(ClassTable.NAME,null,whereClause,whereArgs,null,null,null);
        return new CrimeCursorWrapper(cursor);
    }

    public File getPhotoFile(Crime crime){
        File externalFilesDir = mContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        if(externalFilesDir == null)
            return null;

        return new File(externalFilesDir,crime.getPhotoFileName());
    }

    public List<Crime> getCrimes() {
        List<Crime> crimes = new ArrayList<>();
        Log.d(TAG,"getCrimes()");
        CrimeCursorWrapper crimeCursorWrapper = queryCrime(null,null);

        try{
            Log.d(TAG,"in try block of getCrimes()");
            crimeCursorWrapper.moveToFirst();
            while(!crimeCursorWrapper.isAfterLast()){
                Log.d(TAG,"in try while block of getCrimes()");
                Log.d(TAG,Integer.toString(crimeCursorWrapper.getCount()));
                crimes.add(crimeCursorWrapper.getCrime());
                crimeCursorWrapper.moveToNext();
            }
        }finally {
            crimeCursorWrapper.close();
        }

        return crimes;
    }

    public Crime getCrime(UUID id) {

        CrimeCursorWrapper crimeCursorWrapper = queryCrime(ClassTable.Cols.UUID + " = ? ",new String[] {id.toString()});

        try {
            if (crimeCursorWrapper.getCount() == 0)
                return null;
            crimeCursorWrapper.moveToFirst();
            return crimeCursorWrapper.getCrime();
        }finally {
            crimeCursorWrapper.close();
        }
    }

    private static ContentValues getContectValues(Crime crime){
        ContentValues contentValues = new ContentValues();
        contentValues.put(ClassTable.Cols.UUID,crime.getId().toString());
        contentValues.put(ClassTable.Cols.TITLE,crime.getTitle());
        contentValues.put(ClassTable.Cols.DATE,crime.getDate().toString());
        contentValues.put(ClassTable.Cols.SOLVED,crime.isSolved() ? 1 : 0);
        contentValues.put(ClassTable.Cols.SUSPECT,crime.getSuspect());
        return contentValues;
    }

    public void deleteCrime(UUID crimeId){
            String uuidString = crimeId.toString();
            mSQLiteDatabase.delete(ClassTable.NAME,ClassTable.Cols.UUID + " = ? ",new String[] {uuidString});
        }
}
