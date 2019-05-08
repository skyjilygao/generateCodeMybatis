package com.gao;

import org.mybatis.generator.api.MyBatisGenerator;
import org.mybatis.generator.config.Configuration;
import org.mybatis.generator.config.xml.ConfigurationParser;
import org.mybatis.generator.exception.XMLParserException;
import org.mybatis.generator.internal.DefaultShellCallback;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * 主文件，配置generator.properties文件后，直接运行main方法即可
 * <p> generator.properties配置
 * @author skyjilygao
 */
public class GeneratorMain {
    private static Boolean generateNewCode = false;
    public static void main(String[] args) {

        Properties props = new Properties();
        InputStream in;
        try {
            in = GeneratorMain.class.getClassLoader().getResourceAsStream("generator.properties");
            props.load(in);
            generateNewCode = Boolean.parseBoolean(props.getProperty("generateNewCode"));
            String javaPath =props.getProperty("project");
            String resourcePath =props.getProperty("resource");
            File projectDir = new File(javaPath);
            File resourceDir = new File(resourcePath);
            if(!projectDir.exists()){
                projectDir.mkdirs();
            }else {
                generateNewCode(projectDir, javaPath);
            }
            if(!resourceDir.exists()){
                resourceDir.mkdirs();
            }else {
                generateNewCode(resourceDir, resourcePath);
            }
            List<String> warnings = new ArrayList<String>();
            boolean overwrite = true;
            String filename = GeneratorMain.class.getClassLoader().getResource("generatorConfig.xml").getFile();
            // msyql
            String classPathEntry3 = GeneratorMain.class.getClassLoader().getResource("lib/mysql-connector-java-5.1.38.jar").getFile();
            // postgresql
            String classPathEntry = GeneratorMain.class.getClassLoader().getResource("lib/postgresql-9.4.1209.jar").getFile();

            //读取配置文件
            File configFile = new File(filename);
            ConfigurationParser cp = new ConfigurationParser(warnings);
            Configuration config;
            try {
                config = cp.parseConfiguration(configFile);

                DefaultShellCallback callback = new DefaultShellCallback(overwrite);
                MyBatisGenerator myBatisGenerator;
                try {
                    config.addClasspathEntry(classPathEntry);
                    myBatisGenerator = new MyBatisGenerator(config, callback,
                            warnings);
                    myBatisGenerator.generate(null);
                    //打印结果
                    for (String str : warnings) {
                        System.out.println(str);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } catch (IOException e) {
                e.printStackTrace();
            } catch (XMLParserException e) {
                e.printStackTrace();
            }
            System.out.println("generator ok");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 每次运行是否重新生成目录
     * @param dir
     * @param path
     */
    private static void generateNewCode(File dir, String path){
        if(generateNewCode){
            System.out.println("del...." + path);
            delFolder(path);
//            dir.delete();
            dir.mkdirs();
        }
        System.out.println("del....ok" );
    }
    //删除文件夹
    public static void delFolder(String folderPath) {
        try {
            delAllFile(folderPath); //删除完里面所有内容
            String filePath = folderPath;
            filePath = filePath.toString();
            java.io.File myFilePath = new java.io.File(filePath);
            myFilePath.delete(); //删除空文件夹
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
//    删除指定文件夹下的所有文件

    public static boolean delAllFile(String path) {
        boolean flag = false;
        File file = new File(path);
        if (!file.exists()) {
            return flag;
        }
        if (!file.isDirectory()) {
            return flag;
        }
        String[] tempList = file.list();
        File temp = null;
        for (int i = 0; i < tempList.length; i++) {
            if (path.endsWith(File.separator)) {
                temp = new File(path + tempList[i]);
            } else {
                temp = new File(path + File.separator + tempList[i]);
            }
            if (temp.isFile()) {
                temp.delete();
            }
            if (temp.isDirectory()) {
                delAllFile(path + "/" + tempList[i]);//先删除文件夹里面的文件
                delFolder(path + "/" + tempList[i]);//再删除空文件夹
                flag = true;
            }
        }
        return flag;
    }
}
