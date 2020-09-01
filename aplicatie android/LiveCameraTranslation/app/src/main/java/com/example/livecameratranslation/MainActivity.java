package com.example.livecameratranslation;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.DialogFragment;

import com.example.livecameratranslation.Camera.Camera;
import com.example.livecameratranslation.OverlayWindow.Translator;
import com.example.livecameratranslation.TextFinder.FrameAnalyzer;
import com.example.livecameratranslation.OverlayWindow.Overlay;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage;

import org.opencv.android.OpenCVLoader;

import java.sql.Time;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Set;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class MainActivity extends AppCompatActivity {

    public static final int REQUEST_CAMERA_PERMISSION_RESULT = 0;
    private Camera camera;
    private FrameAnalyzer frameAnalyzer;
    private Overlay overlay;
    private Translator translator;
    private ArrayList<String> availableLanguagesFrom;
    private ArrayList<String> availableLanguagesTo;
    private ArrayList<Integer> availableLanguagesFirebaseCode;
    private int selectedLanguageFrom;
    private int selectedLanguageTo;

    static {
        if (!OpenCVLoader.initDebug())
            Log.d("ERROR", "Unable to load OpenCV");
        else
            Log.d("SUCCESS", "OpenCV loaded");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);//will hide the title
        getSupportActionBar().hide();

        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);

        addLanguages();

        ConstraintLayout layout = findViewById(R.id.layout);

        translator = new Translator(this);

        overlay = new Overlay(this,translator);
        layout.addView(overlay);

        frameAnalyzer = new FrameAnalyzer(this,overlay);
        frameAnalyzer.start();

        Button goButton = findViewById(R.id.button);

        goButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLanguageSelectorDialog();
            }
        });

        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            DialogFragment newFragment = new ErrorAlertDialog("This application requires Android version 5.0 or higher.");
            newFragment.show(getSupportFragmentManager(), "version_error");
        } else {
            camera = new Camera(this,frameAnalyzer,overlay);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (camera != null)
            camera.resume();
        frameAnalyzer.resumeAnalizer();
    }

    @Override
    protected  void onPause() {
        frameAnalyzer.pauseAnalizer();
        frameAnalyzer.interrupt();
        if (camera != null)
            camera.pause();
        super.onPause();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,int[] grantResult) {
        if(requestCode == REQUEST_CAMERA_PERMISSION_RESULT) {
            if(grantResult[0] != PackageManager.PERMISSION_GRANTED) {
                DialogFragment newFragment = new ErrorAlertDialog("App will not run without camera services.");
                newFragment.show(getSupportFragmentManager(), "camera_permission_error");
            }
        }
    }

    private void addLanguages() {
        selectedLanguageFrom = -1;
        selectedLanguageTo = -1;
        availableLanguagesFrom= new ArrayList<>();
        availableLanguagesTo= new ArrayList<>();
        availableLanguagesFirebaseCode= new ArrayList<>();

        availableLanguagesFrom.add("Romanian"); availableLanguagesTo.add("Romanian"); availableLanguagesFirebaseCode.add(FirebaseTranslateLanguage.RO);
        availableLanguagesFrom.add("English"); availableLanguagesTo.add("English"); availableLanguagesFirebaseCode.add(FirebaseTranslateLanguage.EN);

        availableLanguagesTo.add("German"); availableLanguagesFirebaseCode.add(FirebaseTranslateLanguage.DE);
        availableLanguagesTo.add("Thai"); availableLanguagesFirebaseCode.add(FirebaseTranslateLanguage.TH);
    }

    private void showLanguageSelectorDialog() {
        final int selectedLanguageToAux = selectedLanguageTo;
        final int selectedLanguageFromAux = selectedLanguageFrom;

        View dialogView = LayoutInflater.from(this).inflate(R.layout.choose_languages_layout, null, false);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        builder.setNeutralButton("None", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selectedLanguageFrom = -1;
                        selectedLanguageTo = -1;
                        translator.setNoTranslation();
                    }
                })
                .setPositiveButton("Go", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        boolean succeded = translator.setLanguages(availableLanguagesFirebaseCode.get(selectedLanguageFrom),availableLanguagesFirebaseCode.get(selectedLanguageTo));
                        if(!succeded) {
                            selectedLanguageFrom = -1;
                            selectedLanguageTo = -1;
                            //Toast.makeText(getApplicationContext(), "Could not download language model!", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selectedLanguageTo = selectedLanguageToAux;
                        selectedLanguageFrom = selectedLanguageFromAux;
                    }
                });

        final AlertDialog alertDialog = builder.create();
        alertDialog.setCancelable(false);
        alertDialog.show();
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);

        final ListView listFrom = (ListView) ((AlertDialog) alertDialog).findViewById(R.id.languageFrom);
        final ArrayAdapter<String> adapterFrom = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1,
                availableLanguagesFrom);
        listFrom.setAdapter(adapterFrom);

        final ListView listTo = (ListView) ((AlertDialog) alertDialog).findViewById(R.id.languageTo);
        ArrayAdapter<String> adapterTo = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1,
                availableLanguagesTo);
        listTo.setAdapter(adapterTo);

        listFrom.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(selectedLanguageFrom != position)
                    selectedLanguageFrom = position;
                else
                    selectedLanguageFrom = -1;
                setDialogChoices();
            }

            void setDialogChoices() {
                for (int i=0;i<availableLanguagesFrom.size();i++){
                    if(i==selectedLanguageFrom){
                        listFrom.getChildAt(i).setBackgroundColor(Color.BLUE);
                    }else{
                        listFrom.getChildAt(i).setBackgroundColor(Color.WHITE);
                    }
                }

                if(selectedLanguageFrom!=-1 && selectedLanguageTo!=-1 && selectedLanguageFrom!=selectedLanguageTo)
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                else
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
            }
        });

        listTo.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(selectedLanguageTo != position)
                    selectedLanguageTo = position;
                else
                    selectedLanguageTo = -1;
                setDialogChoices();
            }

            void setDialogChoices() {
                for (int i=0;i<availableLanguagesTo.size();i++){
                    if(i==selectedLanguageTo){
                        listTo.getChildAt(i).setBackgroundColor(Color.BLUE);
                    }else{
                        listTo.getChildAt(i).setBackgroundColor(Color.WHITE);
                    }
                }

                if(selectedLanguageFrom!=-1 && selectedLanguageTo!=-1 && selectedLanguageFrom!=selectedLanguageTo)
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                else
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
            }
        });
    }
}
