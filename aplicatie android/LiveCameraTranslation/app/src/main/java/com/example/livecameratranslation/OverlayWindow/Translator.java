package com.example.livecameratranslation.OverlayWindow;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.livecameratranslation.Boxes.WordBox;
import com.example.livecameratranslation.MainActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslator;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions;

public class Translator {
    FirebaseTranslator translator;
    boolean returnSameText;

    public Translator(Context context) {
        FirebaseApp.initializeApp(context);
        returnSameText = true;
    }

    void translate(final WordBox box) {
        if(returnSameText){
            box.translated_text = box.text;
            return;
        }

        translator.translate(box.text)
                .addOnSuccessListener(
                        new OnSuccessListener<String>() {
                            @Override
                            public void onSuccess(@NonNull String translatedText) {
                                box.translated_text = translatedText;
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                box.translated_text = box.text;
                            }
                        });
    }

    public boolean setLanguages(Integer languageFrom, Integer languageTo) {
        FirebaseTranslatorOptions options =
                new FirebaseTranslatorOptions.Builder()
                        .setSourceLanguage(languageFrom)
                        .setTargetLanguage(languageTo)
                        .build();
        translator = FirebaseNaturalLanguage.getInstance().getTranslator(options);

        FirebaseModelDownloadConditions conditions = new FirebaseModelDownloadConditions.Builder()
                .build();

        translator.downloadModelIfNeeded(conditions)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        returnSameText = false;
                    }
                })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                returnSameText = true;
                            }
                        });
        return !returnSameText;
    }

    public void setNoTranslation() {
        returnSameText = true;
    }
}
