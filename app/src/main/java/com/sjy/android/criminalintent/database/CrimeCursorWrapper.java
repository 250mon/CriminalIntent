package com.sjy.android.criminalintent.database;

import android.database.Cursor;
import android.database.CursorWrapper;
import android.util.Log;

import com.sjy.android.criminalintent.Crime;

import java.text.DateFormat;
import java.text.ParseException;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

import static com.sjy.android.criminalintent.database.CrimeDbSchema.CrimeTable;

public class CrimeCursorWrapper extends CursorWrapper {
    public CrimeCursorWrapper(Cursor cursor) {
        super(cursor);
    }

    public Crime getCrime() {
        String uuidString = getString(getColumnIndex(CrimeTable.Cols.UUID));
        String title = getString(getColumnIndex(CrimeTable.Cols.TITLE));
        //long date = getLong(getColumnIndex(CrimeTable.Cols.DATE));
        String date = getString(getColumnIndex(CrimeTable.Cols.DATE));
        int isSolved = getInt(getColumnIndex(CrimeTable.Cols.SOLVED));
        String suspect = getString(getColumnIndex(CrimeTable.Cols.SUSPECT));
        String phoneNumber = getString(getColumnIndex(CrimeTable.Cols.PHONENUMBER));

        Crime crime = new Crime(UUID.fromString(uuidString));
        crime.setTitle(title);
        crime.setDate(Instant.parse(date));
        /*try {
            Date dfDate = DateFormat.getDateInstance().parse(dateStr);
            crime.setDate(dfDate);
        } catch(ParseException e) {
            Log.i("CrimeCursorWrapper.getCrime()", "Date Format parse error", e);
        }*/
        crime.setSolved(isSolved != 0);
        crime.setSuspect(suspect);
        crime.setPhoneNumber(phoneNumber);

        return crime;
    }
}
