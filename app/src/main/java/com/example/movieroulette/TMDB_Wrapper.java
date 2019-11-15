package com.example.movieroulette;

import android.content.Context;
import android.graphics.BitmapFactory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;


/**
 *  Class that contains the necessary methods for sending requests to the TMDb API and
 *  parsing the return values (mainly in JSON format) in types usable by the application.
 */
public final class TMDB_Wrapper {

    // API key necessary for sending requests towards the TMDb API
    private static String API_KEY = "TMDb_API_KEY";

    public TMDB_Wrapper(Context context){
        API_KEY = context.getResources().getString(R.string.TMDb_API_KEY);
    }

    // URLs for various parts of the TMDb API
    private static final String GENRE_URL = "https://api.themoviedb.org/3/genre/movie/list?api_key=";
    private static final String DISCOVER_URL = "https://api.themoviedb.org/3/discover/movie?api_key=";
    private static final  String MOVIE_URL = "https://api.themoviedb.org/3/movie/";

    // Strings containing the necessary URL part for sending requests with filters
    private static final String SORT_CONSTRAINT = "&sort_by=";
    private static final String ADULT_CONSTRAINT = "&include_adult=";
    private static final String VIDEO_CONSTRAINT = "&include_video=";
    private static final String PAGE_CONSTRAINT = "&page=";
    private static final String GENRE_CONSTRAINT = "&with_genres=";
    private static final String YEAR_CONSTRAINT = "&year=";

    // HashMap containing the movies' genres and their genres' ids
    // Required for avoiding making duplicate calls to the API while retrieving movie information
    private static HashMap<String, Integer> genre_hashmap = null;

    /**
     * Returns a Movie object with the necessary information retrieved from TMDb.
     * Sends request to the TMDb API and then parses the data into a Movie object.
     * @param id TMDb id of the movie, whose details are requested
     * @return  Returns a Movie object, with the information retrieved from the API
     */
    public Movie GetMovieDetails(String id){
        //Building the URL
        StringBuilder urlString = new StringBuilder(MOVIE_URL);
        urlString.append(id);
        urlString.append("?api_key=" + API_KEY);

        // Sending the request
        JSONObject response = SendRequest(urlString.toString());

        //Parsing the JSON data retrieved into a Movie object
        Movie movie = new Movie();
        try {
            movie.setOverview(response.getString("overview"));
            movie.setTitle(response.getString("title"));
            movie.setImdbID(response.getString("imdb_id"));
            movie.setTmdb_id(id);

            ArrayList<String> genres = new ArrayList<>();
            JSONArray JSONgenres = response.getJSONArray("genres");
            for (int i = 0; i < JSONgenres.length(); i++) {
                genres.add(JSONgenres.getJSONObject(i).getString("name"));
            }
            movie.setGenres(genres);

            // Downloading the poster image of the movie
            String posterURL = "https://image.tmdb.org/t/p/w500/" + response.getString("poster_path");
            try {
                movie.setPoster(BitmapFactory.decodeStream(new URL(posterURL).openConnection().getInputStream()));
            }
            catch(MalformedURLException e){
                System.out.println(e.getMessage());
                return null;
            }
            catch(IOException e){
                System.out.println(e.getMessage());
                return null;
            }
        }
        catch(JSONException e){
            System.out.println(e.getMessage());
            return null;
        }

        return movie;
    }

    /**
     * Returns a HashMap with all the available movie genres and their IDs.
     * @return  HashMap with movie genres and genres' ids
     */
    public HashMap<String, Integer> GetMovieGenres(){

        HashMap<String, Integer> returnValue = new HashMap<>();
        // Building the URL and sending the request
        JSONObject response = SendRequest(GENRE_URL + API_KEY);

        // Parsing the JSON data into a HashMap
        try {
            JSONArray genres = response.getJSONArray("genres");
            for (int i = 0; i < genres.length(); i++) {
                JSONObject genre = genres.getJSONObject(i);
                returnValue.put(genre.getString("name"), genre.getInt("id"));
            }
        }
        catch(JSONException e){
            System.out.println(e.getMessage());
            return null;
        }
        genre_hashmap = returnValue;
        return returnValue;
    }

