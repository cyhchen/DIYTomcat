package cn.how2j.diytomcat.classloader;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

public class CommonClassLoader extends URLClassLoader {
    public CommonClassLoader(){
        super(new URL[]{});
        try{
            File par = new File(System.getProperty("user.dir"));
            File file = new File(par, "lib");
            File[] files = file.listFiles();
            for(File f : files){
                if(f.getName().endsWith("jar")){
                    this.addURL(new URL("file:"+f.getAbsolutePath()));
                }
            }
        }catch (MalformedURLException e){
            e.printStackTrace();
        }
    }
}
