package com.example.animecharacter;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private String link = "https://you-anime.ru/characters";

    private ImageView imageCharacter;
    private Button button1;
    private Button button2;
    private Button button3;
    private Button button4;

    private ArrayList<String> urlsImg;
    private ArrayList<String> names;
    private ArrayList<Button> buttons;

    private int numberOfQuestion;
    private int numberOfRightAnswer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageCharacter = findViewById(R.id.imageViewCharacter);
        button1 = findViewById(R.id.buttonCharacterName1);
        button2 = findViewById(R.id.buttonCharacterName2);
        button3 = findViewById(R.id.buttonCharacterName3);
        button4 = findViewById(R.id.buttonCharacterName4);

        buttons = new ArrayList<>();
        buttons.add(button1);
        buttons.add(button2);
        buttons.add(button3);
        buttons.add(button4);

        urlsImg = new ArrayList<>();
        names = new ArrayList<>();
        getContent();
        playGame();
    }

    public void onClickTrueCheck(View view) {
        Button button = (Button) view;

        String tag = button.getTag().toString();

        if (Integer.parseInt(tag) == numberOfRightAnswer) {
            Toast.makeText(getApplicationContext(), "Верно!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), "Неверно, правильный ответ: " + names.get(numberOfQuestion), Toast.LENGTH_SHORT).show();
        }
        playGame();
    }

    public void playGame() {
        generateQuest();
        DownloadImage task1 = new DownloadImage();

        try {
            Bitmap bitmap = task1.execute("https://you-anime.ru" + urlsImg.get(numberOfQuestion)).get();
            if (bitmap != null) {
                imageCharacter.setImageBitmap(bitmap);
            }

            for (int i = 0; i < buttons.size(); i++) {
                if (i == numberOfRightAnswer) {
                    buttons.get(i).setText(names.get(numberOfQuestion));
                } else {
                    int wrongAnswer = generateWrongAnswer();
                    buttons.get(i).setText(names.get(wrongAnswer));
                }
            }
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    public void generateQuest() {
        numberOfQuestion = (int) (Math.random() * names.size());
        numberOfRightAnswer = (int) (Math.random() * buttons.size());
    }

    public int generateWrongAnswer() {
        return (int) (Math.random() * names.size());
    }

    public void getContent() {
        DownloadResource task1 = new DownloadResource();

        String start = "<div class=\"collection-list characters\">";
        String end = "<div class=\"paging-wrapper\">";

        try {
            String content =  task1.execute(link).get();

            Pattern pattern = Pattern.compile(start + "(.*?)" + end);
            Matcher matcher = pattern.matcher(content);
            String stringWrapper = null;

            while (matcher.find()) {
                stringWrapper = matcher.group(1);
            }

            Pattern patternImg = Pattern.compile("<img src=\"(.*?)\"");
            Pattern patternName = Pattern.compile("alt=\"(.*?)\"");
            Matcher matcherImg = patternImg.matcher(stringWrapper);
            Matcher matcherName = patternName.matcher(stringWrapper);

            while (matcherName.find()) {
                names.add(matcherName.group(1));
            }
            while (matcherImg.find()) {
                urlsImg.add(matcherImg.group(1));
            }

        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static class DownloadResource extends  AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {

            URL url = null;
            URLConnection urlConnection = null;
            StringBuilder result =  new StringBuilder();

            try {
                url = new URL(strings[0]);
                urlConnection = url.openConnection();

                InputStream stream = urlConnection.getInputStream();

                InputStreamReader reader = new InputStreamReader(stream);
                BufferedReader bufferedReader = new BufferedReader(reader);
                String line = bufferedReader.readLine();

                while(line != null) {
                    result.append(line);
                    line = bufferedReader.readLine();
                }

                return result.toString();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
//                if (urlConnection != null) {
//                    urlConnection.disconnect();
//                }
            }


            return null;
        }
    }

    public static class DownloadImage extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... strings) {

            URL url = null;
            URLConnection urlConnection = null;

            try {
                url = new URL(strings[0]);
                urlConnection = url.openConnection();

                InputStream stream = urlConnection.getInputStream();
                Bitmap picture = BitmapFactory.decodeStream(stream);

                return picture;

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
//                if (urlConnection != null) {
//                   urlConnection.;
//                }
            }


            return null;
        }
    }


}
