package com.example.wrigh.autoslideshowapp;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 100;
    Button mNextButton;
    Button mPlayButton;
    Button mPrevButton;

    //再生ボタン処理
    Handler mHandler = new Handler();
    Timer mTimer;
    int mTimerSec = 0;
    Boolean auto = false;

    //画像のURI
    List<Uri> imageUri = new ArrayList<Uri>();
    int imageUriNum = 0;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNextButton = (Button) findViewById(R.id.nextButton);
        mPlayButton = (Button) findViewById(R.id.playButton);
        mPrevButton = (Button) findViewById(R.id.prevButton);
        TextView textView = (TextView)findViewById(R.id.textView);
        Boolean os_M = false;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //パーミッションの許可確認
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                //許可されている
                getContentsInfo();
            } else {
                //許可されていなければダイアログ表示
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
            }
        }  else {
            try{
                getContentsInfo();
            }catch (SecurityException e){
                textView.setText("お使いの端末には対応していません");
                os_M =true;

            }
        }

        final ImageView imageView = (ImageView) findViewById(R.id.imageView);

        if (imageUri.size() == 0) {
            mNextButton.setEnabled(false);
            mPrevButton.setEnabled(false);
            mPlayButton.setEnabled(false);
            if(!os_M){
                textView.setText("画像が保存されていないか、画像利用を許可していません");
            }
        }
        //「進む」ボタン処理
        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (imageUriNum != imageUri.size() - 1) {
                    imageUriNum++;
                    imageView.setImageURI(imageUri.get(imageUriNum));
                } else {
                    imageView.setImageURI(imageUri.get(0));
                    imageUriNum = 0;
                }

            }
        });
        //「戻る」ボタン処理
        mPrevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (imageUriNum != 0) {
                    imageUriNum--;
                    imageView.setImageURI(imageUri.get(imageUriNum));
                } else {
                    imageUriNum = imageUri.size() - 1;
                    imageView.setImageURI(imageUri.get(imageUriNum));

                }

            }
        });

        //再生ボタン処理
        mPlayButton.setOnClickListener((new View.OnClickListener() {

            int i = 0;

            @Override
            public void onClick(View v) {

                if (!auto) {
                    mPlayButton.setText("停止");
                    mNextButton.setEnabled(false);
                    mPrevButton.setEnabled(false);
                    auto = true;
                    if (mTimer == null) {
                        //タイマー作成
                        mTimer = new Timer();
                        //始動
                        mTimer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                mTimerSec += 1;

                                mHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (mTimerSec % 2 == 0) {
                                            if (imageUriNum != imageUri.size() - 1) {
                                                imageUriNum++;
                                                imageView.setImageURI(imageUri.get(imageUriNum));
                                            } else {
                                                imageView.setImageURI(imageUri.get(0));
                                                imageUriNum = 0;
                                            }
                                            Log.d("android", "a" + i);
                                            mTimerSec = 0;
                                            i++;
                                        }

                                    }
                                });
                            }
                        }, 1000, 1000);

                    }
                } else {
                    mPlayButton.setText("再生");

                    mNextButton.setEnabled(true);
                    mPrevButton.setEnabled(true);
                    mTimer.cancel();
                    mTimer = null;
                    mTimerSec = 0;
                    auto = false;
                }

            }
        }));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getContentsInfo();
                }
                break;
            default:
                break;
        }
    }


    private void getContentsInfo() {
        //画像情報の取得。初回だけ。
        ContentResolver resolver = getContentResolver();
        Cursor cursor = resolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                int fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID);
                Long id = cursor.getLong(fieldIndex);
                Uri imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
                //ArrayListに入れてます。
                this.imageUri.add(imageUri);
            } while (cursor.moveToNext());

        }
        ImageView imageView = (ImageView) findViewById(R.id.imageView);

        Log.d("android","" + imageUri.size());
        mNextButton.setEnabled(true);
        mPrevButton.setEnabled(true);
        mPlayButton.setEnabled(true);
        imageView.setImageURI(imageUri.get(imageUriNum));
        cursor.close();
    }
}