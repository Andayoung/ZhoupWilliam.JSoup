package com.example.my__pc.zdownimage;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.avos.avoscloud.AVAnalytics;
import com.avos.avoscloud.AVOSCloud;
import com.avos.avoscloud.AVObject;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends Activity {

    private  ProgressBar progb;
    private TextView pct;
    private String httpUrl;
    private String ftpUrl;
    private static int COUNT = 0;
    private static int DOWN_COUNT = 0;
    private String SDPATH = Environment.getExternalStorageDirectory() + "/";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_main);
        AVOSCloud.initialize(this, "skaBx9mFAXf6UOWvvhRNvRiN", "d713LoAcswTSxoPxwv98R4P3");
        AVAnalytics.trackAppOpened(getIntent());
        AVObject testObject = new AVObject("TestObject");
        testObject.put("foo", "bar");
        testObject.saveInBackground();



        StrictMode.ThreadPolicy policy=new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        Button btQ=(Button)findViewById(R.id.okpsw);
        Button btX=(Button)findViewById(R.id.cancelpsw);
        Button btO=(Button)findViewById(R.id.openpsw);
        EditText eh=(EditText)findViewById(R.id.httpurl);
        EditText ef=(EditText)findViewById(R.id.ftpurl);
        pct=(TextView)findViewById(R.id.precent);
        eh.setSelection(eh.getText().length());
        ef.setSelection(ef.getText().length());
        progb = (ProgressBar) findViewById(R.id.loading_process_dialog_progressBar);
        progb.setIndeterminate(false);

        btQ.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText ehl=(EditText)findViewById(R.id.httpurl);
                EditText efl=(EditText)findViewById(R.id.ftpurl);
                ftpUrl = efl.getText().toString();
                httpUrl = ehl.getText().toString();
                String addr = "(http://|ftp://|https://|www)[^\u4e00-\u9fa5\\s]*?\\.(com|net|cn|me|tw|fr)[^\u4e00-\u9fa5\\s]*";
                if (!httpUrl.matches(addr)) {
                    Toast.makeText(MainActivity.this, "输入网址有误",
                            Toast.LENGTH_SHORT).show();
                    ehl.setText("http://");
                    ehl.setSelection(ehl.getText().length());
                }else {
                    Message m = handler.obtainMessage();
                    m.what = 0;
                    m.sendToTarget();
                    jsoupHTML();
                }
            }
        });
        btX.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        btO.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                //intent.setType(ContactsContract.Contacts.CONTENT_ITEM_TYPE);
                startActivityForResult(intent, 1);
            }
        });

    }
    Handler handler = new Handler() {

                        @Override
                public void handleMessage(Message msg) {
                       // TODO Auto-generated method stub
                       super.handleMessage(msg);
                       switch(msg.what) {
                                case 0:
                                        progb.setVisibility(View.VISIBLE);
                                        pct.setVisibility(View.VISIBLE);
                                        pct.setText("0%");
                                        break;
                                case 1:
                                        pct.setText(String.valueOf(msg.arg1)+"%");
                                        break;
                                case 2:
                                        progb.setVisibility(View.INVISIBLE);
                                        pct.setVisibility(View.INVISIBLE);
                                        Toast.makeText(getApplicationContext(), "大人，已下载完毕！", Toast.LENGTH_SHORT).show();
                                        break;
                            }
                    }
            };


    private void jsoupHTML() throws RuntimeException {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String filPath = ftpUrl;
                    String urlPath = httpUrl;
                    Document doc = Jsoup.connect(urlPath).timeout(1000000).get();
                    FileUtils fileUtils = new FileUtils();
                    if (!fileUtils.isFileExist(filPath)) {
                        fileUtils.creatSDDir(filPath);
                    }


                    //:当前页中的图片
                    Elements srcLinks = doc.select("img[src$=.jpg]");
                    Log.e("个数：", String.valueOf(srcLinks.size()));
                    int imageSize = srcLinks.size();
                    int was = 1;
                    for (Element link : srcLinks) {
                        //:剔除标签，只剩链接路径
                        String imagesPath = link.attr("src");
                        System.out.println("当前访问路径:" + imagesPath);
                        File f = new File(SDPATH + filPath, COUNT++ + ".jpg");
                        getImages(imagesPath, f);
                        Message m1 = handler.obtainMessage();
                        m1.what=1;
                        m1.arg1 = was * 100 / imageSize;
                        m1.sendToTarget();
                        was++;
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    System.out.println("共访问" + COUNT + "张图片，其中下载" + DOWN_COUNT + "张图片");
                    Message m3 = handler.obtainMessage();
                    m3.what=2;
                    m3.sendToTarget();
                }


            }
        }).start();

    }


    /**
     * @param urlPath 图片路径
     * @throws Exception
     */
    private void getImages(String urlPath,File fileName) throws Exception{
        final URL url = new URL(urlPath);//：获取的路径
        //:http协议连接对象
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setReadTimeout(6 * 10000);
        if (conn.getResponseCode() <10000){
            InputStream inputStream = conn.getInputStream();
            byte[] data = readStream(inputStream);
            if(data.length>(1024*10)){
                FileOutputStream outputStream = new FileOutputStream(fileName);
                outputStream.write(data);
                System.err.println("第" + ++DOWN_COUNT +"图片下载成功");
                outputStream.close();
            }
        }

    }

    /**
     * 读取url中数据，并以字节的形式返回
     * @param inputStream
     * @return
     * @throws Exception
     */
    private byte[] readStream(InputStream inputStream) throws Exception{
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = -1;
        while((len = inputStream.read(buffer)) !=-1){
            outputStream.write(buffer, 0, len);
        }
        outputStream.close();
        inputStream.close();
        return outputStream.toByteArray();
    }


}
