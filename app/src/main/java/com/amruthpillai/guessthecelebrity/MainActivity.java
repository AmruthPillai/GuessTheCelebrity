package com.amruthpillai.guessthecelebrity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    ImageView imageView;
    Button button0, button1, button2, button3;

    ArrayList<String> celebrityURLs = new ArrayList<>();
    ArrayList<String> celebrityNames = new ArrayList<>();

    int chosenCeleb = 0;
    int correctAnswerLocation = 0;
    String[] answers = new String[4];

    private class ImageDownloader extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... urls) {

            try {
                URL url = new URL(urls[0]);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.connect();

                InputStream inputStream = httpURLConnection.getInputStream();
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                return bitmap;
            } catch (Exception e) {
                e.printStackTrace();

                return null;
            }
        }
    }

    private class DownloadTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            String result= "";
            URL url;
            HttpURLConnection httpURLConnection = null;

            try {
                url = new URL(urls[0]);
                httpURLConnection = (HttpURLConnection) url.openConnection();

                InputStream inputStream = httpURLConnection.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);

                int data = inputStreamReader.read();

                while (data != -1) {
                    char current = (char) data;
                    result += current;
                    data = inputStreamReader.read();
                }

                return result;

            } catch (Exception e) {
                e.printStackTrace();

                return null;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = (ImageView) findViewById(R.id.imageView);
        button0 = (Button) findViewById(R.id.button0);
        button1 = (Button) findViewById(R.id.button1);
        button2 = (Button) findViewById(R.id.button2);
        button3 = (Button) findViewById(R.id.button3);

        DownloadTask downloadTask = new DownloadTask();
        String result = null;

        try {
            result = downloadTask.execute("http://www.posh24.se/kandisar").get();

            String[] splitResult = result.split("<div class=\"sidebarContainer\">");

            Pattern pattern = Pattern.compile("src=\"(.*?)\"");
            Matcher matcher = pattern.matcher(splitResult[0]);

            while (matcher.find()) {
                celebrityURLs.add(matcher.group(1));
            }

            Log.i("Amruth", "onCreate: "+ celebrityURLs.size());

            pattern = Pattern.compile("alt=\"(.*?)\"");
            matcher = pattern.matcher(splitResult[0]);

            while (matcher.find()) {
                celebrityNames.add(matcher.group(1));
            }

            Log.i("Amruth", "onCreate: "+ celebrityNames.size());


            Random random = new Random();
            chosenCeleb = random.nextInt(celebrityURLs.size());

            ImageDownloader imageDownloader = new ImageDownloader();
            Bitmap celebrityBitmap = imageDownloader.execute(celebrityURLs.get(chosenCeleb)).get();

            imageView.setImageBitmap(celebrityBitmap);

            correctAnswerLocation = random.nextInt(3);

            int incorrectAnswerLocation;

            for (int i = 0; i < 4; i ++) {
                if (i == correctAnswerLocation) {
                    answers[i] = celebrityNames.get(chosenCeleb);
                } else {
                    incorrectAnswerLocation = random.nextInt(celebrityURLs.size());

                    while (incorrectAnswerLocation == correctAnswerLocation)
                        incorrectAnswerLocation = random.nextInt(celebrityURLs.size());

                    answers[i] = celebrityNames.get(incorrectAnswerLocation);
                }
            }

            button0.setText(answers[0]);
            button1.setText(answers[1]);
            button2.setText(answers[2]);
            button3.setText(answers[3]);

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }
}
