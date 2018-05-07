package com.glandroid.smssender;

import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

/**
 * @author Kevin
 * @version 1.0
 * @des Android6.0 运行时权限控制代码的通用类（工具类）
 * @updateAuthor Kevin
 * @updateDes 2018-1-25
 */
public class BaseActivity extends AppCompatActivity {

    private int permissionRequestCode = 88;

    private PermissionCallback permissionRunnable;

    public interface PermissionCallback {
        void hasPermission();

        void noPermission();
    }

    /**
     * Android M运行时权限请求封装
     *
     * @param permissionDes 权限描述
     * @param runnable      请求权限回调
     * @param permissions   请求的权限（数组类型），直接从Manifest中读取相应的值，比如Manifest.permission.WRITE_CONTACTS
     */
    public void performCodeWithPermission(@NonNull String permissionDes,
                                          PermissionCallback runnable, @NonNull String... permissions) {
        if (permissions == null || permissions.length == 0)
            return;
            //this.permissionrequestCode = requestCode;
        this.permissionRunnable = runnable;

        if ((Build.VERSION.SDK_INT < Build.VERSION_CODES.M) || checkPermissionGranted(permissions)) {
            if (permissionRunnable != null) {
                permissionRunnable.hasPermission();
                permissionRunnable = null;
            }
        } else {
            //permission has not been granted.
            // 权限未被授予
            requestPermission(permissionDes, permissionRequestCode, permissions);
        }

    }

    /**
     * 检查某项功能是否被授权
     *
     * @param permissions 授权队列
     * @return true表示已授权，false表示未授权
     */
    private boolean checkPermissionGranted(String[] permissions) {
        boolean flag = true;
        for (String p : permissions) {
            if (ActivityCompat.checkSelfPermission(this, p) != PackageManager.PERMISSION_GRANTED) {
                flag = false;
                break;
            }
        }
        return flag;
    }

    /**
     * 当用户第一次拒绝授权后，第二次请求时弹出提示框再次请求授权
     *
     * 1. 第一次请求权限时，用户拒绝了，下一次：shouldShowRequestPermissionRationale(),返回 true，应该显示一些为什么需要这个权限的说明
     * 2. 第二次请求权限时，用户拒绝了，并选择了“不在提醒”的选项时：shouldShowRequestPermissionRationale()  返回 false
     * 3. 设备的策略禁止当前应用获取这个权限的授权：shouldShowRequestPermissionRationale()  返回 false
     * @param permissionDes  授权描述
     * @param requestCode  请求码
     * @param permissions  授权队列
     */
    private void requestPermission(String permissionDes, final int requestCode,
                                   final String[] permissions) {
        if (shouldShowRequestPermissionRationale(permissions)) {
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // For example, if the request has been denied previously.

            //            Snackbar.make(getWindow().getDecorView(), requestName,
            //                    Snackbar.LENGTH_INDEFINITE)
            //                    .setAction(R.string.common_ok, new View.OnClickListener() {
            //                        @Override
            //                        public void onClick(View view) {
            //                            ActivityCompat.requestPermissions(BaseAppCompatActivity.this,
            //                                    permissions,
            //                                    requestCode);
            //                        }
            //                    })
            //                    .show();
            //如果用户之前拒绝过此权限，再提示一次准备授权相关权限
            new AlertDialog.Builder(this)
                    .setTitle("提示")
                    .setMessage(permissionDes)
                    .setPositiveButton("授权", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(BaseActivity.this, permissions, requestCode);
                        }
                    }).show();

        } else {
            // Contact permissions have not been granted yet. Request them directly.
            // 权限仍然未被授予，直接进行为授权请求（当然是无法使用功能的）
            ActivityCompat.requestPermissions(BaseActivity.this, permissions, requestCode);
        }
    }

    /**
     *  第二次请求授权是否成功
     *
     * @param permissions 授权队列
     * @return false表示未授权，true表示授权
     */
    private boolean shouldShowRequestPermissionRationale(String[] permissions) {
        boolean flag = false;
        for (String p : permissions) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, p)) {
                flag = true;
                break;
            }
        }
        return flag;
    }

    /**
     * 当一个授权请求被处理时（不管是否允许），回调该函数
     *
     * @param requestCode 请求码
     * @param permissions 授权队列
     * @param grantResults  授权结果
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == permissionRequestCode) {
            if (verifyPermissions(grantResults)) {
                if (permissionRunnable != null) {
                    permissionRunnable.hasPermission();
                    permissionRunnable = null;
                }
            } else {
                Toast.makeText(this, "暂无权限执行相关操作！", Toast.LENGTH_SHORT).show();
                if (permissionRunnable != null) {
                    permissionRunnable.noPermission();
                    permissionRunnable = null;
                }
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }

    }

    /**
     * 判断授权结果
     *
     * @param grantResults 授权结果
     * @return 通过返回true，不通过返回false
     */
    public boolean verifyPermissions(int[] grantResults) {
        // At least one result must be checked.
        if (grantResults.length < 1) {
            return false;
        }

        // Verify that each required permission has been granted, otherwise return false.
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }
}
