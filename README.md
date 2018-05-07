# 运行时权限

Android6.0(Api23) 推出了很多新的特性，最重要的一点就是**新的权限机制**：
新的权限机制更好的保护了用户的隐私，Google将权限分为两类，一类是Normal Permissions，这类权限一般不涉及用户隐私，是不需要用户进行授权的，比如手机震动、访问网络等；另一类是Dangerous Permission，一般是涉及到用户隐私的，需要用户进行授权，比如读取sdcard、访问通讯录等。
官方文档：https://developer.android.com/about/versions/marshmallow/android-6.0.html 
其中，Dangerous Permission在应用程序初次调用时，需要**运行时授权**的。

## 1.项目说明

本项目以调用该系统的短信发送功能为背景，分析了android6.0新增的运行时权限机制的使用方法。

## 2.BaseActivity文件的使用方法

本项目将运行时权限API封装在BaseActivity类，因此，应用程序需要实现运行时权限可以通过继承BaseActivity类来实现。

1. 触发事件需要权限是，调用performCodeWithPermission，传入参数permissionDes（权限描述）、permissionCallback（请求权限回调方法）、permissions（权限集）
2. 重写回调函数，定义授权或不授权后的操作。

```java
performCodeWithPermission("发送短信权限", new PermissionCallback() {
                @Override
                public void hasPermission() {
                    SmsManager smsManager = SmsManager.getDefault();
                    smsManager.sendTextMessage(phoneNumber, null, message, null, null);
                }

                @Override
                public void noPermission() {

                }
            }, Manifest.permission.SEND_SMS);
```

以上代码片段中，permissionDes是“发送短信权限”，PermissionCallback接口对象，复写了权限回调函数，permissions是Manifest.permission.SEND_SMS（短信权限）。

## 3.BaseActivity文件解析

1. PermissionCallback接口

```java
public interface PermissionCallback {
        void hasPermission();

        void noPermission();
    }
```

PermissionCallback接口，方法需要由子类实现，并在授权/不授权后回调方法。

2. checkPermissionGranted方法

```java
private boolean checkPermissionGranted(String[] permissions)
```

检查请求的权限集是否已经被授权

3. performCodeWithPermission方法

```java
 public void performCodeWithPermission(@NonNull String permissionDes,
                PermissionCallback runnable, @NonNull String... permissions)
```

运行时权限请求方法，子类通过调用该方法可以请求运行时权限。

4. requestPermission方法

```java
private void requestPermission(String permissionDes, final int requestCode, final String[] permissions) 
```

当检查到某项限权未授权时，调用此方法，再次请求授权

5. shouldShowRequestPermissionRationale方法

```java
private boolean shouldShowRequestPermissionRationale(String[] permissions)
```

检查再次请求的权限是否已经授权：

- 第一次请求权限时，用户拒绝了，下一次：shouldShowRequestPermissionRationale(),返回 true，应该显示一些为什么需要这个权限的说明
- 第二次请求权限时，用户拒绝了，并选择了“不在提醒”的选项时：shouldShowRequestPermissionRationale()  返回 false
- 设备的策略禁止当前应用获取这个权限的授权：shouldShowRequestPermissionRationale()  返回 false

6. 回调函数

```java
public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
```

当处理一个授权请求后，回调该方法（不管是否授权）。这里是实际执行授权/不授权回调方法的地方