package com.example.movieroulette;

import android.content.Context;
import android.graphics.Bitmap;
import java.util.ArrayList;

/**
 * Class for holding the necessary information for the movies pulled from the TMDb API
 */
public class Movie{
    private ArrayList<String> genres;
    private String title;
    private String overview;
    private Bitmap poster;
    private String imdbID;
    private String tmdb_id;

    /**
     * Overloaded constructor to create a full movie object through the API using its id
     * @param id        TMDb id of the movie
     * @param context   Context of an activity in order to use a TMDB_Wrapper object
     */
    public Movie(String id, Context context){
        // Wrapper for sending requests to TMDb api
        TMDB_Wrapper tmdb = new TMDB_Wrapper(context);
        Movie temp = tmdb.GetMovieDetails(id);

        genres = temp.getGenres();
        title = temp.getTitle();
        overview = temp.getOverview();
        poster = temp.getPoster();
        imdbID = temp.getImdbID();
        tmdb_id = temp.getTmdb_id();
    }

    public Movie(){

    }

    // Getters
    public Bitmap getPoster() {
        return poster;
    }

    public ArrayList<String> getGenres() {
        return genres;
    }

    public String getOverview() {
        return overview;
    }

    public String getTitle() {
        return title;
    }

    public String getImdbID() { return imdbID; }

    public String getTmdb_id() { return tmdb_id; }

    // Setters
    public void setGenres(ArrayList<String> genres) {
        this.genres = genres;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    public void setPoster(Bitmap poster) {
        this.poster = poster;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setImdbID(String imdbID) { this.imdbID = imdbID; }

    public void setTmdb_id(String tmdb_id) { this.tmdb_id = tmdb_id; }
}
