# generateCodeMybatis
逆向工程，生成 mapper.xml,dao层，service层（含BaseService）
也会生成controller，但生成controller代码还未修改，使用不了。

Maven Project .

与 mybatisMappergener 逆向原理相同都是利用mybatis官方的generator包。

与generateCode 逆向原理不同，利用freemaker逆向生成。

步骤：
1. generator.properties中配置数据库

2. 配置包名（package）、表名（tableName）、模块名（domainObjectName）
   1. 注:schema：在mysql中就是DB name，在pgsql中就是模式名称

3. 运行GeneratorMain.main方法即可

### 参考
生成controller/service部分参考：https://github.com/linweiyu21/ControllerServicePlugin.git