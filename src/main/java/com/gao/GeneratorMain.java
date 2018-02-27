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

public class GeneratorMain {
    public static void main(String[] args) {
        Properties props = new Properties();
        InputStream in;
        try {
            in = GeneratorMain.class.getClassLoader().getResourceAsStream("generator.properties");
            props.load(in);
            File projectDir = new File(props.getProperty("project"));
            File resourceDir = new File(props.getProperty("resource"));
            if(!projectDir.exists()){
                projectDir.mkdirs();
            }
            if(!resourceDir.exists()){
                resourceDir.mkdirs();
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}