package cn.how2j.diytomcat.watcher;

import cn.how2j.diytomcat.catalina.Context;
import cn.how2j.diytomcat.catalina.Host;
import cn.how2j.diytomcat.util.Constant;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.watch.WatchMonitor;
import cn.hutool.core.io.watch.WatchUtil;
import cn.hutool.core.io.watch.Watcher;
import cn.hutool.log.LogFactory;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.WatchEvent;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;

public class WarFileWatcher {
    private WatchMonitor monitor;

    public WarFileWatcher(Host host){
        this.monitor = WatchUtil.createAll(Constant.webappsFolder, 1, new Watcher() {
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
                    if(fileName.toLowerCase().endsWith(".war") & ENTRY_CREATE.equals(watchEvent.kind())){
                        LogFactory.get().info(this + " 检测到了Web应用下的重要文件变化 {} " , fileName);
                        File warFile = FileUtil.file(Constant.webappsFolder, fileName);
                        host.loadWar(warFile);
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
