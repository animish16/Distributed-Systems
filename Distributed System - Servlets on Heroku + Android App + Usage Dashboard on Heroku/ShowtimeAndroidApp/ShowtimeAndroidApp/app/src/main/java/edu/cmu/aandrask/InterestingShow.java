package edu.cmu.aandrask;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

public class InterestingShow extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        /*
         * The click listener will need a reference to this object, so that upon successfully finding a picture from Flickr, it
         * can callback to this object with the resulting picture Bitmap.  The "this" of the OnClick will be the OnClickListener, not
         * this InterestingPicture.
         */
        final InterestingShow ma = this;

        /*
         * Find the "submit" button, and add a listener to it
         */
        Button submitButton = (Button) findViewById(R.id.submit);

        // Add a listener to the send button
        submitButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View viewParam) {
                String searchTerm = ((EditText) findViewById(R.id.searchTerm)).getText().toString();
                GetShow gs = new GetShow();
                gs.search(searchTerm, ma); // Done asynchronously in another thread.  It calls ip.pictureReady() in this thread when complete.
            }
        });
    }

    /*
     * This is called by the GetPicture object when the picture is ready.  This allows for passing back the Bitmap picture for updating the ImageView
     */
    public void pictureReady(Show show) {
        // Access all the fields in the app
        ImageView showImage = findViewById(R.id.showImage);
        TextView searchView = (EditText) findViewById(R.id.searchTerm);
        TextView showFound = findViewById(R.id.showFound);
        TextView showName = findViewById(R.id.showName);
        TextView showType = findViewById(R.id.showType);
        TextView showLanguage = findViewById(R.id.showLanguage);
        TextView showGenres = findViewById(R.id.showGenres);
        TextView showStatus = findViewById(R.id.showStatus);
        TextView showRuntime = findViewById(R.id.showRuntime);
        TextView showPremiered = findViewById(R.id.showPremiered);
        TextView showRating = findViewById(R.id.showRating);
        TextView showSummary = findViewById(R.id.showSummary);
        // Read details from the show information received from Heroku API
        // and update respective fields
        if (show != null) {
            // Make fields visible
            showImage.setVisibility(View.VISIBLE);
            showFound.setVisibility(View.VISIBLE);
            showImage.setVisibility(View.VISIBLE);
            showName.setVisibility(View.VISIBLE);
            showType.setVisibility(View.VISIBLE);
            showLanguage.setVisibility(View.VISIBLE);
            showGenres.setVisibility(View.VISIBLE);
            showStatus.setVisibility(View.VISIBLE);
            showRuntime.setVisibility(View.VISIBLE);
            showPremiered.setVisibility(View.VISIBLE);
            showRating.setVisibility(View.VISIBLE);
            showSummary.setVisibility(View.VISIBLE);

            // Update values
            showFound.setText("Show found for keyword: " + searchView.getText());
            showImage.setImageBitmap(show.imageBitmap);
            showName.setText(show.name);
            showType.setText("Type:\t" + show.type);
            showLanguage.setText("Language:\t" + show.language);
            showGenres.setText("Genre/s:\t" + show.genres);
            showStatus.setText("Status:\t" + show.status);
            showRuntime.setText("Runtime (minutes):\t" + show.runtime);
            showPremiered.setText("Premiered:\t" + show.premiered);
            showRating.setText("Ratings:\t" + show.rating);
            showSummary.setText("Show Summary:\n" + show.summary);
        } else {
            // Make fields invisible
            showImage.setImageResource(R.mipmap.ic_launcher);
            showImage.setVisibility(View.INVISIBLE);
            showImage.setVisibility(View.INVISIBLE);
            showName.setVisibility(View.INVISIBLE);
            showType.setVisibility(View.INVISIBLE);
            showLanguage.setVisibility(View.INVISIBLE);
            showGenres.setVisibility(View.INVISIBLE);
            showStatus.setVisibility(View.INVISIBLE);
            showRuntime.setVisibility(View.INVISIBLE);
            showPremiered.setVisibility(View.INVISIBLE);
            showRating.setVisibility(View.INVISIBLE);
            showSummary.setVisibility(View.INVISIBLE);

            // Show failure message
            showFound.setVisibility(View.VISIBLE);
            showFound.setText("No show found for keyword: " + searchView.getText());
        }
        searchView.setText("");
        showImage.invalidate();
    }
}
