package com.example.stt_app;


import android.os.Handler;
import android.os.Looper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import okhttp3.*;

public class MagoSttApi {

    Boolean isUploading = false;
    Boolean isBatching = false;

    // API 엔드포인트 URL
    String apiUrl;
    String id;
    String resultType = "json";
    OkHttpClient client = new OkHttpClient();
    private Handler handler;

    public MagoSttApi(String uri) {
        apiUrl = uri;
    }

    public String UpLoad(String mainAudioFile) throws IOException {
        System.out.println("1");

//        curl -X 'POST' \
//        'http://saturn.mago52.com:9003/speech2text/upload' \
//        -H 'accept: application/json' \
//        -H 'Content-Type: multipart/form-data' \
//        -F 'speech=@f24bb86a2376471b8161fa2dc0af833fwav.wav;type=audio/wav'

        // 파일 경로와 파일 이름 설정
        File audioFile = new File(mainAudioFile);
        // 파일을 바이트 배열로 읽기
        byte[] audioData = Files.readAllBytes(Paths.get(audioFile.toURI()));

        // RequestBody 생성
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("speech", audioFile.getName(), RequestBody.create(MediaType.parse("audio/*"), audioData))
                .build();

        // POST 요청 생성
        Request request = new Request.Builder()
                .url(apiUrl + "upload")
                .header("accept", "application/json")
                .post(requestBody)
                .build();

        //        {
//            "code": 701,
//                "message": "Upload success",
//                "contents": {
//            "id": "0384c54650ae460b9f30308b5e30c8a4",
//                    "detail": "1 speech(es) are uploaded to folder 0384c54650ae460b9f30308b5e30c8a4"
//        }
        id = RequestResult(request);
        System.out.println(id);
        isUploading = true;
        Batch(id);
        return id;
    }


    public String Batch(String id) throws IOException {


        // 생성된 id값을 다시 Request
        // curl -X 'POST' \
        // 'http://saturn.mago52.com:9003/speech2text/batch/' \
        // -H 'accept: application/json' \
        // -H 'Content-Type: application/json' \
        // d '{
        // "lang": "ko"
        // }'
        String message;
        // JSON 데이터 생성
        String jsonBody = "{\"lang\": \"ko\"}";

        // 요청 생성
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), jsonBody);
        Request request = new Request.Builder()
                .url(apiUrl + "batch/" + id)
                .addHeader("accept", "application/json")
                .addHeader("Content-Type", "application/json")
                .post(requestBody)
                .build();

        // 'background에서 실행중' 응답
        // {
        // "code": 712,
        // "message": "Process is running in the background",
        // "contents": {
        // "id": "0384c54650ae460b9f30308b5e30c8a4",
        // "detail": "Files in uploads/0384c54650ae460b9f30308b5e30c8a4 are being processed in the background"
        //  }
        // }
        //
        message = RequestResult(request);
        System.out.println(message);
        isBatching = true;
        GetResult();
        return message;
    }
//
    public String GetResult(){
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        final String[] result = new String[1];

        Request request = new Request.Builder()
                .url(apiUrl + "result/" + id + "?result_type=" + resultType)
                .header("accept", "application/json")
                .get()
                .build();
        Runnable task = new Runnable() {
            @Override
            public void run() {
                try {
                    result[0] = RequestResult(request);
                    System.out.println("Task completed with result: " + result[0]);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        // 주기적인 작업 스케줄링 (초기 지연 0초, 2초마다 반복)
        scheduler.scheduleAtFixedRate(task, 0, 2, TimeUnit.SECONDS);
        isUploading = false;
        isBatching = false;
        return result[0];
    }

    //비동기적 요청 실행
    public String RequestResult(Request request) throws IOException {
        System.out.println("2");
        String jsonResult = null;
        // 요청 실행
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                String responseBody = response.body().string();
                System.out.println(responseBody);
                // json파일 파싱
                jsonResult = getResultFromJson(responseBody);
                System.out.println("json파일 읽기");
            } else {
                System.out.println("API 요청 실패: " + response.code());
            }
        }
        return jsonResult;
    }

    public String getResultFromJson(String jsonResponse) {
        System.out.println("3");
        try {
            // JSON 문자열을 JSONObject로 파싱
            JSONObject jsonObject = new JSONObject(jsonResponse);
            if(isUploading == false && isBatching == false){
                // 'id' 필드의 값을 가져옴
                JSONObject contentsObject = jsonObject.getJSONObject("contents");
                String id = contentsObject.getString("id");
                System.out.println("4");
                return id;
            }
            else if(isUploading == true && isBatching == false){
                JSONObject contentsObject = jsonObject.getJSONObject("contents");
                String detail = contentsObject.getString("detail");
                System.out.println("5");
                return detail;
            }
            else if(isUploading == true && isBatching == true){
                JSONObject contentsObject = jsonObject.getJSONObject("utterances");
                String text = contentsObject.getString("text");
                System.out.println("5");
                return text;
            }
            // 두 가지 조건 모두에 해당하지 않는 경우 null 반환
            return null;
        } catch (JSONException e) {
            e.printStackTrace();
            return null; // 파싱 실패 시 예외 처리
        }
    }
}
