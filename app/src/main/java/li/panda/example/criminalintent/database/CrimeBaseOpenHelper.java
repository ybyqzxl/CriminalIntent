package li.panda.example.criminalintent.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static li.panda.example.criminalintent.database.CrimeDbSchema.*;

/**
 * Created by xueli on 2016/6/2.
 */
public class CrimeBaseOpenHelper extends SQLiteOpenHelper {

    private static final int VERSION = 1;
    private static final String DATABASE_NAME = "crimeBase.db";

    public CrimeBaseOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + CrimeTable.NAME + "(" + "id integer " + "primary key " +
                "autoincrement," + CrimeTable.Cols.UUID + "," + CrimeTable.Cols.TITLE + "," +
                CrimeTable.Cols.DATE + "," + CrimeTable.Cols.SOLVED + "," + CrimeTable.Cols
                .PERSON + ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
