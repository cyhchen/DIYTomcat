package cn.how2j.diytomcat.watcher;

import cn.how2j.diytomcat.catalina.Context;
import cn.hutool.core.io.watch.WatchMonitor;
import cn.hutool.core.io.watch.WatchUtil;
import cn.hutool.core.io.watch.Watcher;
import cn.hutool.log.LogFactory;

import java.nio.file.Path;
import java.nio.file.WatchEvent;

public class ContextFileChangeWatcher {
    private WatchMonitor monitor;
    private boolean stop;

    public ContextFileChangeWatcher(Context context){
        this.monitor = WatchUtil.createAll(context.getDocBase(), Integer.MAX_VALUE, new Watcher() {
            @Override
            public void onCreate(WatchEvent<?> watchEvent, Path path) {
                delWith(watchEvent);
            }

            @Override
            public void onModify(WatchEvent<?> watchEvent, Path path) {
                delWith(watchEvent);
            }

            @Override
            public void onDelete(WatchEvent<?> watchEvent, Path path) {
                delWith(watchEvent);
            }

            @Override
            public void onOverflow(WatchEvent<?> watchEvent, Path path) {
                delWith(watchEvent);
            }

            private void delWith(WatchEvent<?> watchEvent){
                synchronized (this){
                    String fileName = watchEvent.context().toString();
                    if(stop){
                        return;
                    }
                    if(fileName.endsWith(".jar") || fileName.endsWith(".class") || fileName.endsWith(".xml")){
                        stop = true;
                        LogFactory.get().info(ContextFileChangeWatcher.this + " 检测到了Web应用下的重要文件变化 {} " , fileName);
                        context.reload();
                    }
                }
            }
        });
    }

    public void start(){
        this.monitor.start();
    }

    public void stop(){
        this.monitor.close();
    }
}
