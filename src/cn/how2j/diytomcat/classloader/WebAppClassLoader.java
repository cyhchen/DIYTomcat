package cn.how2j.diytomcat.classloader;

import cn.hutool.core.io.FileUtil;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

public class WebAppClassLoader extends URLClassLoader {
    public WebAppClassLoader(String docBase, ClassLoader classLoader){
        super(new URL[]{});
        File par = new File(docBase, "WEB-INF");
        File classesFolder = new File(par, "classes");
        File libFolder = new File(par, "lib");
        try {
            URL url = new URL("file:" + classesFolder + "/");
            this.addURL(url);
            List<File> lists = FileUtil.loopFiles(libFolder);
            for (File f : lists) {
                this.addURL(new URL("file:" + f.getAbsolutePath()));
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void stop(){
        try{
            close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

}
