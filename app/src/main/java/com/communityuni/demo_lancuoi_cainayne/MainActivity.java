package com.communityuni.demo_lancuoi_cainayne;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.repackaged.org.apache.commons.codec.binary.Base64;
import com.google.api.services.speech.v1beta1.Speech;
import com.google.api.services.speech.v1beta1.SpeechRequestInitializer;
import com.google.api.services.speech.v1beta1.model.RecognitionAudio;
import com.google.api.services.speech.v1beta1.model.RecognitionConfig;
import com.google.api.services.speech.v1beta1.model.SpeechRecognitionResult;
import com.google.api.services.speech.v1beta1.model.SyncRecognizeRequest;
import com.google.api.services.speech.v1beta1.model.SyncRecognizeResponse;

import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.util.Locale;
import java.util.UUID;
public class MainActivity extends AppCompatActivity {
    private final String CLOUD_API_KEY_SPEECH= "AIzaSyD9UhPypUfskSBPPqO6o6WCKYuKbcvfy8M";
    private final String CLOUD_API_KEY_TEXT= "AIzaSyCXFa_eeu_R8-bPWBqe9gmmyIMxYohDH5Y";
    ImageView imgSpeech, imgText;
    TextView txtSpeech;
    EditText edtText;
    private static final int RESULT=1;
    MediaRecorder mediaRecorder;
    MediaPlayer mediaPlayer;
    String pathSave="";
    final int REQUEST_PERMISSION_CODE=1000;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        addControls();
        addEvents();
    }
    private void addControls() {
        imgSpeech= findViewById(R.id.imgThu);
        imgText= findViewById(R.id.imgPhat);
        txtSpeech= findViewById(R.id.txtSpeech);
        edtText= findViewById(R.id.edtText);

    }
    private void addEvents() {

        imgSpeech.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog dialog= new Dialog(MainActivity.this);
                dialog.setContentView(R.layout.item);
                dialog.setCanceledOnTouchOutside(false);
                dialog.show();
                pathSave= Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+"_audio_record.3gp";
                setupMediaRecord();
                try{
                    mediaRecorder.prepare();
                    mediaRecorder.start();
                }
                catch (Exception ex){
                    ex.printStackTrace();
                }
                Toast.makeText(MainActivity.this,"Đang ghi...",Toast.LENGTH_LONG).show();


                Button btnKetThuc= dialog.findViewById(R.id.btnStop);
                btnKetThuc.setOnClickListener(new View.OnClickListener() {
                        @Override public void onClick(View v) {
                            mediaRecorder.stop();
                            mediaRecorder.release();
                            Toast.makeText(MainActivity.this,"Done...",Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        }
                    });
                final Uri media= MediaStore.Audio.Media.getContentUriForPath(pathSave);
                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                        try{
                            InputStream stream= getContentResolver().openInputStream(media);
                            byte[] audioData= IOUtils.toByteArray(stream);
                            stream.close();
                            String base64EncodedData= Base64.encodeBase64String(audioData);
                            // sử dụng key API
                            Speech speechService= new Speech.Builder(AndroidHttp.newCompatibleTransport(),
                                    new AndroidJsonFactory(),null)
                                    .setSpeechRequestInitializer(new SpeechRequestInitializer(CLOUD_API_KEY_SPEECH)).build();
                            //Thiết lập ngôn ngữ cho text
                            RecognitionConfig recognitionConfig= new RecognitionConfig();
                            recognitionConfig.setLanguageCode(Locale.getDefault()+"");
                            //đưa chuỗi âm thanh sang dạng base64
                            RecognitionAudio recognitionAudio= new RecognitionAudio();
                            recognitionAudio.setContent(base64EncodedData);
                            //trích dẫn text
                            SyncRecognizeRequest request= new SyncRecognizeRequest();
                            request.setConfig(recognitionConfig);
                            request.setAudio(recognitionAudio);
                            SyncRecognizeResponse response= speechService.speech().syncrecognize(request)
                                    .execute();
                            SpeechRecognitionResult result= response.getResults().get(0);
                            final String textxuat= result.getAlternatives().get(0).getTranscript();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    txtSpeech.setText(textxuat);
                                }
                            });


                        }
                        catch (Exception ex) {}
                    }
                });



            }
        });

        imgText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });





    }
    private void requestPermission() {
        ActivityCompat.requestPermissions(this,new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO
        },REQUEST_PERMISSION_CODE);
    }

    private boolean checkPermissionFromDevice() {
        int write_extenal_storage_result= ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int record_audio_result= ContextCompat.checkSelfPermission(this,Manifest.permission.RECORD_AUDIO);
        return write_extenal_storage_result== PackageManager.PERMISSION_GRANTED &&
                record_audio_result==PackageManager.PERMISSION_GRANTED;
    }
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case REQUEST_PERMISSION_CODE:
            {
                if(grantResults.length>0 &&grantResults[0]==PackageManager.PERMISSION_GRANTED)
                {
                    Toast.makeText(this,"Đang thu giọng nói!",Toast.LENGTH_LONG).show();
                }
                else
                {
                    Toast.makeText(this,"Đã thu thành công!",Toast.LENGTH_LONG).show();
                }
            }break;
        }
    }
    private void setupMediaRecord() {
        mediaRecorder=new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
        mediaRecorder.setOutputFile(pathSave);
    }


}
