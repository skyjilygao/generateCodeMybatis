package com.gao;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.generator.AutoGenerator;
import com.baomidou.mybatisplus.generator.InjectionConfig;
import com.baomidou.mybatisplus.generator.config.*;
import com.baomidou.mybatisplus.generator.config.converts.MySqlTypeConvert;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;
import com.sun.scenario.effect.impl.prism.PrImage;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.*;

/**
 * 这是利用mybatis-plus生成方式。
 * @author skyjilygao
 * @date 20210316
 */
@Slf4j
public class MabatisPlusGenerator {

    /**
     * 配置main方法后直接执行即可
     * @param args
     */
    public static void main(String[] args) {
        // 1. 是否要删除之前生成的文件
        clearOld = true;
        // 2. 配置包名
        basePackage = "com.sky.shop";
        // 3. 类的java doc 作者
        authorName = "skyjilygao";

        // 4. 添加表名
        tables.add("specifications");

        // 5. 配置数据库
        DbDto dbDto;
        dbDto = new DbDto(DbType.MYSQL, "192.168.8.15", 3306, "test", "test", "123456");

        // 6. 执行
        execute(dbDto);
    }

    /**
     * 执行
     * @param dbDto
     */
    private static void execute(DbDto dbDto){
        if(clearOld){
            delFolder(baseProjectPath);
        }
        driverName = dbDto.driverName;
        url = dbDto.url;
        username = dbDto.username;
        password = dbDto.password;
        // 执行生成
        AutoGenerator gen = buidGenerator(dbDto);
        gen.execute();
    }

