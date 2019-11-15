package com.example.movieroulette;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Activity for showing information about the movie selected.
 * Gives the option to view more information via IMDb.
 * Contains the movie's title, overview, genres and poster.
 * The user can add the movie to the database in order to not see it again as a suggestion
 * or can return to the MainActivity.
 */
public class MovieDetails extends AppCompatActivity {

    // IMDb url for directing to specific movies' IMDb pages for more information via browser
    private static final String imdbURL = "https://m.imdb.com/title/";

    // UI elements
    private TextView title;
    private TextView genres;
    private TextView overview;
    private ImageView poster;
    private Button imdbButton;
    private Button backButton;
    private Button addButton;
    private ScrollView overviewScroll;

    // Movie object to access the information for showing
    private Movie movie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);

        // Reference UI
        title = findViewById(R.id.titleText);
        genres = findViewById(R.id.genresText);
        overview = findViewById(R.id.overviewText);
        poster = findViewById(R.id.posterImage);
        imdbButton = findViewById(R.id.imdbButton);
        backButton = findViewById(R.id.backButton);
        addButton = findViewById(R.id.addButton);
        overviewScroll = findViewById(R.id.overviewScroll);

        // Setup listeners
        assignButtonListeners();

        // Get movie from extras
        movie = new Movie(getIntent().getStringExtra("Movie"));

        showMovieDetails();


    }

    /**
     * Sets the UI elements with the values retrieved from the Movie object
     */
    private void showMovieDetails(){
        title.setText(movie.getTitle());
        overview.setText(movie.getOverview());
        poster.setImageBitmap(Bitmap.createScaledBitmap(movie.getPoster(), 250, 370, true));

        StringBuilder genresString = new StringBuilder("|");
        for (String genre: movie.getGenres()) {
            genresString.append(genre + "|");
        }
        genres.setText(genresString.toString());
    }

    /**
     * Assigns listeners to all the buttons
     */
    private void assignButtonListeners(){
        // On click opens the browser in the IMDb page of the movie
        imdbButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(imdbURL + movie.getImdbID()));
                startActivity(browserIntent);
            }
        });

        // On click finishes the current activity in order to return to MainActivity
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // On click adds the suggested movie to the database and displays message to the user.
        // The movie added this way will be ineligible as a suggestion unless the user clears
        //  the database.
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DB_Gate.getInstance().AddMovie(movie.getTmdb_id(), movie.getTitle());
                Toast.makeText(getBaseContext(), "Movie added to your database!", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }
}
