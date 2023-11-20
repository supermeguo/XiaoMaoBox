package com.github.cattv.osc.ui.dialog;

import static androidx.core.content.ContextCompat.startActivity;
import static com.github.cattv.osc.server.ControlManager.mContext;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;


import com.daimajia.numberprogressbar.NumberProgressBar;
import com.github.cattv.osc.BuildConfig;
import com.github.cattv.osc.R;
import com.github.cattv.osc.base.BaseActivity;
import com.github.cattv.osc.bean.VersionBean;
import com.github.cattv.osc.util.DownLoadFileUtils;

import java.io.File;

public class CheckVersionDialog extends BaseDialog {
    private DialogCallback myDialogCallback;
    private VersionBean versionBean;
    private boolean isFinsh = true;
    private Context mContext;

    public void setVersionData(VersionBean versionBean) {
        this.versionBean = versionBean;
    }

    public interface DialogCallback {
        void onCancel();

        void onConfirm();
    }

    public void setMyDialogCallback(DialogCallback myDialogCallback) {
        this.myDialogCallback = myDialogCallback;

    }

    public CheckVersionDialog(@NonNull Context context) {
        super(context);
        this.mContext = context;
        setContentView(R.layout.dialog_checkupdate);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TextView tvNewVersionName = findViewById(R.id.tvNewVersionName);
        TextView tvCurrentVersionName = findViewById(R.id.tvCurrentVersionName);
        TextView tvUpdataInfo = findViewById(R.id.tvUpdataInfo);
        NumberProgressBar progressBar = findViewById(R.id.tipsProgress);
        tvNewVersionName.setText("新版本：" + versionBean.getVersionName());
        tvCurrentVersionName.setText("当前版本：" + BuildConfig.VERSION_NAME);
        tvUpdataInfo.setText("更新内容：" + versionBean.getUpdateDes());
        findViewById(R.id.tvCancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
                if (myDialogCallback != null) {
                    myDialogCallback.onCancel();
                }
            }
        });

        findViewById(R.id.tvConfirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isFinsh) {
                    Toast.makeText(mContext, "正在下载最安装包", Toast.LENGTH_LONG).show();
                    return;
                }
                isFinsh = false;
                String localPath = DownLoadFileUtils.customLocalStoragePath("CatTv");
                DownLoadFileUtils.downloadFile(mContext, versionBean.getDownLoadUrl(), localPath,
                        "CatTv", versionBean.getDownLoadUrl(), new DownLoadFileUtils.DownLoadCallback() {
                            @Override
                            public void onProgress(float progress) {
                                isFinsh = false;
                                progressBar.setProgress((int) progress);
                            }

                            @Override
                            public void onSuccess(String mBasePath) {
                                isFinsh = true;
                                dismiss();
                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                Uri apkUri;

                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                    apkUri = FileProvider.getUriForFile(mContext, "com.cattv.osc.fileprovider", new File(mBasePath));
                                } else {
                                    apkUri = Uri.fromFile(new File(mBasePath));
                                }
                                intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
//
                                mContext.startActivity(intent);

                            }
                        });
                progressBar.setVisibility(View.VISIBLE);

            }
        });
    }
}
