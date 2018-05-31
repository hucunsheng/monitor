package com.ai.util;

import gui.ava.html.image.generator.HtmlImageGenerator;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import sun.net.www.http.HttpClient;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * Created by hucunsheng on 2018/5/24.
 */
public class HtmlToImageMain {
    public static void generateOutput(String url) throws Exception {

        //load the webpage into the editor
        //JEditorPane ed = new JEditorPane(new URL("http://www.google.com"));
        JEditorPane ed = new JEditorPane(new URL(url));
        ed.setSize(800,600);

        //create a new image
        BufferedImage image = new BufferedImage(ed.getWidth(), ed.getHeight(),
                BufferedImage.TYPE_INT_ARGB);

        //paint the editor onto the image
        SwingUtilities.paintComponent(image.createGraphics(),
                ed,
                new JPanel(),
                0, 0, image.getWidth(), image.getHeight());
        //save the image to file
        String path = "/Users/hucunsheng/Downloads/testImage/";
        ImageIO.write((RenderedImage)image, "png", new File(path+"generateOutput.png"));
    }


    public static void HtmlImageGeneratorFile(String url) throws InterruptedException {
        HtmlImageGenerator imageGenerator = new HtmlImageGenerator();
//        String str = httpClientByGet(url);
//        imageGenerator.loadHtml(str);
//      imageGenerator
//              .loadHtml("<b>Hello World!</b> Please goto <a title=\"Goto Google\" href=\"http://www.google.com\">Google</a>.");
        //imageGenerator.loadUrl("http://mtkplmap2:7001/esdm_web");
        imageGenerator.loadUrl(url);
       // TimeUnit.SECONDS.sleep(2);
        //imageGenerator.getBufferedImage();
        TimeUnit.SECONDS.sleep(2);
        String path = "/Users/hucunsheng/Downloads/testImage/";
        imageGenerator.saveAsImage(path+ System.currentTimeMillis()+".jpg");
//        imageGenerator.saveAsHtmlWithMap("hello-world.html", path+ System.currentTimeMillis()+"hello-world.png");
    }

    public void JFrameFile(String args) throws Exception {
//        JFrame window = new JFrame();
//        HtmlPanel panel = new HtmlPanel();
//        window.getContentPane().add(panel);
//        window.setSize(600, 400);
//        window.setVisible(true);
//        new SimpleHtmlRendererContext(panel, new SimpleUserAgentContext())
//                .navigate("http://www.hefeipet.com/client/chongwuzhishi/shenghuozatan/2012/0220/95.html");
//
//        BufferedImage image = new BufferedImage(panel.getWidth(),
//                panel.getHeight(), BufferedImage.TYPE_INT_ARGB);
//
//        // paint the editor onto the image
//        SwingUtilities.paintComponent(image.createGraphics(), panel,
//                new JPanel(), 0, 0, image.getWidth(), image.getHeight());
//        // save the image to file
//        ImageIO.write((RenderedImage) image, "png", new File("html.png"));
    }

    public static String httpClientByGet(String url){
        CloseableHttpClient httpCilent2 = HttpClients.createDefault();
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(5000)   //设置连接超时时间
                .setConnectionRequestTimeout(5000) // 设置请求超时时间
                .setSocketTimeout(5000)
                .setRedirectsEnabled(true)//默认允许自动重定向
                .build();
        HttpGet httpGet2 = new HttpGet("http://www.baidu.com");
        httpGet2.setConfig(requestConfig);
        String srtResult = "";
        try {

            HttpResponse httpResponse = httpCilent2.execute(httpGet2);
            if(httpResponse.getStatusLine().getStatusCode() == 200){
                srtResult = EntityUtils.toString(httpResponse.getEntity(),"UTF-8");//获得返回的结果
                System.out.println(srtResult);
            }else if(httpResponse.getStatusLine().getStatusCode() == 400){
                //..........
            }else if(httpResponse.getStatusLine().getStatusCode() == 500){
                //.............
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                httpCilent2.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return srtResult;
    }


    public static void main(String[] args) {
        try {
//            HtmlToImageMain.HtmlImageGeneratorFile("https://zhuanjinke.com/basic_status");
            HtmlToImageMain.generateOutput("http://47.92.6.106:8889/monitor/echarts.html");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}