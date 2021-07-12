# generateCodeMybatis
## 方法1 推荐使用
使用Mybatis-Plus生成，有完整的controller，service，dao，mapper
### 步骤
1. 配置包名，数据库配置等信息
   
   `src/main/java/com/gao/MabatisPlusGenerator.java`下的 `main(...)`
   
2. 运行main方法
   
   执行main方法即可
   
3. 查看日志，找到生成文件路径即可。

4. 注意：
   1. 生成的dao文件没有 `@Mapper`注解，需要手动加。或是使用 `MapperScan`注解扫描
   2. 生成entity文件，重写了`protected Serializable pkVal()`方法，但是方法还是被`protected`修饰，需要改成 `public` 或直接删除该方法

## 方法2 
使用mybatis插件，自定义生成service，dao等。controller不完整

逆向工程，生成 mapper.xml,dao层，service层（含BaseService）
也会生成controller，但生成controller代码还未修改，使用不了。

Maven Project .

与 mybatisMappergener 逆向原理相同都是利用mybatis官方的generator包。

与generateCode 逆向原理不同，利用freemaker逆向生成。

### 步骤：
1. generator.properties中配置数据库

2. 配置包名（package）、表名（tableName）、模块名（domainObjectName）
   1. 注:schema：在mysql中就是DB name，在pgsql中就是模式名称

3. 运行GeneratorMain.main方法即可

### 参考
生成controller/service部分参考：https://github.com/linweiyu21/ControllerServicePlugin.git