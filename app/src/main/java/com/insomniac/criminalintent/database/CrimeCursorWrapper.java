package com.insomniac.criminalintent.database;

import android.database.Cursor;
import android.database.CursorWrapper;
import android.util.Log;

import com.insomniac.criminalintent.Crime;
import com.insomniac.criminalintent.database.CrimeDbSchema.ClassTable;

import java.util.Date;
import java.util.UUID;

import static android.content.ContentValues.TAG;

public class CrimeCursorWrapper extends CursorWrapper {

    public CrimeCursorWrapper(Cursor cursor) {
        super(cursor);
    }

    public Crime getCrime(){
        Log.d(TAG,"getCrime()CrimeCursorWrapper");
        String uuidString = getString(getColumnIndex(ClassTable.Cols.UUID));
        String title = getString(getColumnIndex(ClassTable.Cols.TITLE));
        long date = getLong(getColumnIndex(ClassTable.Cols.DATE));
        int isSolved = getInt(getColumnIndex(ClassTable.Cols.SOLVED));
        String suspect = getString(getColumnIndex(ClassTable.Cols.SUSPECT));


        Crime crime = new Crime(UUID.fromString(uuidString));
        crime.setTitle(title);
        crime.setDate(new Date(date));
        crime.setSolved(isSolved != 0);
        crime.setSuspect(suspect);

        return crime;
    }
}
