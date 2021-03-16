package com.gao;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.generator.AutoGenerator;
import com.baomidou.mybatisplus.generator.InjectionConfig;
import com.baomidou.mybatisplus.generator.config.DataSourceConfig;
import com.baomidou.mybatisplus.generator.config.GlobalConfig;
import com.baomidou.mybatisplus.generator.config.PackageConfig;
import com.baomidou.mybatisplus.generator.config.StrategyConfig;
import com.baomidou.mybatisplus.generator.config.converts.MySqlTypeConvert;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;

import java.util.HashMap;
import java.util.Map;

/**
 * 这是利用mybatis-plus生成方式。
 * @author skyjilygao
 * @date 20210316
 */
public class MabatisPlusGenerator {

    //生成文件所在项目路径
    private static String baseProjectPath = "tmpCode2\\";

    //基本包名
    private static String basePackage = "com.powerwin.fbads.aliexpress";
    //作者
    private static String authorName = "skyjilygao";
    //要生成的表名
    private static String[] tables = {"seller_product_catalog"};
    //table前缀
    private static String prefix = "";

    //数据库配置四要素
    private static String driverName = "com.mysql.cj.jdbc.Driver";
    private static String url = "jdbc:mysql://192.168.88.89:3306/ae_seller?characterEncoding=utf8&useUnicode=true&autoReconnect=true&serverTimezone=CST&nullCatalogMeansCurrent=true";
    private static String username = "user-admin";
    private static String password = "ceshi";


    public static void main(String[] args) {


        DataSourceConfig dataSourceConfig = new DataSourceConfig();
        dataSourceConfig.setDbType(DbType.MYSQL).setTypeConvert(MySqlTypeConvert.INSTANCE).setDriverName(driverName).setUrl(url).setUsername(username).setPassword(password);
        AutoGenerator gen = new AutoGenerator();
        gen.setDataSource(dataSourceConfig);

        GlobalConfig globalConfig = new GlobalConfig();
        globalConfig.setOutputDir(baseProjectPath + "/src/main/java").setFileOverride(true).setActiveRecord(true).setEnableCache(false).setBaseResultMap(true).setBaseColumnList(true).setOpen(false).setAuthor(authorName).setMapperName("%sMapper").setXmlName("%sMapper").setServiceName("%sService").setServiceImplName("%sServiceImpl").setControllerName("%sController");


        // 全局配置
        gen.setGlobalConfig(globalConfig);

        /**
         * 策略配置
         */
        gen.setStrategy(new StrategyConfig()
                        // .setCapitalMode(true)// 全局大写命名
                        //.setDbColumnUnderline(true)//全局下划线命名
                        .setTablePrefix(new String[]{prefix})// 此处可以修改为您的表前缀
                        .setNaming(NamingStrategy.underline_to_camel)// 表名生成策略
                        .setInclude(tables) // 需要生成的表
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

        // 执行生成
        gen.execute();
    }
}
