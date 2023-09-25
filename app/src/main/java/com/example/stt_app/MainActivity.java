package com.example.stt_app;

import android.os.Bundle;
import android.os.Environment;

import android.media.MediaRecorder;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    private MediaRecorder mediaRecorder;
    private boolean isRecording = false;
    private Button recordButton;
    private EditText resultTextView;
    static String audioFile;
    private MagoSttApi mMagoSttApi = new MagoSttApi("http://saturn.mago52.com:9003/speech2text/"); // 클래스 객체 생성 & 호출

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recordButton = findViewById(R.id.recordBtn);
        resultTextView = findViewById(R.id.resultTextView);

        // permissions 클래스 객체 생성
        Permissions mPermissions = new Permissions();
        //request permission
        mPermissions.requestPermissions(this);

        //Press the Button to start & stop recording
        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isRecording) {
                    startRecording();
                } else {
                    try {
                        stopRecording();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
    }

    public String FileName() {
        // 파일 이름 랜덤 생성
        // parameters
        // Returns
        // String uuid

        String uuid = UUID.randomUUID().toString().replace("-", "");
        //System.out.println(uuid);

        return uuid;
    }

    public String createAudioFile() {
        // 오디오 파일 생성
        // parameters
        // Returns
        // String outputFilePath

        // Set output file name and path
        String uuid = FileName() + ".awb";

        // 외부 저장소 앱별 디렉터리에 파일 생성
        //String timeStamp = new SimpleDateFormat("yyMMdd_HHmm", Locale.getDefault()).format(new Date());
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_MUSIC); // 외부 저장소의 앱 전용 디렉터리에 저장, 'Music'이라는 디렉터리를 생성하고 거기에 파일 저장

        if (storageDir != null) {
            audioFile = new File(storageDir, uuid).getAbsolutePath();
            System.out.println(audioFile);
        } else {
            Toast.makeText(this, "외부 저장소 접근 불가능합니다.", Toast.LENGTH_SHORT).show();
        }
        return audioFile;
    }

    private void startRecording() {

        if (isRecording) {
            return;
        }

        try {
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);

        // 설정한 출력 포맷 및 인코더가 올바른지 확인
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_WB);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_WB);
        } catch (IllegalStateException e) {
            e.printStackTrace();
            Toast.makeText(this, "오디오 설정을 지원하지 않습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 파일 경로 생성
        String audiofile = createAudioFile();

        // setOutputFile() 메서드를 사용하여 출력 파일 경로 설정
        mediaRecorder.setOutputFile(audiofile);


        if (audiofile != null) {
            try {
                mediaRecorder.prepare();
                mediaRecorder.start();
                isRecording = true;
                recordButton.setText("녹음 중지");
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "녹음을 시작할 수 없습니다.", Toast.LENGTH_SHORT).show();
                // 녹음을 시작할 수 없을 때는 MediaRecorder를 해제해야 합니다.
                mediaRecorder.release();
            }
        } else {
            Toast.makeText(this, "오디오 파일을 생성할 수 없습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopRecording() {

        if (!isRecording) {
            return;
        }

        try {
            mediaRecorder.stop();
            mediaRecorder.release();
            isRecording = false;
            recordButton.setText("녹음 시작");
            startTranscription();
            //setTextResult();
        } catch (IllegalStateException e) {
            e.printStackTrace();
            Toast.makeText(this, "녹음을 중지할 수 없습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    // backgroud에서 MagoSttApi실행
    public void startTranscription() {
        Thread transcriptionThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                   String id = mMagoSttApi.UpLoad(audioFile);

                   String message = mMagoSttApi.Batch(id);
                   // 메시지를 UI 쓰레드에 전달하여 TextView에 설정
                   runOnUiThread(() -> resultTextView.setText(message));

                   String result = mMagoSttApi.GetResult(id);
                   // 메시지를 UI 쓰레드에 전달하여 TextView에 설정
                   runOnUiThread(() -> resultTextView.setText(result));

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        transcriptionThread.start();
    }
}
