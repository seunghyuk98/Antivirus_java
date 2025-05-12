package com.happ.antivirus_java.ui.apk;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.happ.antivirus_java.databinding.FragmentApkBinding;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ApkFragment extends Fragment {

    private FragmentApkBinding binding;
    private List<String> lastScannedPermissions = Collections.emptyList();

    private String getFriendlyPermissionName(String permission) {
        if (permission.contains("READ_CONTACTS")) return "📇 연락처 접근 권한";
        if (permission.contains("CAMERA")) return "📸 카메라 사용 권한";
        if (permission.contains("INTERNET")) return "🌐 인터넷 사용 권한";
        if (permission.contains("ACCESS_FINE_LOCATION")) return "📍 정확한 위치 정보 권한";
        if (permission.contains("ACCESS_COARSE_LOCATION")) return "📍 대략적인 위치 정보 권한";
        if (permission.contains("READ_EXTERNAL_STORAGE")) return "📁 외부 저장소 읽기 권한";
        if (permission.contains("WRITE_EXTERNAL_STORAGE")) return "✏️ 외부 저장소 쓰기 권한";
        if (permission.contains("CALL_PHONE")) return "📞 전화 걸기 권한";
        if (permission.contains("RECORD_AUDIO")) return "🎙️ 마이크 사용 권한";
        return "❓ 기타 권한: " + permission;
    }

    private String getPermissionDetail(String permission) {
        if (permission.contains("READ_CONTACTS"))
            return "📇 연락처 접근 권한\n- 연락처를 읽을 수 있습니다.\n- 악성앱은 사용자의 지인 정보를 무단 수집하거나 스팸 전송에 악용할 수 있습니다.\n";
        if (permission.contains("CAMERA"))
            return "📸 카메라 사용 권한\n- 사진 또는 영상을 촬영할 수 있습니다.\n- 악성앱은 사용자의 동의 없이 촬영하거나 감시 목적으로 사용할 수 있습니다.\n";
        if (permission.contains("INTERNET"))
            return "🌐 인터넷 사용 권한\n- 네트워크를 통해 외부 서버와 통신할 수 있습니다.\n- 악성앱은 사용자 정보를 외부로 전송하는 데 사용할 수 있습니다.\n";
        if (permission.contains("ACCESS_FINE_LOCATION"))
            return "📍 정확한 위치 정보 권한\n- 현재 위치를 GPS로 추적할 수 있습니다.\n- 악성앱은 사용자의 실시간 이동 경로를 추적하거나 위치 기반 광고에 활용할 수 있습니다.\n";
        if (permission.contains("ACCESS_COARSE_LOCATION"))
            return "📍 대략적인 위치 정보 권한\n- 기지국 등을 이용한 대략적인 위치 확인이 가능합니다.\n";
        if (permission.contains("READ_EXTERNAL_STORAGE"))
            return "📁 외부 저장소 읽기 권한\n- 사용자의 사진, 문서 등 파일 접근이 가능합니다.\n- 악성앱은 개인 사진이나 파일을 무단으로 읽을 수 있습니다.\n";
        if (permission.contains("WRITE_EXTERNAL_STORAGE"))
            return "✏️ 외부 저장소 쓰기 권한\n- 파일을 생성하거나 수정할 수 있습니다.\n- 악성앱은 파일을 조작하거나 랜섬웨어처럼 변조할 수 있습니다.\n";
        if (permission.contains("CALL_PHONE"))
            return "📞 전화 걸기 권한\n- 직접 전화를 걸 수 있습니다.\n- 악성앱은 유료 전화번호로 자동 연결해 과금 피해를 줄 수 있습니다.\n";
        if (permission.contains("RECORD_AUDIO"))
            return "🎙️ 마이크 사용 권한\n- 소리를 녹음할 수 있습니다.\n- 악성앱은 사용자의 대화를 몰래 녹음해 사생활 침해가 우려됩니다.\n";
        return "❓ 기타 권한: " + permission + "\n- 이 권한에 대한 정보가 부족합니다.\n";
    }

    private ActivityResultLauncher<String> apkPickerLauncher;

    public ApkFragment() {
        // ViewModel 초기화
//        apkViewModel = new ViewModelProvider(this).get(ApkViewModel.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentApkBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initActivityResult();
        setEvent();
    }

    private void setEvent() {
        setupListener();

        binding.btnShowDialog.setOnClickListener(v -> {
            if (!lastScannedPermissions.isEmpty()) {
                showPermissionDialog(lastScannedPermissions);
            }
        });
    }

    private void setupListener() {
        binding.btApk.setOnClickListener(v -> {
            apkPickerLauncher.launch("application/vnd.android.package-archive");
        });
    }

    private void initActivityResult() {
        apkPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        extractPermissionsFromApk(uri);
                    }
                }
        );
    }

    private void extractPermissionsFromApk(@NonNull Uri uri) {
        try {
            File tempFile = new File(requireContext().getCacheDir(), "selected_apk.apk");

            InputStream input = requireContext().getContentResolver().openInputStream(uri);
            if (input != null) {
                OutputStream output = new FileOutputStream(tempFile);
                byte[] buffer = new byte[4096];
                int read;
                while ((read = input.read(buffer)) != -1) {
                    output.write(buffer, 0, read);
                }
                input.close();
                output.close();
            }

            PackageManager pm = requireActivity().getPackageManager();
            PackageInfo packageInfo = pm.getPackageArchiveInfo(tempFile.getAbsolutePath(), PackageManager.GET_PERMISSIONS);

            List<String> permissions;
            if (packageInfo != null && packageInfo.requestedPermissions != null) {
                permissions = Arrays.asList(packageInfo.requestedPermissions);
            } else {
                permissions = Collections.emptyList();
            }

            // 텍스트뷰에 유저 친화적 표시
            StringBuilder detailText = new StringBuilder();
            for (String perm : permissions) {
                detailText.append(getPermissionDetail(perm)).append("\n");
            }
            binding.tvPermission.setText(detailText.toString());

            // 위험도 다이얼로그 띄우기
            showPermissionDialog(permissions);

            lastScannedPermissions = permissions;

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showPermissionDialog(List<String> permissions) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(requireContext());
        builder.setTitle("앱 권한 및 위험도 분석");

        StringBuilder message = new StringBuilder();
        message.append(getRiskLevel(permissions)).append("\n\n");

        for (String perm : permissions) {
            message.append("• ").append(getFriendlyPermissionName(perm)).append("\n");
        }

        builder.setMessage(message.toString());
        builder.setPositiveButton("확인", null);
        builder.show();
    }


    private String getRiskLevel(List<String> permissions) {
        boolean hasDangerous = false;
        boolean hasCritical = false;

        for (String perm : permissions) {
            if (perm.contains("CALL_PHONE") || perm.contains("SEND_SMS") || perm.contains("RECORD_AUDIO")) {
                hasCritical = true;
            } else if (perm.contains("READ_CONTACTS") || perm.contains("ACCESS_FINE_LOCATION") || perm.contains("CAMERA")) {
                hasDangerous = true;
            }
        }

        if (hasCritical) return "⚠️ 고위험: 개인정보 유출 가능성이 있는 앱일 수 있습니다.";
        if (hasDangerous) return "⚠️ 중위험: 민감 정보 접근이 필요한 앱일 수 있습니다.";
        return "✅ 저위험: 일반적인 권한만을 요청하는 앱입니다.";
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