    /**
     * 构建 AutoGenerator
     * @param dbDto
     * @return
     */
    private static AutoGenerator buidGenerator(DbDto dbDto){
        DataSourceConfig dataSourceConfig = new DataSourceConfig();
        dataSourceConfig.setDbType(dbDto.dbType).setTypeConvert(dbDto.typeConvert).setDriverName(driverName).setUrl(url).setUsername(username).setPassword(password);

        AutoGenerator gen = new AutoGenerator();
        gen.setDataSource(dataSourceConfig);

        GlobalConfig globalConfig = new GlobalConfig();
        globalConfig.setOutputDir(baseProjectPath + "/src/main/java").setFileOverride(true).setActiveRecord(true).setEnableCache(false).setBaseResultMap(true).setBaseColumnList(true).setOpen(false).setAuthor(authorName).setMapperName("%sMapper").setXmlName("%sMapper").setServiceName("%sService").setServiceImplName("%sServiceImpl").setControllerName("%sController");


        // 全局配置
        gen.setGlobalConfig(globalConfig);

        String[] tbls = tables.toArray(new String[tables.size()]);
        /**
         * 策略配置
         */
        gen.setStrategy(new StrategyConfig()
                        // .setCapitalMode(true)// 全局大写命名
                        //.setDbColumnUnderline(true)//全局下划线命名
                        .setTablePrefix(new String[]{prefix})// 此处可以修改为您的表前缀
                        .setNaming(NamingStrategy.underline_to_camel)// 表名生成策略
                        .setInclude(tbls) // 需要生成的表
                        .setRestControllerStyle(true)
                        //.setExclude(new String[]{"test"}) // 排除生成的表
                        // 自定义实体父类
                        // .setSuperEntityClass("com.baomidou.demo.TestEntity")
                        // 自定义实体，公共字段
                        //.setSuperEntityColumns(new String[]{"test_id"})
                        //.setTableFillList(tableFillList)
                        // 自定义 mapper 父类 默认BaseMapper
                        //.setSuperMapperClass("com.baomidou.mybatisplus.mapper.BaseMapper")
                        // 自定义 service 父类 默认IService
                        // .setSuperServiceClass("com.baomidou.demo.TestService")
                        // 自定义 service 实现类父类 默认ServiceImpl
                        // .setSuperServiceImplClass("com.baomidou.demo.TestServiceImpl")
                        // 自定义 controller 父类
                        //.setSuperControllerClass("com.kichun."+packageName+".controller.AbstractController")
                        // 【实体】是否生成字段常量（默认 false）
                        // public static final String ID = "test_id";
                        // .setEntityColumnConstant(true)
                        // 【实体】是否为构建者模型（默认 false）
                        // public User setName(String name) {this.name = name; return this;}
                        // .setEntityBuilderModel(true)
                        // 【实体】是否为lombok模型（默认 false）<a href="https://projectlombok.org/">document</a>
                        .setEntityLombokModel(true)
                // Boolean类型字段是否移除is前缀处理
                // .setEntityBooleanColumnRemoveIsPrefix(true)
                // .setRestControllerStyle(true)
                // .setControllerMappingHyphenStyle(true)
        );

        /**
         * 包配置
         */
        gen.setPackageInfo(new PackageConfig()
                //.setModuleName("User")
                .setParent(basePackage)// 自定义包路径
                .setController("controller")// 这里是控制器包名，默认 web
                .setEntity("entity").setMapper("dao").setService("service").setServiceImpl("service.impl").setXml("mapper"));

        /**
         * 注入自定义配置
         */
        // 注入自定义配置，可以在 VM 中使用 cfg.abc 设置的值
        InjectionConfig abc = new InjectionConfig() {
            @Override
            public void initMap() {
                Map<String, Object> map = new HashMap<>();
                map.put("abc", this.getConfig().getGlobalConfig().getAuthor() + "-mp");
                this.setMap(map);
            }
        };
        //自定义文件输出位置（非必须）
        /*List<FileOutConfig> fileOutList = new ArrayList<>();
        fileOutList.add(new FileOutConfig("/templates/mapper.xml.ftl") {
            @Override
            public String outputFile(TableInfo tableInfo) {
                return baseProjectPath + "/src/main/resources/mappers/" + tableInfo.getEntityName() + ".xml";
            }
        });
        abc.setFileOutConfigList(fileOutList);
        gen.setCfg(abc);*/

        /**
         * 指定模板引擎 默认是VelocityTemplateEngine ，需要引入相关引擎依赖
         */
        gen.setTemplateEngine(new FreemarkerTemplateEngine());

        /**
         * 模板配置
         */
//        gen.setTemplate(
        // 关闭默认 xml 生成，调整生成 至 根目录
//                new TemplateConfig().setXml(null)
        // 自定义模板配置，模板可以参考源码 /mybatis-plus/src/main/resources/template 使用 copy
        // 至您项目 src/main/resources/template 目录下，模板名称也可自定义如下配置：
        // .setController("...");
        // .setEntity("...");
        // .setMapper("...");
        // .setXml("...");
        // .setService("...");
        // .setServiceImpl("...");
//        );

        return gen;
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

    @Getter
    static class DbDto{
        private DbType dbType;
        private ITypeConvert typeConvert;
        private String driverName;
        private String ip;
        private int port;
        private String dbName;
        private String username;
        private String password;
        private String url;

        public DbDto(DbType dbType, String ip, int port, String dbName, String username, String password) {
            this.dbType = dbType;
            this.ip = ip;
            this.port = port;
            this.dbName = dbName;
            this.username = username;
            this.password = password;

            init();
        }
        private void init(){
            switch (dbType){
                case MYSQL:
                    driverName ="com.mysql.cj.jdbc.Driver";
                    typeConvert = MySqlTypeConvert.INSTANCE;
                    String format = "jdbc:mysql://%s:%s/%s?characterEncoding=utf8&useUnicode=true&autoReconnect=true&serverTimezone=CST&nullCatalogMeansCurrent=true";
                    this.url = String.format(format, ip,port, dbName);
                    break;
                default:
                    System.out.println("没有找到数据库类型，需要在这里配置");
                    log.error("没有找到数据库类型，需要在这里配置");
                    break;

            }
        }
    }



    //生成文件所在项目路径
    private static String baseProjectPath = "tmpCode2";

    //基本包名
    private static String basePackage;
    //作者
    private static String authorName;
    //要生成的表名
//    sww_commodity_tag, sww_goods_material, sww_goods_source, sww_log_commodity, sww_tag

    private static Set<String> tables = new LinkedHashSet<>();
    //table前缀
    private static String prefix = "";

    //数据库配置四要素
    private static String driverName;
    private static String url;
    private static String username;
    private static String password;

    private static boolean clearOld = false;
}
