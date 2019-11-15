package com.example.movieroulette;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * Singleton class containing the necessary methods to make calls to the SQLite database
 */
public final class DB_Gate {

    private static final String DB_NAME = "MovieRoulette";
    public static String getDbName() { return DB_NAME; }

    private static SQLiteDatabase db;

    // Flag used for checking if the db has been setup on runtime before doing other operations
    private static boolean setupFlag = false;

    private static DB_Gate instance = null;

    private DB_Gate(){
        // Private Constructor to make singleton
    }

    public static DB_Gate getInstance(){
        if(instance == null){
            instance = new DB_Gate();
            return instance;
        }
        else{
            return instance;
        }
    }

    /**
     * Sets up the database by assigning the reference of an open database and creating the
     * necessary tables.
     * @param openedDatabase    Needs an already open SQLiteDatabase reference
     */
    public void SetUp(SQLiteDatabase openedDatabase){
        setupFlag = true;
        db = openedDatabase;
        CreateMovieTable();
    }

    /**
     * Creates the movie table if it doesn't exist already.
     * The table has the TMDb movie id as a PK.
     */
    private void CreateMovieTable(){
        if(!setupFlag)
            return;

        String query = "CREATE TABLE IF NOT EXISTS movies (" +
                            "id TEXT NOT NULL," +
                            "title TEXT NOT NULL," +
                            "PRIMARY KEY(id));";

        db.execSQL(query);
    }

    /**
     * Deletes row from the movie table using the TMDb id.
     * @param id    TMDb id of the movie for deletion
     */
    public void DeleteMovie(String id){
        if(!setupFlag)
            return;

        String query = "DELETE FROM movies " +
                        "WHERE id='" + id + "';";

        db.execSQL(query);
    }

    /**
     * Adds a row to the movie table. Saves the id and the title of the movie.
     * Title is currently unused by the app.
     * @param id    TMDb id of the movie to add
     * @param title Title of the movie to add
     */
    public void AddMovie(String id, String title){
        if(!setupFlag)
            return;

        String query = "INSERT INTO movies(id, title) " +
                        "VALUES('" + id + "', '" + title + "');";

        db.execSQL(query);
    }

    /**
     * Checks if the id belongs to a movie that exists in the database
     * @param id    TMDb id, used as PK by the movie table
     * @return      Boolean value depending on finding the id in the database -- True if found
     */
    public boolean DoesMovieExist(String id){
        if(!setupFlag)
            return false;

        String query = "SELECT id FROM movies " +
                        "WHERE id='" + id + "';";

        Cursor result = db.rawQuery(query, null);
        if(result.getCount() > 0){
            return true;
        }
        else
            return false;
    }

    /**
     * Clears the whole database by dropping the tables and recreating them empty.
     * Currently drops only one table, as only the movie table exists.
     */
    public void ClearDatabase(){
        if(!setupFlag)
            return;

        // Deletes tables
        String query = "DROP TABLE IF EXISTS movies;";
        db.execSQL(query);

        // Creates them again
        CreateMovieTable();
    }
}
