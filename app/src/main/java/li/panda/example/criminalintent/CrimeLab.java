package li.panda.example.criminalintent;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import li.panda.example.criminalintent.database.CrimeBaseOpenHelper;
import li.panda.example.criminalintent.database.CrimeCursorWrapper;
import li.panda.example.criminalintent.database.CrimeDbSchema;

import static li.panda.example.criminalintent.database.CrimeDbSchema.*;

/**
 * Created by xueli on 2016/5/30.
 */
public class CrimeLab {
    private static CrimeLab crimeLab;
    //    private List<Crime> crimeList;
    private Context mContext;
    private SQLiteDatabase db;

    public static CrimeLab getCrimeLab(Context context) {
        if (crimeLab == null) {
            crimeLab = new CrimeLab(context);
        }
        return crimeLab;
    }

    private CrimeLab(Context context) {
        mContext = context.getApplicationContext();
        db = new CrimeBaseOpenHelper(mContext).getWritableDatabase();
        //crimeList = new ArrayList<>();
//        for (int i = 0; i < 100; i++) {
//            Crime crime = new Crime();
//            crime.setTitle("crime #" + i);
//            crime.setSolved(i % 2 == 0);
//            crimeList.add(crime);
//        }
    }

    public List<Crime> getCrimeList() {
        List<Crime> crimeList = new ArrayList<>();

        CrimeCursorWrapper cursorWrapper = queryCrimes(null, null);
        try {
            cursorWrapper.moveToFirst();
            while (!cursorWrapper.isAfterLast()) {
                crimeList.add(cursorWrapper.getCrime());
                cursorWrapper.moveToNext();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            cursorWrapper.close();
        }
        //return crimeList;
        return crimeList;
    }

    public Crime getCrime(UUID id) {
//        for (Crime crime : crimeList) {
//            if (crime.getId().equals(id)) {
//                return crime;
//            }
//        }
        CrimeCursorWrapper cursorWrapper = queryCrimes(CrimeTable.Cols.UUID + "=?", new
                String[]{id.toString()});
        try {
            if (cursorWrapper.getCount() == 0) {
                return null;
            }
            cursorWrapper.moveToFirst();
            return cursorWrapper.getCrime();
        } finally {
            cursorWrapper.close();
        }
    }

    public File getPhotoFile(Crime crime) {
        File externalFilesDir = Environment.getExternalStorageDirectory();
        if (externalFilesDir == null) {
            return null;
        }
        return new File(externalFilesDir, crime.getPhotoFilename());
    }

    public void addCrime(Crime c) {
        ContentValues values = getContentValues(c);
        db.insert(CrimeTable.NAME, null, values);
    }

    public void updateCrime(Crime crime) {
        String uuidString = crime.getId().toString();
        ContentValues values = getContentValues(crime);
        db.update(CrimeTable.NAME, values, CrimeTable.Cols.UUID + "= ?", new String[]{uuidString});
    }

    private static ContentValues getContentValues(Crime crime) {
        ContentValues values = new ContentValues();
        values.put(CrimeTable.Cols.UUID, crime.getId().toString());
        values.put(CrimeTable.Cols.TITLE, crime.getTitle());
        values.put(CrimeTable.Cols.DATE, crime.getDate().getTime());
        values.put(CrimeTable.Cols.SOLVED, crime.isSolved() ? 1 : 0);
        values.put(CrimeTable.Cols.PERSON, crime.getPerson());
        return values;
    }

    private CrimeCursorWrapper queryCrimes(String whereClause, String[] whereArgs) {
        Cursor cursor = db.query(CrimeTable.NAME, null, whereClause, whereArgs, null, null, null);
        return new CrimeCursorWrapper(cursor);
    }

}
