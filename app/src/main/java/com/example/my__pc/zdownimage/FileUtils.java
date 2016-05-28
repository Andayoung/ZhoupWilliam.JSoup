package com.example.my__pc.zdownimage;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Created by Administrator on 2015/7/22.
 */
public class FileUtils {

    private String SDPATH;

    public String getSDPATH() {
        return SDPATH;
    }

    public FileUtils() {
        //得到当前外部存储设备的目录
        // /SDCARD
        SDPATH = Environment.getExternalStorageDirectory() + "/";
    }

    /**
     * 在SD卡上删除目录
     *
     * @param dirName
     */
    public void deleteSDDir(String dirName) {
        File dir = new File(SDPATH + dirName);
        dir.delete();
    }

    /**
     * 在SD卡上删除目录下指定的文件
     *
     * @param dirName
     */
    public void deleteSDDirFile(String dirName,String fileName) {
        File dir = new File(SDPATH + dirName+fileName);
        dir.delete();
    }


    /**
     * 在SD卡上创建目录
     *
     * @param dirName
     */
    public void creatSDDir(String dirName) {
        File dir = new File(SDPATH + dirName);
        dir.mkdirs();
    }

    /**
     * 判断SD卡上的文件夹是否存在
     */
    public boolean isFileExist(String fileName) {
        File file = new File(SDPATH + fileName);
        return file.exists();
    }

    /**
     * 将一个Bitmap图片写入到SD卡指定路径中
     */
    public void writeBmpToSD(Bitmap bitmap, String picName, String dirName) throws IOException {

        File f = new File(SDPATH + dirName, picName);
        FileOutputStream out = new FileOutputStream(f);
        if (bitmap!=null){
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
        }

        out.flush();
        out.close();
    }

    /**
     * 读取SD卡指定位置的Bitmap。
     */
    public Bitmap readBitmapfromSD(String picName, String dirName) {
        String absolutePath=getSDPATH() + dirName + picName;
        return  BitmapFactory.decodeFile(absolutePath);
    }

    /**
     * 读取SD卡指定文件夹下的文件名数组。
     */
    public String[] readFileListfromSD(String dirName) {
        String absolutePath=getSDPATH() + dirName;
        File f = new File(absolutePath);
        return f.list();

    }

    /**
     * 删除一个List中包含的文件名的所有文件。指定文件夹路径。dirName,文件夹路径。
     */
    public void deleteFileListfromSD(List deleteList,String dirName) {

        for (int i=0;i<deleteList.size();i++){
            File dir = new File(SDPATH + dirName+deleteList.get(i));
            dir.delete();
        }


    }



}
