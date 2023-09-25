package com.example.stt_app;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class Permissions extends AppCompatActivity {

    private static final int REQUEST_PERMISSION_CODE = 101;
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private static final int REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION = 201;

    //Request permissions
    public void requestPermissions(Activity activity) {
        //Check the version, API 26 or higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String[] permissions = {
                    android.Manifest.permission.RECORD_AUDIO,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            };

            boolean allPermissionsGranted = true; // Authorization status

            //Check current permission status
            for (String permission : permissions) {
                //If permission is not granted
                if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false; //Set to 'false' if permission is not granted
                    ActivityCompat.requestPermissions(activity, new String[]{permission},
                            getPermissionRequestCode(permission)); //Request permissions
                }
            }
            //Check with log if all permissions have been granted
            if (allPermissionsGranted) {
                Log.d("TAG", "Permission is granted!");
            }
        }
    }

    //Permission request code
    private int getPermissionRequestCode(String permission) {
        switch (permission) {
            case android.Manifest.permission.RECORD_AUDIO:
                return REQUEST_RECORD_AUDIO_PERMISSION;
            case android.Manifest.permission.WRITE_EXTERNAL_STORAGE:
                return REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION;
            default:
                return REQUEST_PERMISSION_CODE;
        }
    }

    // Handle permission request results
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_RECORD_AUDIO_PERMISSION:
                handlePermissionResult(grantResults, android.Manifest.permission.RECORD_AUDIO, "Audio_Permission");
                break;

            case REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION:
                handlePermissionResult(grantResults, Manifest.permission.WRITE_EXTERNAL_STORAGE, "Write_Storage_Permission");
                break;

            case REQUEST_PERMISSION_CODE:
                handleMultiplePermissionResult(grantResults, "Multiple_Permissions");
                break;
        }
    }

    // 권한 요청을 거절했을 경우 직접 설정하도록 요청
    private void handlePermissionResult(int[] grantResults, String permissionName, String logMessage) {
        // grantResults는 사용자가 권한 요청에 대한 응답으로 받은 결과
        // PackageManager.PERMISSION_GRANTED는 Android 시스템에서 제공하는 상수, 권한이 부여되면 '0'
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d("TAG", logMessage + " is granted!");
        } else {
            Toast.makeText(this, "설정 앱으로 가서 " + permissionName + " 권한을 활성화해주세요.", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleMultiplePermissionResult(int[] grantResults, String logMessage) {
        handlePermissionResult(grantResults, logMessage, logMessage);
    }

}
