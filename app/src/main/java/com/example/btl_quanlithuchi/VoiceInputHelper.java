package com.example.btl_quanlithuchi;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.widget.Toast;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.util.ArrayList;

public class VoiceInputHelper {

    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 1001;
    private static final int REQUEST_SPEECH_RECOGNITION = 1002;

    private Context context;
    private Activity activity;
    private VoiceListener listener;

    public interface VoiceListener {
        void onVoiceResult(String text);
        void onVoiceError(String message);
        void onListeningStarted();
        void onListeningStopped();
    }

    public VoiceInputHelper(Context context, Activity activity) {
        this.context = context;
        this.activity = activity;
    }

    public void setListener(VoiceListener listener) {
        this.listener = listener;
    }

    public boolean checkPermission() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    REQUEST_RECORD_AUDIO_PERMISSION);
            return false;
        }
        return true;
    }

    public void startListening() {
        if (!checkPermission()) {
            if (listener != null) listener.onVoiceError("Cần cấp quyền ghi âm");
            return;
        }

        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            if (listener != null) listener.onVoiceError("Thiết bị không hỗ trợ nhận diện giọng nói");
            return;
        }

        if (listener != null) listener.onListeningStarted();

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "vi-VN");
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Nói giao dịch của bạn...");
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);

        try {
            activity.startActivityForResult(intent, REQUEST_SPEECH_RECOGNITION);
        } catch (Exception e) {
            if (listener != null) listener.onVoiceError("Không thể mở nhận diện giọng nói");
        }
    }

    public void handleActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SPEECH_RECOGNITION) {
            if (listener != null) listener.onListeningStopped();

            if (resultCode == Activity.RESULT_OK && data != null) {
                ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                if (results != null && !results.isEmpty() && listener != null) {
                    listener.onVoiceResult(results.get(0));
                } else if (listener != null) {
                    listener.onVoiceError("Không có kết quả nhận diện");
                }
            } else if (listener != null) {
                listener.onVoiceError("Nhận diện bị hủy hoặc lỗi");
            }
        }
    }

    public void handlePermissionResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startListening();
            } else {
                if (listener != null) listener.onVoiceError("Từ chối quyền ghi âm");
                Toast.makeText(context, "Cần quyền ghi âm để sử dụng tính năng này", Toast.LENGTH_SHORT).show();
            }
        }
    }
}