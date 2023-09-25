package com.example.stt_app;


import android.os.Handler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MagoSttApi {

    Boolean isUploading = false;
    Boolean isBatching = false;

    // API 엔드포인트 URL
    String apiUrl;
    String resultType = "json";
    OkHttpClient client = new OkHttpClient();

    // 클래스 생성자
    public MagoSttApi(String uri) {
        apiUrl = uri;
    }

    public String UpLoad(String mainAudioFile) throws IOException {

        // 파일 업로드 api 요청
        // parameters
        // String mainAudioFile
        // Returns
        // String id

        // 요청
//        curl -X 'POST' \
//        'http://saturn.mago52.com:9003/speech2text/upload' \
//        -H 'accept: application/json' \
//        -H 'Content-Type: multipart/form-data' \
//        -F 'speech=@f24bb86a2376471b8161fa2dc0af833fwav.wav;type=audio/wav'

        // 응답
//        {
//            "code": 701,
//                "message": "Upload success",
//                "contents": {
//            "id": "0384c54650ae460b9f30308b5e30c8a4",
//                    "detail": "1 speech(es) are uploaded to folder 0384c54650ae460b9f30308b5e30c8a4"
//        }

        // 파일 경로
        File audioFile = new File(mainAudioFile);
        // 파일을 바이트 배열로 읽기
        byte[] audioData = Files.readAllBytes(Paths.get(audioFile.toURI()));

        // RequestBody 생성,  HTTP 요청 바디에 들어갈 데이터
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("speech", audioFile.getName(), RequestBody.create(MediaType.parse("audio/*"), audioData))
                .build();

        // POST 요청 생성, HTTP 요청 자체
        Request request = new Request.Builder()
                .url(apiUrl + "upload")
                .header("accept", "application/json")
                .post(requestBody)
                .build();

        // RequestResult를 통해 return 받은 json 파일에서 id값 받아옴
        String id = RequestResult(request);
        System.out.println(id);
        isUploading = true;
        return id;
    }


    public String Batch(String id) throws IOException {

        // 생성된 id값 다시 Request
        // parameters
        // String id
        // Returns
        // String message

        // curl -X 'POST' \
        // 'http://saturn.mago52.com:9003/speech2text/batch/' \
        // -H 'accept: application/json' \
        // -H 'Content-Type: application/json' \
        // d '{
        // "lang": "ko"
        // }'

        // {
        // "code": 712,
        // "message": "Process is running in the background",
        // "contents": {
        // "id": "0384c54650ae460b9f30308b5e30c8a4",
        // "detail": "Files in uploads/0384c54650ae460b9f30308b5e30c8a4 are being processed in the background"
        //  }
        // }
        //

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

        // 'background에서 실행중' message 리턴
        message = RequestResult(request);
        System.out.println(message);
        isBatching = true;
        return message;
    }

    public String GetResult(String id){

        // 생성된 id값 다시 Request
        // parameters
        // String id
        // Returns
        // String result

        // curl -X 'GET' \
        // 'http://saturn.mago52.com:9003/speech2text/result/d99f70e373c545f383095cf36f9ece4c?result_type=json' \
        // -H 'accept: application/json'

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        final String[] result = new String[1]; // 단일 변수로 선언

        Request request = new Request.Builder()
                .url(apiUrl + "result/" + id + "?result_type=" + resultType)
                .header("accept", "application/json")
                .get()
                .build();

        // 주기적인 작업 스케줄링 (초기 지연 0초, 2초마다 반복)
        scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    result[0] = RequestResult(request); // 변수로 직접 할당
                    System.out.println("Task completed with result: " + result[0]);
                    // 만약 원하는 조건을 만족하면 작업 종료
                    if (result[0] != null) {
                        scheduler.shutdown(); // 작업 종료
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 0, 2, TimeUnit.SECONDS);

        // 작업이 완료되기 전까지 여기서 대기하거나, 결과를 기다리지 않고 바로 반환할 수 있습니다.
        return result[0];
    }

    // 요청 실행 & 응답 json파일 파싱
    public String RequestResult(Request request) throws IOException {

        // 요청 execute
        // parameters
        // String request
        // Returns
        // String jsonResult

        String jsonResult = null;
        // 요청 실행
        try (Response response = client.newCall(request).execute()) {

            if (response.isSuccessful()) {
                String responseBody = response.body().string();
                System.out.println(responseBody);
                // json파일 파싱
                jsonResult = getResultFromJson(responseBody);

            } else {
                System.out.println("API 요청 실패: " + response.code());
            }
        }
        return jsonResult;
    }

    public String getResultFromJson(String jsonResponse) {

        // 요청 execute
        // parameters
        // String jsonResponse
        // Returns
        // String id || message || text, 모두 해당하지 않을 경우 null return

        try {
            // JSON 문자열을 JSONObject로 파싱
            JSONObject jsonObject = new JSONObject(jsonResponse);

            if(isUploading == false && isBatching == false){
                // 'id' 필드의 값을 가져옴
                JSONObject contentsObject = jsonObject.getJSONObject("contents");
                String id = contentsObject.getString("id");
                return id;
            }
            else if(isUploading == true && isBatching == false){
                String message = jsonObject.getString("message");
                return message;
            }
            else if(isUploading == true && isBatching == true){

                // "contents" 객체 가져오기
                JSONObject contentsObject = jsonObject.getJSONObject("contents");
                // "results" 객체 가져오기
                JSONObject resultsObject = contentsObject.getJSONObject("results");
                // "text" 필드의 값을 가져오기
                String text = resultsObject.getJSONArray("utterances")
                        .getJSONObject(0)
                        .getString("text");
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
