package com.okunev.moviebase.utils;

/**
 * Created by 777 on 2/14/2016.
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.okunev.moviebase.models.Movie;

import java.util.ArrayList;

public class DBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "MovieBase.db";
    public static final String MOVIES_TABLE_NAME = "movies";
    public static final String MOVIES_COLUMN_ID = "id";
    public static final String MOVIES_COLUMN_NAME = "name";
    public static final String MOVIES_COLUMN_PART = "part";
    public static final String MOVIES_COLUMN_SEASON = "season";
    public static final String MOVIES_COLUMN_SERIES = "series";
    public static final String MOVIES_COLUMN_TIME = "time";


    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // TODO Auto-generated method stub
        db.execSQL(
                "create table if not exists movies " +
                        "(id integer primary key, name text, time text, part text, season text, series text)"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
        db.execSQL("DROP TABLE IF EXISTS " + MOVIES_TABLE_NAME);
        onCreate(db);
    }

    public boolean insertMovie(String name, String part, String season, String series, String time) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("name", name);
        contentValues.put("time", time);
        contentValues.put("part", part);
        contentValues.put("season", season);
        contentValues.put("series", series);
        db.insert(MOVIES_TABLE_NAME, null, contentValues);
        return true;
    }

    public Movie getData(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select * from movies where id=" + id + "", null);
        res.moveToFirst();
        Movie movie = new Movie();
        movie.setId(res.getInt(res.getColumnIndex(MOVIES_COLUMN_ID)));
        movie.setName(res.getString(res.getColumnIndex(MOVIES_COLUMN_NAME)));
        movie.setPart(res.getString(res.getColumnIndex(MOVIES_COLUMN_PART)));
        movie.setSeason(res.getString(res.getColumnIndex(MOVIES_COLUMN_SEASON)));
        movie.setSeries(res.getString(res.getColumnIndex(MOVIES_COLUMN_SERIES)));
        movie.setTime(res.getString(res.getColumnIndex(MOVIES_COLUMN_TIME)));
        res.close();
        return movie;
    }

    public int numberOfRows() {
        SQLiteDatabase db = this.getReadableDatabase();
        int numRows = (int) DatabaseUtils.queryNumEntries(db, MOVIES_TABLE_NAME);
        return numRows;
    }

    public boolean updateMovie(Integer id, String name, String part, String season, String series, String time) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("name", name);
        contentValues.put("part", part);
        contentValues.put("season", season);
        contentValues.put("series", series);
        contentValues.put("time", time);
        db.update(MOVIES_TABLE_NAME, contentValues, "id = ? ", new String[]{Integer.toString(id)});
        return true;
    }

    public Integer deleteMovie(Integer id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(MOVIES_TABLE_NAME,
                "id = ? ",
                new String[]{Integer.toString(id)});
    }

    public ArrayList<String> getAllMovieNames() {
        ArrayList<String> array_list = new ArrayList<>();

        //hp = new HashMap();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select * from " + MOVIES_TABLE_NAME, null);
        res.moveToFirst();

        while (!res.isAfterLast()) {
            array_list.add(res.getString(res.getColumnIndex(MOVIES_COLUMN_NAME)));
            res.moveToNext();
        }
        res.close();
        return array_list;
    }

    public ArrayList<Integer> getAllId() {
        ArrayList<Integer> array_list = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select * from " + MOVIES_TABLE_NAME, null);
        res.moveToFirst();

        while (!res.isAfterLast()) {
            array_list.add(res.getInt(res.getColumnIndex("id")));
            res.moveToNext();
        }
        res.close();
        return array_list;
    }

    public void migrateTables(Context context) {
        ArrayList<Movie> array_list = new ArrayList<>();

        //hp = new HashMap();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select * from contacts", null);
        res.moveToFirst();

        while (!res.isAfterLast()) {
            Movie movie = new Movie();
            movie.setName(res.getString(res.getColumnIndex(MOVIES_COLUMN_NAME)));
            movie.setPart(res.getString(res.getColumnIndex("phone")));
            movie.setSeason(res.getString(res.getColumnIndex("email")));
            movie.setSeries(res.getString(res.getColumnIndex("street")));
            movie.setTime(res.getString(res.getColumnIndex("place")));
            array_list.add(movie);
            res.moveToNext();
        }
        res.close();
        onCreate(db);
        for (Movie movie : array_list)
            insertMovie(movie.getName(), movie.getPart(), movie.getSeason(), movie.getSeries(), movie.getTime());

    }
}