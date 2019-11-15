package com.example.movieroulette;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Simple activity containing a ListView for choosing a movie genre.
 * The genres' list is retrieved from the TMDb API is a Wrapper object.
 * The user's choice is returned to the calling activity using Extras.
 */
public class GenreList extends AppCompatActivity {

    ListView genreList;
    TMDB_Wrapper tmdb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_genre_list);

        genreList = findViewById(R.id.genreList);
        tmdb = new TMDB_Wrapper();

        populateGenreList();
        AssignListenerToList();
    }


    /**
     * Retrieves the genres from the TMDb and fills the ListView with them.
     */
    private void populateGenreList(){
        HashMap<String, Integer> genreMap = tmdb.GetMovieGenres();
        ArrayList<String> genreArray = new ArrayList<>();

        for (HashMap.Entry<String, Integer> genre: genreMap.entrySet()) {
            genreArray.add(genre.getKey());
        }

        ArrayAdapter<String> genreAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, genreArray);
        genreList.setAdapter(genreAdapter);
    }

    /**
     * Assigns listener to the ListView for the event of choosing a genre by clicking on it.
     */
    private void AssignListenerToList(){
        genreList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selected = genreList.getItemAtPosition(position).toString();
                setResult(RESULT_OK, new Intent().putExtra("SelectedGenre", selected));
                finish();
            }
        });
    }
}
