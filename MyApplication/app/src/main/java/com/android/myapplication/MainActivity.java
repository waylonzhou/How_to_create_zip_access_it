package com.android.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class MainActivity extends AppCompatActivity {
    private Button Update;
    String testPathNew;
    private final String TAG ="OTA_UPDATE_TEST";
    File fileTest;
    //String testPath;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Update = (Button)findViewById(R.id.test);
        //checkNeedPermissions();
        testPathNew = getAPPFilePath(this);
       // testPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
        String theUpdatePath =testPathNew+"/update4.txt";
        Log.d(TAG, "onCreate: " + theUpdatePath);
        fileTest = new File(theUpdatePath);
        if (!fileTest.exists()) {
            try {
                fileTest.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            FileWriter fileWriter = new FileWriter(fileTest);
            fileWriter.write("this is the datalogic test");
            fileWriter.close();
            Log.d(TAG, "onClick: write file success");
        } catch (IOException e) {
            Log.d(TAG, "onClick: write file failed");
            e.printStackTrace();
        }
        Update.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
               // String theUpdatePathTwo = testPathNew+"/update3.zip";
                //File file = new File(theUpdatePathTwo);
               // update(file,2,0,0);
                String filepath = testPathNew;
//getFilesAllName找出目录下文件路径的所有文件名
                List<String> listfileinfo = getFilesAllName(testPathNew);
                Log.i(TAG, "handleMessage: 文件路径=" + listfileinfo);
                //批量压缩到指定文件夹下并命名为test.zip
                String finalTestPath = filepath + File.separator + "test.zip";
                try {

                    ZipUtils.zipFiles(listfileinfo, finalTestPath);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                 File myFile = new File(finalTestPath);

//                String testpath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
//                String theUpdatePath =testpath+"/update2.zip";
//                String theUpdatePathTwo ="/sdcard/Download/update2.zip";
//                Log.d(TAG, "onClick: " + theUpdatePath);
//                //File file = new File(theUpdatePath);
//                File file = new File(theUpdatePathTwo);
                update(myFile,2,0,0);
            }
        });
    }
    public  List<String> getFilesAllName(String path) {
        Log.d(TAG, "getFilesAllName: " + path);
        File file=new File(path);

        File[] files=file.listFiles();
//        for(File temp:files){
//            if(temp.isDirectory()){
//                Log.d(TAG, "getFilesAllName: " + temp.toString());
//            }
//        }
        if (files == null){Log.e(TAG,"空目录");return null;}
        List<String> s = new ArrayList<>();
        for(int i =0;i<files.length;i++){
            s.add(files[i].getAbsolutePath());
        }
        return s;
    }
    private  String getAPPFilePath(Context context) {
        String cachePath;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable()) {
            cachePath = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
        } else {
            cachePath = context.getFilesDir().getAbsolutePath();
        }
        Log.d(TAG, "getSystemFilePath: "+ cachePath);
        return cachePath;
    }
    private void checkNeedPermissions(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
            }, 1);
        }
    }
    private boolean update(File ota, int actionType, int resetType,int forceUpdate){
        Log.d(TAG, "update: ");
        Intent i = new Intent();
        i.setComponent(new ComponentName("com.datalogic.systemupdate","com.datalogic.systemupdate.SystemUpgradeService"));
        i.putExtra("action",actionType);
        i.putExtra("path",ota.getAbsolutePath());
        i.putExtra("reset",resetType);
        i.putExtra("force_update",forceUpdate);
        try {
            return getApplicationContext().startService(i) != null;
        }catch (SecurityException e){
            Log.d(TAG, "update: failed " + e.getMessage());
        }
        return false;
    }

    private  void ZipFiles(String folderString, String fileString, ZipOutputStream zipOutputSteam) throws Exception {
        Log.d(TAG, "ZipFiles: " + folderString + "\n" +
                "fileString:" + fileString + "\n==========================");
        if (zipOutputSteam == null)
            return;
        File file = new File(folderString + fileString);
        if (file.isFile()) {
            ZipEntry zipEntry = new ZipEntry(fileString);
            FileInputStream inputStream = new FileInputStream(file);
            zipOutputSteam.putNextEntry(zipEntry);
            int len;
            byte[] buffer = new byte[4096];
            while ((len = inputStream.read(buffer)) != -1) {
                zipOutputSteam.write(buffer, 0, len);
            }
            zipOutputSteam.closeEntry();
        } else {
            //文件夹
            String fileList[] = file.list();
            //没有子文件和压缩
            if (fileList.length <= 0) {
                ZipEntry zipEntry = new ZipEntry(fileString + File.separator);
                zipOutputSteam.putNextEntry(zipEntry);
                zipOutputSteam.closeEntry();
            }
            //子文件和递归
            for (int i = 0; i < fileList.length; i++) {
                ZipFiles(folderString+fileString+"/", fileList[i], zipOutputSteam);
            }
        }
    }
    public void ZipFolder(String location,String srcFileString, String zipFileString) {
        //创建ZIP
        try {
            ZipOutputStream outZip = new ZipOutputStream(new FileOutputStream(new File(zipFileString + ".zip")));
            //创建文件
            File file = new File(srcFileString);
            //压缩
            ZipFiles(file.getParent() + File.separator, file.getName(), outZip);
            //完成和关闭
            outZip.finish();
            outZip.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}