    /**
     * Returns a movie's TMDb id. The movie selected is the most popular according to TMDb API.
     * If the user used the year or the genre filter, the search will take them into account
     * and present results with the required year of release or/and required genre.
     * After that the movie id will be checked against the SQLiteDatabase.
     * If the movie id is found, then the next movie that meets the criteria will be presented to
     * the user.
     * The value of the year filter is -1 if the filter was not used.
     * The value of with_genre is an empty string if the filter was not used.
     *
     * NOTICE: Known unintended behaviour -- API returns results disregarding the year filter //TODO
     *          if the value given doesn't exist in TMDb (e.g year = 1453)
     *
     * @param year  Filter that restricts movies selected by year of release (-1 if N/A)
     * @param with_genre The genre the movie must include ("" if N/A)
     * @return  The movie's id or null if something went wrong
     */
    public String GetPopularMovieID(int year, String with_genre){
        // Building the basic url
        StringBuilder urlString = new StringBuilder(DISCOVER_URL);
        urlString.append(API_KEY);
        // Appends filters to the url
        urlString.append(SORT_CONSTRAINT + "popularity.desc");
        urlString.append(ADULT_CONSTRAINT + "false");
        urlString.append(VIDEO_CONSTRAINT + "false");
        if(year != -1)
            urlString.append(YEAR_CONSTRAINT + year);
        if(with_genre != "")
            if(genre_hashmap != null)
                urlString.append(GENRE_CONSTRAINT + genre_hashmap.get(with_genre));

        // Initially requesting the first page of results from the API.
        // If all of the results are in the database of seen/not interested movies then the next
        // page is requested from the API.
        // In the end the movie id is extracted from the JSON response and returned.
        JSONObject response;
        int pageCount = 1;
        try {
            do {
                urlString.append(PAGE_CONSTRAINT + pageCount);
                response = SendRequest(urlString.toString());
                try {
                    JSONArray page_movies = response.getJSONArray("results");
                    String id = null;
                    for (int i = 0; i < page_movies.length(); i++) {
                        id = page_movies.getJSONObject(i).getString("id");
                        if (!IsIdDisqualified(id))
                            return id;
                    }
                    pageCount++;
                } catch (JSONException e) {
                    System.out.println(e.getMessage());
                    return null;
                }
            } while (pageCount <= response.getInt("total_pages"));
            return null;
        }
        catch(JSONException e){
            System.out.println(e.getMessage());
            return null;
        }
    }

    /**
     * Method responsible for sending all the requests to the API and returning the result as
     * a JSON object
     * @param url   The url for sending the request to TMDb API
     * @return      JSONObject with response data
     */
    private JSONObject SendRequest(String url){
        try {
            URL queryURL = new URL(url);
            HttpURLConnection con = (HttpURLConnection) queryURL.openConnection();

            InputStream response = new BufferedInputStream(con.getInputStream());
            String responseString = StreamToString(response);
            return StringToJSON(responseString);
        }catch (MalformedURLException e){
            System.out.println(e.getMessage());
            return null;
        }
        catch(IOException e){
            System.out.println(e.getMessage());
            return null;
        }
    }


    /**
     * Simple method for parsing the raw data from JSON format to JSONObject
     * @param data  String containng the JSON datra
     * @return      JSONObject with the data of the string passed
     */
    private JSONObject StringToJSON(String data){
        try{
            JSONObject json = new JSONObject(data);
            return json;
        }
        catch(JSONException e){
            System.out.println(e.getMessage());
            return null;
        }
    }

    /**
     * Method for parsing the stream of data from the API's response as a String
     * Code snippet was taken from https://www.baeldung.com/convert-input-stream-to-string
     * (Method 4) by https://www.baeldung.com/author/eugen/
     * @param stream    Stream returned from an API request
     * @return          Returns stream as string
     */
    private String StreamToString(InputStream stream){
        // Code snippet from https://www.baeldung.com/convert-input-stream-to-string
        StringBuilder returnValue = new StringBuilder();
        try(Reader rdr = new BufferedReader(new InputStreamReader(stream))){
            int c = 0;
            while((c= rdr.read()) != -1){
                returnValue.append((char)c);
            }
        }catch(IOException e){
            System.out.println(e.getMessage());
            return null;
        }
        return returnValue.toString();
    }

    /**
     * Simple method for checking the database for the existence of the movie given its id
     * @param id TMDb id of the movie
     * @return  Boolean value -- True if movie exists in database
     */
    private boolean IsIdDisqualified(String id){
        return DB_Gate.getInstance().DoesMovieExist(id);
    }
}
