package com.lzz.controller;

import com.lzz.entity.MovieBean;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by 李壮壮 on 2019/6/28.
 */
@Controller
public class MoviePlayController {

    private static LinkedHashMap<Integer, String> hash;
    private static int id;
    private static String viewName = "img";
    private static Integer pageNumber = 6;
    @RequestMapping("/")
    public String movie(String filepath, Model model, Integer number) {
        if (filepath == null) return viewName;
        if (number != null) pageNumber = number;
        hash = new LinkedHashMap<>();
        id = 0;
        File file = new File(filepath);
        List<MovieBean> movies = new ArrayList<>();
        if (file.isDirectory()) {
            String[] filelist = file.list();
            for (int i = 0; i < filelist.length; i++) {
                File readfile = new File(filepath + File.separator + filelist[i]);
                if (!readfile.isDirectory() && (readfile.getName().toLowerCase().contains("img") || readfile.getName().toLowerCase().contains("jpg"))) {
                    hash.put(id, readfile.getAbsolutePath());
                    id++;
                }
            }
            int len = hash.size() <= pageNumber ? hash.size() : pageNumber;
            for (int i = 0; i < len; i++){
                MovieBean movieBean = new MovieBean();
                movieBean.setUrl("stream?id=" + i);
                movieBean.setName(hash.get(i));
                movies.add(movieBean);
            }
        }
        model.addAttribute("movies", movies);
        model.addAttribute("cur", 1);
        if (hash != null && hash.size() != 0 && hash.get(0).toLowerCase().contains("mp4")){
            viewName = "movie";
        }else viewName = "img";
        return viewName;
    }

    @RequestMapping("/page")
    public String moviePage(Integer cur, Model model, Integer number) {
        if (number != null) pageNumber = number;
        if (cur == null) cur = 1;
        List<MovieBean> movies = new ArrayList<>();
        if (hash != null){
            cur = cur < 1 ? 1 : cur;
            int totalPage;
            if (hash.size() % pageNumber == 0) totalPage = hash.size() / pageNumber;
            else totalPage = hash.size() / pageNumber + 1;
            cur = cur > totalPage ? totalPage : cur;
            int len = cur * pageNumber;
            len = hash.size() <= len ? hash.size() : len;
            for (int i = (cur - 1) * pageNumber; i < len; i++){
                MovieBean movieBean = new MovieBean();
                movieBean.setUrl("stream?id=" + i);
                movieBean.setName(hash.get(i));
                movies.add(movieBean);
            }
        }
        model.addAttribute("movies", movies);
        model.addAttribute("cur", cur);
        return viewName;
    }

    @RequestMapping("/stream")
    public void getStreamData(HttpServletResponse response, @RequestParam("id") Integer id) {
        try {
            FileInputStream instream = new FileInputStream(hash.get(id));
            byte[] b = new byte[1024 * 1024];
            BufferedInputStream buf = new BufferedInputStream(instream);
            ServletOutputStream out = response.getOutputStream();
            BufferedOutputStream bot = new BufferedOutputStream(out);
            if (buf.available() > 0) {
                int len = buf.available();
                int now = 0;
                while (true) {
                    now = buf.read(b) + now;
                    System.out.println(id + " " + now + " " + len);
                    bot.write(b, 0, b.length);
                    if (now >= len) {
                        break;
                    }
                }
            }
            System.out.println("ok");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            //e.printStackTrace();
            System.out.println("error");
        }
    }
}
