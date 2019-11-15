package com.example.movieroulette;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import android.os.StrictMode;

/**
 * Activity that launches with the application.
 * Contains the main interface with which the user defines the filters he wants in order for the
 * application to make a suggestion.
 * There is also a button enabling the user to delete his database of movies in order to reset
 * the suggestions of the application.
 */
public class MainActivity extends AppCompatActivity {

    // Codes for identifying various requests
    private static final int REQ_CODE_PERMISSIONS = 750;
    private static final int REQ_CODE_GENRE_LIST = 760;
    private static final int REQ_CODE_MOVIE_DETAILS = 770;

    // Declaring UI elements
    private Switch yearSwitch;
    private Switch genreSwitch;
    private Switch clearDbSwitch;
    private EditText yearInput;
    private Button genreInput;
    private Button searchButton;
    private Button clearDatabase;
    private TextView appDescrption;

    // Wrapper for sending requests to TMDb api
    private TMDB_Wrapper tmdb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // TODO make internet access calls in async
        // Bad practice to be fixed as this makes the app laggy
        // ==================== DANGER ZONE ====================
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        // ==================== DANGER ZONE ====================
        //(FT. Kenny Loggins)

        // Requesting permission to use internet
        requestInternetPermission();

        // Initializing TMDB_Wrapper for use
        tmdb = new TMDB_Wrapper(this);

        // Referencing UI
        yearSwitch = findViewById(R.id.yearSwitch);
        genreSwitch = findViewById(R.id.genreSwitch);
        yearInput = findViewById(R.id.yearInput);
        genreInput = findViewById(R.id.genreInput);
        searchButton = findViewById(R.id.searchButton);
        appDescrption = findViewById(R.id.appDescription);
        clearDatabase = findViewById(R.id.clearDB);
        clearDbSwitch = findViewById(R.id.clearDbSwitch);

        // Initializing state of UI
        yearSwitch.setChecked(false);
        yearInput.setEnabled(false);

        genreSwitch.setChecked(false);
        genreInput.setEnabled(false);

        clearDbSwitch.setChecked(false);
        clearDatabase.setEnabled(false);

        appDescrption.setText(appDescriptionText);
        clearDatabase.setEnabled(false);

        // Assign Listeners
        assignListenersToSwitches();
        assignListenerToButtons();

        // Setup Database
        // Passing an already open database or creating one
        SQLiteDatabase db = openOrCreateDatabase(DB_Gate.getDbName(), Context.MODE_PRIVATE, null);
        DB_Gate.getInstance().SetUp(db);

    }

    /**
     * Asking for internet permission if not available already.
     * Exits the application if not given.
     */
    private void requestInternetPermission(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET}, REQ_CODE_PERMISSIONS);
        }
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET)!= PackageManager.PERMISSION_GRANTED){
            this.finish();
        }
    }

    /**
     * Assigns listeners to the switches of the UI.
     * Enabling and disabling UI elements, as well defining the filters used in the search.
     */
    private void assignListenersToSwitches(){
        yearSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(!isChecked){
                    yearInput.setEnabled(false);
                }
                else{
                    yearInput.setEnabled(true);
                }
            }
        });

        genreSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(!isChecked){
                    genreInput.setEnabled(false);
                }
                else{
                    genreInput.setEnabled(true);
                }
            }
        });

        clearDbSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(!isChecked){
                    clearDatabase.setEnabled(false);
                }
                else{
                    clearDatabase.setEnabled(true);
                }
            }
        });
    }

    /**
     * Assigns listeners to the buttons of the UI.
     */
    private void assignListenerToButtons(){
        // Starting a new activity for the user to choose the genre he wishes
        genreInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent showGenreList = new Intent(MainActivity.this, GenreList.class);
                startActivityForResult(showGenreList, REQ_CODE_GENRE_LIST);
            }
        });

        // Sends a request to the TMDb API in order to find a movie suggestion
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Filter values to be passed
                int year;
                String genre;

                // Checks if year filter is used
                if(yearSwitch.isChecked()){
                    // If the year field is empty, prints alert and aborts request
                    if(yearInput.getText().toString().equals("")){
                        Toast.makeText(getBaseContext(), "Invalid year value!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    year = Integer.parseInt(yearInput.getText().toString());
                }
                else{
                    // Sets the default if filter is not used
                    year = -1;
                }

                // Checks if the genre filter is used
                if(genreSwitch.isChecked()){
                    // If genre is not selected, prints alert and aborts request
                    if(genreInput.getText().toString().equals(getString(R.string.select_genre_button))){
                        Toast.makeText(getBaseContext(), "No genre selected!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    genre = genreInput.getText().toString();
                }
                else{
                    // Sets the default if filter is not used
                    genre = "";
                }
                String movieID = tmdb.GetPopularMovieID(year, genre);
                ShowMovieDetails(movieID);
            }
        });

        // Clears the database after long click by the user
        clearDatabase.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                DB_Gate.getInstance().ClearDatabase();
                Toast.makeText(getBaseContext(), "You cleared your database!", Toast.LENGTH_LONG).show();
                clearDbSwitch.setChecked(false);
                return false;
            }
        });
    }

    /**
     * Starts new activity for showing the suggested movies details after the searching is completed
     * @param movieID   The TMDb id of the movie
     */
    private void ShowMovieDetails(String movieID){
        Intent showDetails = new Intent(MainActivity.this, MovieDetails.class);
        showDetails.putExtra("Movie", movieID);

        startActivityForResult(showDetails, REQ_CODE_MOVIE_DETAILS);

    }

    /**
     * After a genre is chosen sets the text of the genre button equal to the name of the genre
     * in order to further use it when searching.
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode == REQ_CODE_GENRE_LIST){
            if(resultCode == RESULT_OK){
                genreInput.setText(data.getStringExtra("SelectedGenre"));
            }
        }
    }

    // Wall of text to be displayed as minimal guidance for the user.
    private static final String appDescriptionText = "Description: \n\n" +
                                        "Movie Roulette is an app that using:\n" +
                                        "       TMDB (https://www.themoviedb.org/)\n" +
                                        "and search filters presents you the most popular movie.\n\n" +
                                        "   By adding movies to your database you disqualify them from popping " +
                                        "up again.\n\n" +
                                        "   Clicking for more information will open your browser on the IMDb page of the" +
                                        "selected movie.";
}
