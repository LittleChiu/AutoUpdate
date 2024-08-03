package me.yeoc.autoupdate;

import org.bukkit.Bukkit;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

public final class AutoUpdate extends JavaPlugin {

    private static AutoUpdate instance;

    public static AutoUpdate getInstance() {
        return instance;
    }

    public File mainfile = new File("C:\\Users\\Administrator\\Desktop\\AutoUpdate");
    public File logfile = new File(mainfile,"update.log");
    public File configfile = new File(mainfile,"config.yml");
    public File pluginfile = new File(mainfile,"\\plugins");
    public File serverpluginfile = new File(".\\plugins");
    public Config config;
    public File serverconfigfile = new File(getDataFolder(),"config.yml");
    public Config serverconfig;
    public List<File> needdelete = new ArrayList<>();
    @Override
    public void onEnable() {
        // Plugin startup logic
        instance = this;
        loadfile();
        update();
        Bukkit.getScheduler().runTask(this,()->{
            int i = 0;
            for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
                for (File need : needdelete) {
                    if(need.getName().contains(plugin.getName().toUpperCase())){
                        PluginUtils.unload(plugin);
                        System.out.println(plugin.getName());
                        need.delete();
                        i++;
                        break;
                    }
                }
            }
            if(i != 0){
                Bukkit.shutdown();
            }
        });
    }

    void update(){
        ConfigurationSection cs = config.getConfigurationSection("plugins");
        if(cs == null || cs.getKeys(false) == null) return;
        for (String key : cs.getKeys(false)) {
            String path = "plugins."+key+".";
            String pluginname = config.getString(path+"name");
            if(pluginname == null){
                System.out.println(pluginname+"在主配置文件中没有指定插件名称, 已跳过");
                writeLog(pluginname+"在主配置文件中没有指定插件名称, 已跳过");
                return;
            }
            String defversion = config.getString(path+"version");
            String version = config.getString(path+"version");
            if(version != null) version = version.replace(".","");
            String serverver = serverconfig.getString("plugins."+pluginname+".version");
            if(serverver != null) serverver = serverver.replace(".","");
            if(version == null){
                System.out.println(pluginname+"在主配置文件中没有指定版本, 已跳过");
                writeLog(pluginname+"在主配置文件中没有指定版本, 已跳过");
                return;
            }
            String port = config.getString(path+"server");
            if(port == null){
                System.out.println(pluginname+"在主配置文件中没有指定端口, 已跳过");
                writeLog(pluginname+"在主配置文件中没有指定端口, 已跳过");
                return;
            }
            String[] range = port.split("-");
            List<Integer> ports = new ArrayList<>();
            for (int i = Integer.parseInt(range[0]);i<=Integer.parseInt(range[1]);i++){
                ports.add(i);
            }
            System.out.println(serverver == null);
            System.out.println(serverver != null &&Integer.parseInt(serverver) < Integer.parseInt(version));
            if((serverver == null || Integer.parseInt(serverver) < Integer.parseInt(version)) && ports.contains(Bukkit.getPort())){
                replace(pluginname,defversion);
            }

        }
    }
    @SuppressWarnings("all")
    void replace(String pluginname,String version){
        File replacement = null;
        File need = null;
        for (File file : pluginfile.listFiles()) {
            if(file.getName().toUpperCase().contains(pluginname.toUpperCase())){
                replacement = file;
                break;
            }
        }
        for (File file : serverpluginfile.listFiles()) {
            if(file != null && file.getName() != null);
            if(file.getName().toUpperCase().contains(pluginname.toUpperCase()) && file.getName().contains("jar")){
                need = file;
                break;
            }
        }
        if(need != null) {
            Plugin pl = null;
            needdelete.add(need);
        }
        try {
            File co = new File(serverpluginfile,replacement.getName());
            co.createNewFile();
            FileUtil.copyFile(replacement,co);
            serverconfig.set("plugins."+pluginname+".version",version);
            serverconfig.save(serverconfigfile);
            writeLog(pluginname+"更新成功! 版本号: "+version);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    @SuppressWarnings("all")
    void loadfile(){
        if(!getDataFolder().exists()) getDataFolder().mkdir();
        if(!mainfile.exists()) mainfile.mkdir();
        if(!pluginfile.exists()) pluginfile.mkdir();
        for (File file : Arrays.asList(logfile, configfile,serverconfigfile)) {
            if(!file.exists()) {
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        config = new Config(configfile);
        serverconfig = new Config(serverconfigfile);

    }
    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
    void writeLog(String content) {
        File writefile;
        try {
            // 通过这个对象来判断是否向文本文件中追加内容
            // boolean addStr = append;

            writefile = logfile;
            SimpleDateFormat sdp = new SimpleDateFormat("yy/MM/dd hh:mm:ss");
            String date = sdp.format(new Date());
            FileOutputStream fw = new FileOutputStream(writefile,true);
            Writer out = new OutputStreamWriter(fw, "utf-8");
            out.write("["+date+"]["+Bukkit.getPort()+"] "+content);
            String newline = System.getProperty("line.separator");
            //写入换行
            out.write(newline);
            out.close();
            fw.flush();
            fw.close();
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    // 获取
//    void writeLog(String s) {
//        SimpleDateFormat sdp = new SimpleDateFormat("yy/MM/dd hh:mm:ss");
//        String date = sdp.format(new Date());
//        FileWriter fw = null;
//        try {
//            fw=new FileWriter(logfile);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        try {
//            assert fw != null;
//            fw.write("["+date+"]["+Bukkit.getPort()+"] "+s+"/n");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
}
