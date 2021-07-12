package com.gao.plugins;

import com.gao.MyCommentGenerator;
import org.mybatis.generator.api.GeneratedJavaFile;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.exception.ShellException;
import org.mybatis.generator.internal.DefaultShellCallback;
import org.mybatis.generator.internal.util.StringUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 在 Mybatis Generator 的基础上生成 Controller 与 Service 类,适用于Spring与Mybatis整合的项目.
 * 为 Mybatis Generator 的 Mapper.java 生成对应的 Controller 与 Service 类.
 *
 * @author linweiyu
 */
public class ControllerServicePlugin extends PluginAdapter {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final String IS_GENERATE_CONTROLLER_SERVICE = "generate.controller.service";

    private final static String BASE_SERVICE_NAME = "EntityServiceBase";

    @Override
    public boolean validate(List<String> warnings) {
        logger.info("--- ControllerServicePlugin validate invoke");
        return true;
    }

    /**
     * 重写此方法,实现额外Java文件生成
     *
     * @param introspectedTable
     * @return
     */
    @Override
    public List<GeneratedJavaFile> contextGenerateAdditionalJavaFiles(IntrospectedTable introspectedTable) {
        logger.info("--- ControllerServicePlugin contextGenerateAdditionalJavaFiles invoke");

        List<GeneratedJavaFile> super_result = super.contextGenerateAdditionalJavaFiles(introspectedTable);

        Properties properties = getProperties();
        // 是否生成Service,Controller类
        String is_generate_controller_service = properties.getProperty(IS_GENERATE_CONTROLLER_SERVICE);
        logger.info("--- ControllerServicePlugin is_generate_controller_service=[{}]", is_generate_controller_service);

        if (StringUtility.stringHasValue(is_generate_controller_service) && Boolean.valueOf(is_generate_controller_service)) {
            if (super_result == null) {
                super_result = new ArrayList<GeneratedJavaFile>();
            }

            // 实体类的包名
            String modelTargetPackage = getContext().getJavaModelGeneratorConfiguration().getTargetPackage();
            // 实体类的类名
            String domainObjectName = introspectedTable.getTableConfiguration().getDomainObjectName();
            // 实体类的全限定类名
            String modalFullName = modelTargetPackage + "." + domainObjectName;
            logger.info("--- ControllerServicePlugin modalFullName=[{}]", modalFullName);

            // src/main/java
            String targetProject = properties.getProperty("targetProject");
            logger.info("--- ControllerServicePlugin targetProject=[{}]", targetProject);

            // Service与Controller所在的包的包名
            String servicePackage = properties.getProperty("service.package");
            String serviceImplPackage = servicePackage + ".impl";
            String webPackage = properties.getProperty("web.package");
            logger.info("--- ControllerServicePlugin servicePackage=[{}]", servicePackage);
            logger.info("--- ControllerServicePlugin webPackage=[{}]", webPackage);

            // 生成Service类所在的包(对应文件系统的文件夹),同时生成impl包
            DefaultShellCallback shellCallback = new DefaultShellCallback(true);
            try {
                File serviceDirectory = shellCallback.getDirectory(targetProject, serviceImplPackage);
                serviceDirectory.mkdirs();
                String absolutePath = serviceDirectory.getAbsolutePath();
                if (StringUtility.stringHasValue(absolutePath)) {
                    logger.info("--- ControllerServicePlugin 创建目录(包)成功=[{}]", absolutePath);
                }
            } catch (ShellException e) {
                logger.error(e.getMessage(), e);
            }

            // 生成Service相关类
            generateServiceFile(introspectedTable, super_result, domainObjectName, modalFullName, targetProject, servicePackage, serviceImplPackage);

            // 生成Controller相关类
            generateControllerJavaFile(introspectedTable, super_result, domainObjectName, modalFullName, targetProject, servicePackage, webPackage, shellCallback);

        }

        return super_result;
    }

    // 生成Service相关类
    private void generateControllerJavaFile(IntrospectedTable introspectedTable, List<GeneratedJavaFile> super_result, String domainObjectName, String modalFullName, String targetProject, String servicePackage, String webPackage, DefaultShellCallback shellCallback) {
        try {
            File controllerDirectory = shellCallback.getDirectory(targetProject, webPackage);
            controllerDirectory.mkdirs();
            String absolutePath = controllerDirectory.getAbsolutePath();

            if (StringUtility.stringHasValue(absolutePath)) {
                logger.info("--- ControllerServicePlugin 创建目录(包)成功=[{}]", absolutePath);
            }
        } catch (ShellException e) {
            logger.error(e.getMessage(), e);
        }
        // ServiceImpl类
        // typeName为类的全限定类名
        String typeName = webPackage + "." + domainObjectName + "Controller";
        // TopLevelClass类代表要生成的类,mybatis根据TopLevelClass类代表要生成一个类的field,method等
        TopLevelClass controllerCompilationUnit = new TopLevelClass(typeName);
        controllerCompilationUnit.setVisibility(JavaVisibility.PUBLIC);

        // 此类所在包
        String packageName = controllerCompilationUnit.getType().getPackageName();

        // 为类添加注解
        controllerCompilationUnit.addAnnotation("@Controller");
        if (domainObjectName.endsWith("s")) {
            controllerCompilationUnit.addAnnotation("@RequestMapping(\"/" + toLowerCaseFirstOne(domainObjectName) + "\")");
        } else {
            controllerCompilationUnit.addAnnotation("@RequestMapping(\"/" + toLowerCaseFirstOne(domainObjectName) + "s" + "\")");
        }

        // 为类添加Field
        setControllerField(controllerCompilationUnit, introspectedTable, servicePackage);

        // 为类添加方法
        setControllerMethod(controllerCompilationUnit, introspectedTable);

        // 为Service类添加import类
        // 添加实现的接口的包
        controllerCompilationUnit.addImportedType(new FullyQualifiedJavaType("org.springframework.stereotype.Controller"));
        controllerCompilationUnit.addImportedType(new FullyQualifiedJavaType("org.springframework.web.bind.annotation.*"));
        controllerCompilationUnit.addImportedType(new FullyQualifiedJavaType(servicePackage + "." + domainObjectName + "Service"));
        controllerCompilationUnit.addImportedType(new FullyQualifiedJavaType("org.springframework.beans.factory.annotation.Autowired"));
        controllerCompilationUnit.addImportedType(new FullyQualifiedJavaType(modalFullName));// 对应的实体类
        controllerCompilationUnit.addImportedType(new FullyQualifiedJavaType("java.util.List"));
        controllerCompilationUnit.addImportedType(new FullyQualifiedJavaType("org.springframework.http.ResponseEntity"));
        controllerCompilationUnit.addImportedType(new FullyQualifiedJavaType("org.springframework.http.HttpStatus"));

        GeneratedJavaFile controller = new GeneratedJavaFile(controllerCompilationUnit, targetProject, "UTF-8", getContext().getJavaFormatter());

        // mybatis会根据List<GeneratedJavaFile> super_result中的GeneratedJavaFile实例来生成对应的Java文件
        super_result.add(controller);
    }

    // Controller添加方法
    private void setControllerMethod(TopLevelClass controllerCompilationUnit, IntrospectedTable introspectedTable) {
        // 创建deleteByPrimaryKey方法,对应Service中的deleteByPrimaryKey方法
        // 参考DeleteByPrimaryKeyMethodGenerator.addInterfaceElements()方法
        addDeleteByPrimarykeyMethod4Controller(controllerCompilationUnit, introspectedTable);

        // 创建insert方法,对应Service中的insert方法
        // 参考InsertMethodGenerator.addInterfaceElements()方法
        addInsertMethod4Controller(controllerCompilationUnit, introspectedTable);

        // 创建insertSelective方法,对应Service中的insertSelective方法
        // 参考InsertSelectiveMethodGenerator.addInterfaceElements()方法
        addInsertSelectiveMethod4Controller(controllerCompilationUnit, introspectedTable);

        // 创建updateByPrimaryKey方法,对应Service中的updateByPrimaryKey方法d
        // 参考UpdateByPrimaryKeyMethodGenerator.addInterfaceElements()方法
        addUpdateByPrimaryKeyMethod4Controller(controllerCompilationUnit, introspectedTable);

        // 创建updateByPrimaryKeySelective方法,对应Service中的updateByPrimaryKeySelective方法
        // 参考UpdateByPrimaryKeySelectiveMethodGenerator.addInterfaceElements()方法
        addUpdateByPrimaryKeySelectiveMethod4Controller(controllerCompilationUnit, introspectedTable);

        // 创建selectByPrimaryKey方法,对应Service中的selectByPrimaryKey方法
        // 参考SelectByPrimaryKeyMethodGenerator.addInterfaceElements()方法
        addSelectByPrimaryKeyMethod4Controller(controllerCompilationUnit, introspectedTable);

        // 创建selectByPage方法,对应Service中的selectByPage方法,与 https://github.com/linweiyu21/PaginationPlugin 插件配合时使用
        //addSelectByPageMethod4Controller(controllerCompilationUnit, introspectedTable);
    }

    private void addSelectByPageMethod4Controller(TopLevelClass controllerCompilationUnit, IntrospectedTable introspectedTable) {
        String domainObjectName = introspectedTable.getTableConfiguration().getDomainObjectName();

        Set<FullyQualifiedJavaType> importedTypes = new TreeSet<FullyQualifiedJavaType>();
        Method method = new Method();
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setReturnType(new FullyQualifiedJavaType("ResponseEntity<List<" + domainObjectName + ">>"));
        method.setName("selectByPage");

        // 添加方法参数
        // offsetParam
        Parameter offsetParam = new Parameter(new FullyQualifiedJavaType("Long"), "offset");
        Parameter limitParam = new Parameter(new FullyQualifiedJavaType("Long"), "limit");

        offsetParam.addAnnotation("@PathVariable(\"offset\")");
        limitParam.addAnnotation("@PathVariable(\"limit\")");

        method.addParameter(offsetParam); //$NON-NLS-1$
        method.addParameter(limitParam); //$NON-NLS-1$

        method.addAnnotation("@RequestMapping(value = \"/page/{offset}/{limit}\", method = RequestMethod.GET, produces = {\"application/json;charset=UTF-8\"})");

        // addBodyline,必须配置bodyline,方法才有实现体,否则这个方法就是个abstract方法了
        List<Parameter> parameters = method.getParameters();
        StringBuilder sb = new StringBuilder();
        for (Parameter p : parameters) {
            sb.append(p.getName());
            sb.append(",");
        }
        sb.delete(sb.lastIndexOf(","), sb.length());
        method.addBodyLine("List<" + domainObjectName + "> result = this." + toLowerCaseFirstOne(domainObjectName) + "Service.selectByPage(" + sb.toString() + ");");
        method.addBodyLine("return new ResponseEntity<List<" + domainObjectName + ">>(result,HttpStatus.OK);");

        if (context.getPlugins().clientDeleteByPrimaryKeyMethodGenerated(
                method, controllerCompilationUnit, introspectedTable)) {
            controllerCompilationUnit.addImportedTypes(importedTypes);
            controllerCompilationUnit.addMethod(method);
        }
    }

    private void addUpdateByPrimaryKeyMethod4Controller(TopLevelClass controllerCompilationUnit, IntrospectedTable introspectedTable) {
        String domainObjectName = introspectedTable.getTableConfiguration().getDomainObjectName();

        Set<FullyQualifiedJavaType> importedTypes = new TreeSet<FullyQualifiedJavaType>();
        Method method = new Method();
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setReturnType(new FullyQualifiedJavaType("ResponseEntity<Integer>"));
        method.setName(introspectedTable.getUpdateByPrimaryKeyStatementId());

        // 添加方法参数
        FullyQualifiedJavaType parameterType;
        parameterType = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());
        importedTypes.add(parameterType);
        method.addParameter(new Parameter(parameterType, "record")); //$NON-NLS-1$

        // 添加方法注解
        StringBuilder pathVar = new StringBuilder();
        List<IntrospectedColumn> introspectedColumns = introspectedTable.getPrimaryKeyColumns();
        for (IntrospectedColumn introspectedColumn : introspectedColumns) {
            FullyQualifiedJavaType type = introspectedColumn.getFullyQualifiedJavaType();
            importedTypes.add(type);
            Parameter parameter = new Parameter(type, introspectedColumn.getJavaProperty());
            pathVar.append("{");
            pathVar.append(parameter.getName());
            pathVar.append("}");
            pathVar.append("/");
        }
        pathVar.deleteCharAt(pathVar.lastIndexOf("/"));
        method.addAnnotation("@RequestMapping(value = \"/" + pathVar.toString() + "\", method = RequestMethod.PUT, produces = {\"application/json;charset=UTF-8\"})");

        // addBodyline,必须配置bodyline,方法才有实现体,否则这个方法就是个abstract方法了
        List<Parameter> parameters = method.getParameters();
        StringBuilder sb = new StringBuilder();
        for (Parameter p : parameters) {
            sb.append(p.getName());
            sb.append(",");
        }
        sb.delete(sb.lastIndexOf(","), sb.length());
        method.addBodyLine("int resultCount = this." + toLowerCaseFirstOne(domainObjectName) + "Service." + introspectedTable.getUpdateByPrimaryKeyStatementId() + "(" + sb.toString() + ");");
        method.addBodyLine("return new ResponseEntity<Integer>(resultCount,HttpStatus.OK);");

        if (context.getPlugins().clientDeleteByPrimaryKeyMethodGenerated(
                method, controllerCompilationUnit, introspectedTable)) {
            controllerCompilationUnit.addImportedTypes(importedTypes);
            controllerCompilationUnit.addMethod(method);
        }
    }

    private void addUpdateByPrimaryKeySelectiveMethod4Controller(TopLevelClass controllerCompilationUnit, IntrospectedTable introspectedTable) {
        String domainObjectName = introspectedTable.getTableConfiguration().getDomainObjectName();

        Set<FullyQualifiedJavaType> importedTypes = new TreeSet<FullyQualifiedJavaType>();
        Method method = new Method();
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setReturnType(new FullyQualifiedJavaType("ResponseEntity<Integer>"));
        method.setName(introspectedTable.getUpdateByPrimaryKeySelectiveStatementId());

        // 添加方法参数
        FullyQualifiedJavaType parameterType;
        parameterType = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());
        importedTypes.add(parameterType);
        method.addParameter(new Parameter(parameterType, "record")); //$NON-NLS-1$

        // 添加方法注解
        StringBuilder pathVar = new StringBuilder();
        List<IntrospectedColumn> introspectedColumns = introspectedTable.getPrimaryKeyColumns();
        for (IntrospectedColumn introspectedColumn : introspectedColumns) {
            FullyQualifiedJavaType type = introspectedColumn.getFullyQualifiedJavaType();
            importedTypes.add(type);
            Parameter parameter = new Parameter(type, introspectedColumn.getJavaProperty());
            pathVar.append("{");
            pathVar.append(parameter.getName());
            pathVar.append("}");
            pathVar.append("/");
        }
        pathVar.deleteCharAt(pathVar.lastIndexOf("/"));
        method.addAnnotation("@RequestMapping(value = \"/selective/" + pathVar.toString() + "\", method = RequestMethod.PUT, produces = {\"application/json;charset=UTF-8\"})");

        // addBodyline,必须配置bodyline,方法才有实现体,否则这个方法就是个abstract方法了
        List<Parameter> parameters = method.getParameters();
        StringBuilder sb = new StringBuilder();
        for (Parameter p : parameters) {
            sb.append(p.getName());
            sb.append(",");
        }
        sb.delete(sb.lastIndexOf(","), sb.length());
        method.addBodyLine("int resultCount = this." + toLowerCaseFirstOne(domainObjectName) + "Service." + introspectedTable.getUpdateByPrimaryKeySelectiveStatementId() + "(" + sb.toString() + ");");
        method.addBodyLine("return new ResponseEntity<Integer>(resultCount,HttpStatus.OK);");

        if (context.getPlugins().clientDeleteByPrimaryKeyMethodGenerated(
                method, controllerCompilationUnit, introspectedTable)) {
            controllerCompilationUnit.addImportedTypes(importedTypes);
            controllerCompilationUnit.addMethod(method);
        }
    }

    private void addSelectByPrimaryKeyMethod4Controller(TopLevelClass controllerCompilationUnit, IntrospectedTable introspectedTable) {
        String domainObjectName = introspectedTable.getTableConfiguration().getDomainObjectName();

        Set<FullyQualifiedJavaType> importedTypes = new TreeSet<FullyQualifiedJavaType>();
        Method method = new Method();
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setReturnType(new FullyQualifiedJavaType("ResponseEntity<" + domainObjectName + ">"));
        method.setName(introspectedTable.getSelectByPrimaryKeyStatementId());

        // 添加方法参数
        StringBuilder pathVar = new StringBuilder();
        List<IntrospectedColumn> introspectedColumns = introspectedTable.getPrimaryKeyColumns();
        for (IntrospectedColumn introspectedColumn : introspectedColumns) {
            FullyQualifiedJavaType type = introspectedColumn.getFullyQualifiedJavaType();
            importedTypes.add(type);
            Parameter parameter = new Parameter(type, introspectedColumn.getJavaProperty());
            parameter.addAnnotation("@PathVariable(\"" + parameter.getName() + "\")");
            pathVar.append("{");
            pathVar.append(parameter.getName());
            pathVar.append("}");
            pathVar.append("/");
            method.addParameter(parameter);
        }

        pathVar.delete(pathVar.lastIndexOf("/"), pathVar.length());
        // 添加方法注解
        method.addAnnotation("@RequestMapping(value = \"/" + pathVar.toString() + "\", method = RequestMethod.GET, produces = {\"application/json;charset=UTF-8\"})");

        // addBodyline,必须配置bodyline,方法才有实现体,否则这个方法就是个abstract方法了
        List<Parameter> parameters = method.getParameters();
        StringBuilder sb = new StringBuilder();
        for (Parameter p : parameters) {
            sb.append(p.getName());
            sb.append(",");
        }
        sb.delete(sb.lastIndexOf(","), sb.length());
        method.addBodyLine(domainObjectName + " " + toLowerCaseFirstOne(domainObjectName) + " = this." + toLowerCaseFirstOne(domainObjectName) + "Service." + introspectedTable.getSelectByPrimaryKeyStatementId() + "(" + sb.toString() + ");");
        method.addBodyLine("return new ResponseEntity<" + domainObjectName + ">(" + toLowerCaseFirstOne(domainObjectName) + ",HttpStatus.OK);");

        if (context.getPlugins().clientDeleteByPrimaryKeyMethodGenerated(
                method, controllerCompilationUnit, introspectedTable)) {
            controllerCompilationUnit.addImportedTypes(importedTypes);
            controllerCompilationUnit.addMethod(method);
        }
    }

    private void addInsertSelectiveMethod4Controller(TopLevelClass controllerCompilationUnit, IntrospectedTable introspectedTable) {
        String domainObjectName = introspectedTable.getTableConfiguration().getDomainObjectName();

        Set<FullyQualifiedJavaType> importedTypes = new TreeSet<FullyQualifiedJavaType>();
        Method method = new Method();
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setReturnType(new FullyQualifiedJavaType("ResponseEntity<Integer>"));
        method.setName(introspectedTable.getInsertSelectiveStatementId());

        // 添加方法参数
        FullyQualifiedJavaType parameterType;
        parameterType = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());
        importedTypes.add(parameterType);
        method.addParameter(new Parameter(parameterType, "record")); //$NON-NLS-1$

        // 添加方法注解
        method.addAnnotation("@RequestMapping(value = \"/selective\", method = RequestMethod.POST, produces = {\"application/json;charset=UTF-8\"})");

        method.addBodyLine("int resultCount = this." + toLowerCaseFirstOne(domainObjectName) + "Service." + introspectedTable.getInsertSelectiveStatementId() + "(record);");
        method.addBodyLine("return new ResponseEntity<Integer>(resultCount,HttpStatus.OK);");

        if (context.getPlugins().clientDeleteByPrimaryKeyMethodGenerated(
                method, controllerCompilationUnit, introspectedTable)) {
            controllerCompilationUnit.addImportedTypes(importedTypes);
            controllerCompilationUnit.addMethod(method);
        }
    }

    private void addInsertMethod4Controller(TopLevelClass controllerCompilationUnit, IntrospectedTable introspectedTable) {
        String domainObjectName = introspectedTable.getTableConfiguration().getDomainObjectName();

        Set<FullyQualifiedJavaType> importedTypes = new TreeSet<FullyQualifiedJavaType>();
        Method method = new Method();
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setReturnType(new FullyQualifiedJavaType("ResponseEntity<Integer>"));
        method.setName(introspectedTable.getInsertStatementId());

        // 添加方法参数
        FullyQualifiedJavaType parameterType;
        parameterType = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());
        importedTypes.add(parameterType);
        method.addParameter(new Parameter(parameterType, "record")); //$NON-NLS-1$

        // 添加方法注解
        method.addAnnotation("@RequestMapping(value = \"/\", method = RequestMethod.POST, produces = {\"application/json;charset=UTF-8\"})");

        method.addBodyLine("int resultCount = this." + toLowerCaseFirstOne(domainObjectName) + "Service." + introspectedTable.getInsertStatementId() + "(record);");
        method.addBodyLine("return new ResponseEntity<Integer>(resultCount,HttpStatus.OK);");

        if (context.getPlugins().clientDeleteByPrimaryKeyMethodGenerated(
                method, controllerCompilationUnit, introspectedTable)) {
            controllerCompilationUnit.addImportedTypes(importedTypes);
            controllerCompilationUnit.addMethod(method);
        }
    }

    private void addDeleteByPrimarykeyMethod4Controller(TopLevelClass controllerCompilationUnit, IntrospectedTable introspectedTable) {
        String domainObjectName = introspectedTable.getTableConfiguration().getDomainObjectName();

        Set<FullyQualifiedJavaType> importedTypes = new TreeSet<FullyQualifiedJavaType>();
        Method method = new Method();
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setReturnType(new FullyQualifiedJavaType("ResponseEntity<Integer>"));
        method.setName(introspectedTable.getDeleteByPrimaryKeyStatementId());

        // 添加方法参数
        StringBuilder pathVar = new StringBuilder();
        List<IntrospectedColumn> introspectedColumns = introspectedTable.getPrimaryKeyColumns();
        for (IntrospectedColumn introspectedColumn : introspectedColumns) {
            FullyQualifiedJavaType type = introspectedColumn.getFullyQualifiedJavaType();
            importedTypes.add(type);
            Parameter parameter = new Parameter(type, introspectedColumn.getJavaProperty());
            parameter.addAnnotation("@PathVariable(\"" + parameter.getName() + "\")");
            pathVar.append("{");
            pathVar.append(parameter.getName());
            pathVar.append("}");
            pathVar.append("/");
            method.addParameter(parameter);
        }

        pathVar.delete(pathVar.lastIndexOf("/"), pathVar.length());
        // 添加方法注解
        method.addAnnotation("@RequestMapping(value = \"/" + pathVar.toString() + "\", method = RequestMethod.DELETE, produces = {\"application/json;charset=UTF-8\"})");

        // addBodyline,必须配置bodyline,方法才有实现体,否则这个方法就是个abstract方法了
        List<Parameter> parameters = method.getParameters();
        StringBuilder sb = new StringBuilder();
        for (Parameter p : parameters) {
            sb.append(p.getName());
            sb.append(",");
        }
        sb.delete(sb.lastIndexOf(","), sb.length());
        method.addBodyLine("int resultCount = this." + toLowerCaseFirstOne(domainObjectName) + "Service." + introspectedTable.getDeleteByPrimaryKeyStatementId() + "(" + sb.toString() + ");");
        method.addBodyLine("return new ResponseEntity<Integer>(resultCount,HttpStatus.OK);");

        if (context.getPlugins().clientDeleteByPrimaryKeyMethodGenerated(
                method, controllerCompilationUnit, introspectedTable)) {
            controllerCompilationUnit.addImportedTypes(importedTypes);
            controllerCompilationUnit.addMethod(method);
        }
    }

    // Controller类添加Field
    private void setControllerField(TopLevelClass controllerCompilationUnit, IntrospectedTable introspectedTable, String servicePackage) {
        // 实体类的类名
        String domainObjectName = introspectedTable.getTableConfiguration().getDomainObjectName();

        // 添加日志类
        Field loggerField = new Field();
        loggerField.setName("logger");
        loggerField.setVisibility(JavaVisibility.PRIVATE);
        loggerField.setFinal(true);
        loggerField.setType(new FullyQualifiedJavaType("Logger"));
        loggerField.setInitializationString("LoggerFactory.getLogger(this.getClass())");
        controllerCompilationUnit.addField(loggerField);
        controllerCompilationUnit.addImportedType(new FullyQualifiedJavaType("org.slf4j.Logger"));
        controllerCompilationUnit.addImportedType(new FullyQualifiedJavaType("org.slf4j.LoggerFactory"));

        // 添加Service类
        Field serviceField = new Field();
        serviceField.setName(toLowerCaseFirstOne(domainObjectName) + "Service");
        serviceField.setVisibility(JavaVisibility.PRIVATE);
        serviceField.setType(new FullyQualifiedJavaType(domainObjectName + "Service"));
        serviceField.addAnnotation("@Autowired");
        controllerCompilationUnit.addField(serviceField);
        controllerCompilationUnit.addImportedType(new FullyQualifiedJavaType(servicePackage + "." + domainObjectName + "Service"));
    }

    /**
     * 生成Service相关类
     *
     * @param introspectedTable
     * @param super_result
     * @param domainObjectName
     * @param modalFullName
     * @param targetProject
     * @param servicePackage
     * @param serviceImplPackage
     */
    private void generateServiceFile(IntrospectedTable introspectedTable, List<GeneratedJavaFile> super_result, String domainObjectName, String modalFullName, String targetProject, String servicePackage, String serviceImplPackage) {
        // 生成Service接口
        generateBaseServiceJavaFile(introspectedTable, super_result, domainObjectName, targetProject, servicePackage);
        generateServiceJavaFile(introspectedTable, super_result, domainObjectName, targetProject, servicePackage);

        // 生成ServiceImpl类
        generateServiceImplJavaFile(introspectedTable, super_result, domainObjectName, modalFullName, targetProject, servicePackage, serviceImplPackage);
    }

    /**
     * // 生成Service接口
     *
     * @param introspectedTable
     * @param super_result
     * @param domainObjectName
     * @param targetProject
     * @param servicePackage
     */
    private void generateBaseServiceJavaFile(IntrospectedTable introspectedTable, List<GeneratedJavaFile> super_result, String domainObjectName, String targetProject, String servicePackage) {
        // 生成Service接口
        // typeName为接口的全限定类名
        String typeName = servicePackage + "." + BASE_SERVICE_NAME;
        Interface serviceCompilationUnit = new Interface(typeName + "<T>");
        serviceCompilationUnit.setVisibility(JavaVisibility.PUBLIC);

        // 为接口添加方法
        setBaseServiceMethod(serviceCompilationUnit, introspectedTable);

        // 为接口添加import
        serviceCompilationUnit.addImportedType(new FullyQualifiedJavaType("java.util.List"));

        GeneratedJavaFile service = new GeneratedJavaFile(serviceCompilationUnit, targetProject, "UTF-8", getContext().getJavaFormatter());

        // mybatis会根据List<GeneratedJavaFile> super_result中的GeneratedJavaFile实例来生成对应的Java文件
        super_result.add(service);
    }

    /**
     * // 生成Service接口
     *
     * @param introspectedTable
     * @param super_result
     * @param domainObjectName
     * @param targetProject
     * @param servicePackage
     */
    private void generateServiceJavaFile(IntrospectedTable introspectedTable, List<GeneratedJavaFile> super_result, String domainObjectName, String targetProject, String servicePackage) {
        // 生成Service接口
        // typeName为接口的全限定类名
        String typeName = servicePackage + "." + domainObjectName + "Service";
        Interface serviceCompilationUnit = new Interface(typeName);
        serviceCompilationUnit.setVisibility(JavaVisibility.PUBLIC);

        // 为接口添加方法
//        setServiceMethod(serviceCompilationUnit, introspectedTable);

        // 为接口添加import
//        serviceCompilationUnit.addImportedType(new FullyQualifiedJavaType("java.util.List"));
        serviceCompilationUnit.addImportedType(new FullyQualifiedJavaType(introspectedTable.getBaseRecordType()));
        serviceCompilationUnit.addSuperInterface(new FullyQualifiedJavaType(BASE_SERVICE_NAME + "<" + domainObjectName + ">"));
        GeneratedJavaFile service = new GeneratedJavaFile(serviceCompilationUnit, targetProject, "UTF-8", getContext().getJavaFormatter());

        // mybatis会根据List<GeneratedJavaFile> super_result中的GeneratedJavaFile实例来生成对应的Java文件
        super_result.add(service);
    }

    /**
     * 为接口添加方法
     *
     * @param serviceCompilationUnit
     * @param introspectedTable
     */
    private void setBaseServiceMethod(Interface serviceCompilationUnit, IntrospectedTable introspectedTable) {
        // 创建deleteByPrimaryKey方法,对应ServiceImpl中的deleteByPrimaryKey方法
        // 参考DeleteByPrimaryKeyMethodGenerator.addInterfaceElements()方法
//        addDeleteByPrimarykeyMethod4Interface(serviceCompilationUnit, introspectedTable);
        setAddOrUpdate(serviceCompilationUnit, introspectedTable);
        setDelete(serviceCompilationUnit, introspectedTable);
        setGet(serviceCompilationUnit, introspectedTable);
        setList2(serviceCompilationUnit, introspectedTable);
        setList(serviceCompilationUnit, introspectedTable);
        setListOfPaging(serviceCompilationUnit, introspectedTable);
        setBatchInsert(serviceCompilationUnit, introspectedTable);
        // 创建insert方法,对应ServiceImpl中的insert方法
        // 参考InsertMethodGenerator.addInterfaceElements()方法
        /*addInsertMethod4Interface(serviceCompilationUnit, introspectedTable);

        // 创建insertSelective方法,对应ServiceImpl中的insertSelective方法
        // 参考InsertSelectiveMethodGenerator.addInterfaceElements()方法
        addInsertSelectiveMethod4Interface(serviceCompilationUnit, introspectedTable);

        // 创建selectByPrimaryKey方法,对应ServiceImpl中的selectByPrimaryKey方法
        // 参考SelectByPrimaryKeyMethodGenerator.addInterfaceElements()方法
        addSelectByPrimaryKeyMethod4Interface(serviceCompilationUnit, introspectedTable);

        // 创建updateByPrimaryKeySelective方法,对应ServiceImpl中的updateByPrimaryKeySelective方法
        // 参考UpdateByPrimaryKeySelectiveMethodGenerator.addInterfaceElements()方法
        addUpdateByPrimaryKeySelectiveMethod4Interface(serviceCompilationUnit, introspectedTable);

        // 创建updateByPrimaryKey方法,对应ServiceImpl中的updateByPrimaryKey方法
        // 参考UpdateByPrimaryKeyMethodGenerator.addInterfaceElements()方法
        addUpdateByPrimaryKeyMethod4Interface(serviceCompilationUnit, introspectedTable);*/

        // 创建selectByPage方法,对应ServiceImpl中的selectByPage方法,与 https://github.com/linweiyu21/PaginationPlugin 插件配合时使用
        //addSelectByPageMethod4Interface(serviceCompilationUnit, introspectedTable);
    }

    /**
     * 为接口添加方法
     *
     * @param serviceCompilationUnit
     * @param introspectedTable
     */
    private void setServiceMethod(Interface serviceCompilationUnit, IntrospectedTable introspectedTable) {
        // 创建deleteByPrimaryKey方法,对应ServiceImpl中的deleteByPrimaryKey方法
        // 参考DeleteByPrimaryKeyMethodGenerator.addInterfaceElements()方法
        addDeleteByPrimarykeyMethod4Interface(serviceCompilationUnit, introspectedTable);

        // 创建insert方法,对应ServiceImpl中的insert方法
        // 参考InsertMethodGenerator.addInterfaceElements()方法
        addInsertMethod4Interface(serviceCompilationUnit, introspectedTable);

        // 创建insertSelective方法,对应ServiceImpl中的insertSelective方法
        // 参考InsertSelectiveMethodGenerator.addInterfaceElements()方法
        addInsertSelectiveMethod4Interface(serviceCompilationUnit, introspectedTable);

        // 创建selectByPrimaryKey方法,对应ServiceImpl中的selectByPrimaryKey方法
        // 参考SelectByPrimaryKeyMethodGenerator.addInterfaceElements()方法
        addSelectByPrimaryKeyMethod4Interface(serviceCompilationUnit, introspectedTable);

        // 创建updateByPrimaryKeySelective方法,对应ServiceImpl中的updateByPrimaryKeySelective方法
        // 参考UpdateByPrimaryKeySelectiveMethodGenerator.addInterfaceElements()方法
        addUpdateByPrimaryKeySelectiveMethod4Interface(serviceCompilationUnit, introspectedTable);

        // 创建updateByPrimaryKey方法,对应ServiceImpl中的updateByPrimaryKey方法
        // 参考UpdateByPrimaryKeyMethodGenerator.addInterfaceElements()方法
        addUpdateByPrimaryKeyMethod4Interface(serviceCompilationUnit, introspectedTable);

        // 创建selectByPage方法,对应ServiceImpl中的selectByPage方法,与 https://github.com/linweiyu21/PaginationPlugin 插件配合时使用
        //addSelectByPageMethod4Interface(serviceCompilationUnit, introspectedTable);
    }

    private void addSelectByPageMethod4Interface(Interface serviceCompilationUnit, IntrospectedTable introspectedTable) {
        String domainObjectName = introspectedTable.getTableConfiguration().getDomainObjectName();

        Set<FullyQualifiedJavaType> importedTypes = new TreeSet<FullyQualifiedJavaType>();

        Method method = new Method();
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setReturnType(new FullyQualifiedJavaType("List<" + domainObjectName + ">"));
        method.setName("selectByPage");
        method.addParameter(new Parameter(new FullyQualifiedJavaType("Long"), "offset")); //$NON-NLS-1$
        method.addParameter(new Parameter(new FullyQualifiedJavaType("Long"), "limit")); //$NON-NLS-1$

        // 不需要方法实现体

        if (context.getPlugins()
                .clientUpdateByPrimaryKeySelectiveMethodGenerated(method,
                        serviceCompilationUnit, introspectedTable)) {
            serviceCompilationUnit.addImportedTypes(importedTypes);
            serviceCompilationUnit.addMethod(method);
        }
    }

    private void addUpdateByPrimaryKeyMethod4Interface(Interface serviceCompilationUnit, IntrospectedTable introspectedTable) {
        String domainObjectName = introspectedTable.getTableConfiguration().getDomainObjectName();

        Set<FullyQualifiedJavaType> importedTypes = new TreeSet<FullyQualifiedJavaType>();
        FullyQualifiedJavaType parameterType;

        if (introspectedTable.getRules().generateRecordWithBLOBsClass()) {
            parameterType = new FullyQualifiedJavaType(introspectedTable
                    .getRecordWithBLOBsType());
        } else {
            parameterType = new FullyQualifiedJavaType(introspectedTable
                    .getBaseRecordType());
        }

        importedTypes.add(parameterType);

        Method method = new Method();
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setReturnType(FullyQualifiedJavaType.getIntInstance());
        method.setName(introspectedTable
                .getUpdateByPrimaryKeyStatementId());
        method.addParameter(new Parameter(parameterType, "record")); //$NON-NLS-1$

        // 不需要方法实现体

        if (context.getPlugins()
                .clientUpdateByPrimaryKeySelectiveMethodGenerated(method,
                        serviceCompilationUnit, introspectedTable)) {
            serviceCompilationUnit.addImportedTypes(importedTypes);
            serviceCompilationUnit.addMethod(method);
        }
    }

    private void addUpdateByPrimaryKeySelectiveMethod4Interface(Interface serviceCompilationUnit, IntrospectedTable introspectedTable) {
        String domainObjectName = introspectedTable.getTableConfiguration().getDomainObjectName();

        Set<FullyQualifiedJavaType> importedTypes = new TreeSet<FullyQualifiedJavaType>();
        FullyQualifiedJavaType parameterType;

        if (introspectedTable.getRules().generateRecordWithBLOBsClass()) {
            parameterType = new FullyQualifiedJavaType(introspectedTable
                    .getRecordWithBLOBsType());
        } else {
            parameterType = new FullyQualifiedJavaType(introspectedTable
                    .getBaseRecordType());
        }

        importedTypes.add(parameterType);

        Method method = new Method();
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setReturnType(FullyQualifiedJavaType.getIntInstance());
        method.setName(introspectedTable
                .getUpdateByPrimaryKeySelectiveStatementId());
        method.addParameter(new Parameter(parameterType, "record")); //$NON-NLS-1$

        // 不需要方法实现体

        if (context.getPlugins()
                .clientUpdateByPrimaryKeySelectiveMethodGenerated(method,
                        serviceCompilationUnit, introspectedTable)) {
            serviceCompilationUnit.addImportedTypes(importedTypes);
            serviceCompilationUnit.addMethod(method);
        }
    }

    private void addSelectByPrimaryKeyMethod4Interface(Interface serviceCompilationUnit, IntrospectedTable introspectedTable) {
        String domainObjectName = introspectedTable.getTableConfiguration().getDomainObjectName();

        Set<FullyQualifiedJavaType> importedTypes = new TreeSet<FullyQualifiedJavaType>();
        Method method = new Method();
        method.setVisibility(JavaVisibility.PUBLIC);

        FullyQualifiedJavaType returnType = introspectedTable.getRules()
                .calculateAllFieldsClass();
        method.setReturnType(returnType);
        importedTypes.add(returnType);

        method.setName(introspectedTable.getSelectByPrimaryKeyStatementId());


        List<IntrospectedColumn> introspectedColumns = introspectedTable
                .getPrimaryKeyColumns();

        for (IntrospectedColumn introspectedColumn : introspectedColumns) {
            FullyQualifiedJavaType type = introspectedColumn
                    .getFullyQualifiedJavaType();
            importedTypes.add(type);
            Parameter parameter = new Parameter(type, introspectedColumn
                    .getJavaProperty());
            method.addParameter(parameter);
        }

        // 不需要方法实现体

        if (context.getPlugins().clientSelectByPrimaryKeyMethodGenerated(
                method, serviceCompilationUnit, introspectedTable)) {
            serviceCompilationUnit.addImportedTypes(importedTypes);
            serviceCompilationUnit.addMethod(method);
        }

    }

    private void addInsertSelectiveMethod4Interface(Interface serviceCompilationUnit, IntrospectedTable introspectedTable) {
        String domainObjectName = introspectedTable.getTableConfiguration().getDomainObjectName();

        Set<FullyQualifiedJavaType> importedTypes = new TreeSet<FullyQualifiedJavaType>();
        Method method = new Method();

        method.setReturnType(FullyQualifiedJavaType.getIntInstance());
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setName(introspectedTable.getInsertSelectiveStatementId());

        FullyQualifiedJavaType parameterType = introspectedTable.getRules()
                .calculateAllFieldsClass();

        importedTypes.add(parameterType);
        method.addParameter(new Parameter(parameterType, "record")); //$NON-NLS-1$

        // 不需要方法实现体

        if (context.getPlugins().clientInsertSelectiveMethodGenerated(
                method, serviceCompilationUnit, introspectedTable)) {
            serviceCompilationUnit.addImportedTypes(importedTypes);
            serviceCompilationUnit.addMethod(method);
        }

    }

    private void addInsertMethod4Interface(Interface serviceCompilationUnit, IntrospectedTable introspectedTable) {
        String domainObjectName = introspectedTable.getTableConfiguration().getDomainObjectName();

        Set<FullyQualifiedJavaType> importedTypes = new TreeSet<FullyQualifiedJavaType>();
        Method method = new Method();

        method.setReturnType(FullyQualifiedJavaType.getIntInstance());
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setName(introspectedTable.getInsertStatementId());

        FullyQualifiedJavaType parameterType;
        parameterType = new FullyQualifiedJavaType(
                introspectedTable.getBaseRecordType());

        importedTypes.add(parameterType);
        method.addParameter(new Parameter(parameterType, "record")); //$NON-NLS-1$

        // 不需要方法实现体

        if (context.getPlugins().clientInsertMethodGenerated(method, serviceCompilationUnit,
                introspectedTable)) {
            serviceCompilationUnit.addImportedTypes(importedTypes);
            serviceCompilationUnit.addMethod(method);
        }
    }

    /**
     * 新增或更新
     *
     * @param serviceCompilationUnit
     * @param introspectedTable
     */
    private void setBatchInsert(Interface serviceCompilationUnit, IntrospectedTable introspectedTable) {
        String domainObjectName = introspectedTable.getTableConfiguration().getDomainObjectName();

        Set<FullyQualifiedJavaType> importedTypes = new TreeSet<FullyQualifiedJavaType>();
        Method method = new Method();

        method.setReturnType(FullyQualifiedJavaType.getIntInstance());
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setName(BaseServiceMethod.batchInsert);

        FullyQualifiedJavaType parameterType;
        parameterType = new FullyQualifiedJavaType("List<T>");
        importedTypes.add(parameterType);
        method.addParameter(new Parameter(parameterType, "entitys"));

        // 不需要方法实现体

        if (context.getPlugins().clientInsertMethodGenerated(method, serviceCompilationUnit,
                introspectedTable)) {
            serviceCompilationUnit.addImportedTypes(importedTypes);
            serviceCompilationUnit.addMethod(method);
        }
    }
    /**
     * 新增或更新
     *
     * @param serviceCompilationUnit
     * @param introspectedTable
     */
    private void setAddOrUpdate(Interface serviceCompilationUnit, IntrospectedTable introspectedTable) {
        String domainObjectName = introspectedTable.getTableConfiguration().getDomainObjectName();

        Set<FullyQualifiedJavaType> importedTypes = new TreeSet<FullyQualifiedJavaType>();
        Method method = new Method();

        method.setReturnType(FullyQualifiedJavaType.getBooleanPrimitiveInstance());
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setName("addOrUpdate");

        FullyQualifiedJavaType parameterType;
        parameterType = new FullyQualifiedJavaType("T");
        importedTypes.add(parameterType);
        method.addParameter(new Parameter(parameterType, "entity"));

        // 不需要方法实现体

        if (context.getPlugins().clientInsertMethodGenerated(method, serviceCompilationUnit,
                introspectedTable)) {
            serviceCompilationUnit.addImportedTypes(importedTypes);
            serviceCompilationUnit.addMethod(method);
        }
    }

    private void addDeleteByPrimarykeyMethod4Interface(Interface serviceCompilationUnit, IntrospectedTable introspectedTable) {
        String domainObjectName = introspectedTable.getTableConfiguration().getDomainObjectName();

        Set<FullyQualifiedJavaType> importedTypes = new TreeSet<FullyQualifiedJavaType>();
        Method method = new Method();
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setReturnType(FullyQualifiedJavaType.getIntInstance());
        method.setName(introspectedTable.getDeleteByPrimaryKeyStatementId());


        // no primary key class - fields are in the base class
        // if more than one PK field, then we need to annotate the
        // parameters
        // for MyBatis
        List<IntrospectedColumn> introspectedColumns = introspectedTable
                .getPrimaryKeyColumns();

        for (IntrospectedColumn introspectedColumn : introspectedColumns) {
            FullyQualifiedJavaType type = introspectedColumn
                    .getFullyQualifiedJavaType();
            importedTypes.add(type);
            Parameter parameter = new Parameter(type, introspectedColumn
                    .getJavaProperty());
            method.addParameter(parameter);
        }

        // 不需要方法实现体

        if (context.getPlugins().clientDeleteByPrimaryKeyMethodGenerated(
                method, serviceCompilationUnit, introspectedTable)) {
            serviceCompilationUnit.addImportedTypes(importedTypes);
            serviceCompilationUnit.addMethod(method);
        }
    }

    /**
     * 新增delete
     *
     * @param serviceCompilationUnit
     * @param introspectedTable
     */
    private void setDelete(Interface serviceCompilationUnit, IntrospectedTable introspectedTable) {
        String domainObjectName = introspectedTable.getTableConfiguration().getDomainObjectName();

        Set<FullyQualifiedJavaType> importedTypes = new TreeSet<FullyQualifiedJavaType>();
        Method method = new Method();
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setReturnType(FullyQualifiedJavaType.getBooleanPrimitiveInstance());
        method.setName("delete");

        List<IntrospectedColumn> introspectedColumns = introspectedTable
                .getPrimaryKeyColumns();
        for (IntrospectedColumn introspectedColumn : introspectedColumns) {
            FullyQualifiedJavaType type = introspectedColumn
                    .getFullyQualifiedJavaType();
            importedTypes.add(type);
            Parameter parameter = new Parameter(type, introspectedColumn
                    .getJavaProperty());
            method.addParameter(parameter);
        }

        // 不需要方法实现体
        if (context.getPlugins().clientDeleteByPrimaryKeyMethodGenerated(
                method, serviceCompilationUnit, introspectedTable)) {
            serviceCompilationUnit.addImportedTypes(importedTypes);
            serviceCompilationUnit.addMethod(method);
        }
    }

    /**
     * 新增delete
     *
     * @param serviceCompilationUnit
     * @param introspectedTable
     */
    private void setGet(Interface serviceCompilationUnit, IntrospectedTable introspectedTable) {
        String domainObjectName = introspectedTable.getTableConfiguration().getDomainObjectName();

        Set<FullyQualifiedJavaType> importedTypes = new TreeSet<FullyQualifiedJavaType>();
        Method method = new Method();
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setReturnType(new FullyQualifiedJavaType("T"));
        method.setName("get");

        List<IntrospectedColumn> introspectedColumns = introspectedTable
                .getPrimaryKeyColumns();
        for (IntrospectedColumn introspectedColumn : introspectedColumns) {
            FullyQualifiedJavaType type = introspectedColumn
                    .getFullyQualifiedJavaType();
            importedTypes.add(type);
            Parameter parameter = new Parameter(type, introspectedColumn
                    .getJavaProperty());
            method.addParameter(parameter);
        }

        // 不需要方法实现体
        if (context.getPlugins().clientDeleteByPrimaryKeyMethodGenerated(
                method, serviceCompilationUnit, introspectedTable)) {
            serviceCompilationUnit.addImportedTypes(importedTypes);
            serviceCompilationUnit.addMethod(method);
        }
    }

    /**
     * 新增 listOfPaging
     *
     * @param serviceCompilationUnit
     * @param introspectedTable
     */
    private void setListOfPaging(Interface serviceCompilationUnit, IntrospectedTable introspectedTable) {
        String domainObjectName = introspectedTable.getTableConfiguration().getDomainObjectName();

        Set<FullyQualifiedJavaType> importedTypes = new TreeSet<FullyQualifiedJavaType>();
        Method method = new Method();
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setReturnType(new FullyQualifiedJavaType("PageInfo"));
        method.setName("listOfPaging");

        // 导入相关包
        serviceCompilationUnit.addImportedType(new FullyQualifiedJavaType("com.github.pagehelper.PageInfo"));

        method.addParameter(new Parameter(new FullyQualifiedJavaType("int"), "pageNum"));
        method.addParameter(new Parameter(new FullyQualifiedJavaType("int"), "pageSize"));
        method.addParameter(new Parameter(new FullyQualifiedJavaType("String"), "sidx"));
        method.addParameter(new Parameter(new FullyQualifiedJavaType("String"), "sord"));
        method.addParameter(new Parameter(new FullyQualifiedJavaType("T"), "query"));

        // 不需要方法实现体
        if (context.getPlugins().clientDeleteByPrimaryKeyMethodGenerated(
                method, serviceCompilationUnit, introspectedTable)) {
            serviceCompilationUnit.addImportedTypes(importedTypes);
            serviceCompilationUnit.addMethod(method);
        }
    }

    /**
     * 新增 listOfPaging
     *
     * @param serviceCompilationUnit
     * @param introspectedTable
     */
    private void setList(Interface serviceCompilationUnit, IntrospectedTable introspectedTable) {
        String domainObjectName = introspectedTable.getTableConfiguration().getDomainObjectName();

        Set<FullyQualifiedJavaType> importedTypes = new TreeSet<FullyQualifiedJavaType>();
        Method method = new Method();
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setReturnType(new FullyQualifiedJavaType("List<T>"));
        method.setName("list");

        // 导入相关包
        serviceCompilationUnit.addImportedType(new FullyQualifiedJavaType("com.github.pagehelper.PageInfo"));

//        int pageNum, int pageSize, String sidx, String sord, T query
        method.addParameter(new Parameter(new FullyQualifiedJavaType("int"), "pageNum"));
        method.addParameter(new Parameter(new FullyQualifiedJavaType("int"), "pageSize"));
        method.addParameter(new Parameter(new FullyQualifiedJavaType("String"), "sidx"));
        method.addParameter(new Parameter(new FullyQualifiedJavaType("String"), "sord"));
        method.addParameter(new Parameter(new FullyQualifiedJavaType("T"), "query"));

        // 不需要方法实现体
        if (context.getPlugins().clientDeleteByPrimaryKeyMethodGenerated(
                method, serviceCompilationUnit, introspectedTable)) {
            serviceCompilationUnit.addImportedTypes(importedTypes);
            serviceCompilationUnit.addMethod(method);
        }
    }

    /**
     * 新增 listOfPaging
     *
     * @param serviceCompilationUnit
     * @param introspectedTable
     */
    private void setList2(Interface serviceCompilationUnit, IntrospectedTable introspectedTable) {
        String domainObjectName = introspectedTable.getTableConfiguration().getDomainObjectName();

        Set<FullyQualifiedJavaType> importedTypes = new TreeSet<FullyQualifiedJavaType>();
        Method method = new Method();
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setReturnType(new FullyQualifiedJavaType("List<T>"));
        method.setName("list");

        // 导入相关包
        serviceCompilationUnit.addImportedType(new FullyQualifiedJavaType("com.github.pagehelper.PageInfo"));

//        int pageNum, int pageSize, String sidx, String sord, T query
        method.addParameter(new Parameter(new FullyQualifiedJavaType("T"), "query"));

        // 不需要方法实现体
        if (context.getPlugins().clientDeleteByPrimaryKeyMethodGenerated(
                method, serviceCompilationUnit, introspectedTable)) {
            serviceCompilationUnit.addImportedTypes(importedTypes);
            serviceCompilationUnit.addMethod(method);
        }
    }

    /**
     * 生成ServiceImpl,Service接口的实现类
     *
     * @param introspectedTable
     * @param super_result
     * @param domainObjectName
     * @param modalFullName
     * @param targetProject
     * @param servicePackage
     * @param serviceImplPackage
     */
    private void generateServiceImplJavaFile(IntrospectedTable introspectedTable, List<GeneratedJavaFile> super_result, String domainObjectName, String modalFullName, String targetProject, String servicePackage, String serviceImplPackage) {
        // ServiceImpl类
        // typeName为类的全限定类名
        String typeName = serviceImplPackage + "." + domainObjectName + "ServiceImpl";
        // TopLevelClass类代表要生成的类,mybatis根据TopLevelClass类代表要生成一个类的field,method等
        TopLevelClass serviceImplCompilationUnit = new TopLevelClass(typeName);
        serviceImplCompilationUnit.setVisibility(JavaVisibility.PUBLIC);
        // 设置类实现的接口
        serviceImplCompilationUnit.addSuperInterface(new FullyQualifiedJavaType(domainObjectName + "Service"));
        // 此类所在包
        String packageName = serviceImplCompilationUnit.getType().getPackageName();

        serviceImplCompilationUnit.addJavaDocLine("/**");
        serviceImplCompilationUnit.addJavaDocLine("* @author " + MyCommentGenerator.author);
        serviceImplCompilationUnit.addJavaDocLine("* @sine " + LocalDateTime.now());
        serviceImplCompilationUnit.addJavaDocLine("* " + domainObjectName + " service");
        serviceImplCompilationUnit.addJavaDocLine("*/");
        // 为类添加注解
        serviceImplCompilationUnit.addAnnotation("@Slf4j");
        serviceImplCompilationUnit.addAnnotation("@Service");

        // 为类添加Field
        setServiceImplField(serviceImplCompilationUnit, introspectedTable);

        // 为类添加方法
        setServiceImplMethod(serviceImplCompilationUnit, introspectedTable);

        // 为Service类添加import类
        // 添加实现的接口的包
        serviceImplCompilationUnit.addImportedType(new FullyQualifiedJavaType(servicePackage + "." + domainObjectName + "Service"));
        serviceImplCompilationUnit.addImportedType(new FullyQualifiedJavaType("org.springframework.beans.factory.annotation.Autowired"));
        serviceImplCompilationUnit.addImportedType(new FullyQualifiedJavaType("org.springframework.stereotype.Service"));
        serviceImplCompilationUnit.addImportedType(new FullyQualifiedJavaType("lombok.extern.slf4j.Slf4j"));
        serviceImplCompilationUnit.addImportedType(new FullyQualifiedJavaType(modalFullName));// 对应的实体类
        serviceImplCompilationUnit.addImportedType(new FullyQualifiedJavaType("java.util.Date"));
        serviceImplCompilationUnit.addImportedType(new FullyQualifiedJavaType("java.util.List"));

        serviceImplCompilationUnit.addImportedType(new FullyQualifiedJavaType("com.github.pagehelper.PageHelper"));
        serviceImplCompilationUnit.addImportedType(new FullyQualifiedJavaType("com.github.pagehelper.PageInfo"));

        GeneratedJavaFile service = new GeneratedJavaFile(serviceImplCompilationUnit, targetProject, "UTF-8", getContext().getJavaFormatter());

        // mybatis会根据List<GeneratedJavaFile> super_result中的GeneratedJavaFile实例来生成对应的Java文件
        super_result.add(service);
    }

    /**
     * 为类添加Field
     *
     * @param serviceImplCompilationUnit
     * @param introspectedTable
     */
    private void setServiceImplField(TopLevelClass serviceImplCompilationUnit, IntrospectedTable introspectedTable) {
        // 添加日志类
        /*Field loggerField = new Field();
        loggerField.setName("logger");
        loggerField.setVisibility(JavaVisibility.PRIVATE);
        loggerField.setFinal(true);
        loggerField.setType(new FullyQualifiedJavaType("Logger"));
        loggerField.setInitializationString("LoggerFactory.getLogger(this.getClass())");
        serviceImplCompilationUnit.addField(loggerField);
        serviceImplCompilationUnit.addImportedType(new FullyQualifiedJavaType("org.slf4j.Logger"));
        serviceImplCompilationUnit.addImportedType(new FullyQualifiedJavaType("org.slf4j.LoggerFactory"));*/

        // 实体类的类名
        String domainObjectName = introspectedTable.getTableConfiguration().getDomainObjectName();
        // DAO类所在包的包名
        String daoTargetPackage = introspectedTable.getContext().getJavaClientGeneratorConfiguration().getTargetPackage();
        Field daoField = new Field();
        // 设置Field的注解
        daoField.addAnnotation("@Autowired");
        daoField.setVisibility(JavaVisibility.PRIVATE);
        // 设置Field的类型
        daoField.setType(new FullyQualifiedJavaType(domainObjectName + "Mapper"));
        // 设置Field的名称
        daoField.setName(toLowerCaseFirstOne(domainObjectName) + "Mapper");
        // 将Field添加到对应的类中
        serviceImplCompilationUnit.addField(daoField);
        // 对应的类需要import DAO类(使用全限定类名)
        serviceImplCompilationUnit.addImportedType(new FullyQualifiedJavaType(daoTargetPackage + "." + domainObjectName + "Mapper"));
    }

    /**
     * 为类添加方法
     *
     * @param topLevelClass
     * @param introspectedTable
     */
    private void setServiceImplMethod(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        // 创建deleteByPrimaryKey方法,对应DAO中的deleteByPrimaryKey方法
        // 参考DeleteByPrimaryKeyMethodGenerator.addInterfaceElements()方法
        serviceImplSetAddOrUpdate(topLevelClass, introspectedTable);
        serviceImplAddDelete(topLevelClass, introspectedTable);
        serviceImplSetGet(topLevelClass, introspectedTable);
        serviceImplSetList2(topLevelClass, introspectedTable);
        serviceImplSetList(topLevelClass, introspectedTable);
        serviceImplSetListOfPaging(topLevelClass, introspectedTable);
        serviceImplSetBatchInsert(topLevelClass, introspectedTable);
//        addDeleteByPrimarykeyMethod4Impl(topLevelClass, introspectedTable);

        // 创建insert方法,对应DAO中的insert方法
        // 参考InsertMethodGenerator.addInterfaceElements()方法
        /*addInsertMethod4Impl(topLevelClass, introspectedTable);

        // 创建insertSelective方法,对应DAO中的insertSelective方法
        // 参考InsertSelectiveMethodGenerator.addInterfaceElements()方法
        addInsertSelectiveMethod4Impl(topLevelClass, introspectedTable);

        // 创建selectByPrimaryKey方法,对应DAO中的selectByPrimaryKey方法
        // 参考SelectByPrimaryKeyMethodGenerator.addInterfaceElements()方法
        addSelectByPrimaryKeyMethod4Impl(topLevelClass, introspectedTable);

        // 创建updateByPrimaryKeySelective方法,对应DAO中的updateByPrimaryKeySelective方法
        // 参考UpdateByPrimaryKeySelectiveMethodGenerator.addInterfaceElements()方法
        addUpdateByPrimaryKeySelectiveMethod4Impl(topLevelClass, introspectedTable);

        // 创建updateByPrimaryKey方法,对应DAO中的updateByPrimaryKey方法
        // 参考UpdateByPrimaryKeyMethodGenerator.addInterfaceElements()方法
        addUpdateByPrimaryKeyMethod4Impl(topLevelClass, introspectedTable);
*/
        // 创建selectByPage方法,对应DAO中的selectByPage方法,与 https://github.com/linweiyu21/PaginationPlugin 插件配合时使用
        //addSelectByPageMethod4Impl(topLevelClass, introspectedTable);
    }

    private void addSelectByPageMethod4Impl(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        String domainObjectName = introspectedTable.getTableConfiguration().getDomainObjectName();

        Set<FullyQualifiedJavaType> importedTypes = new TreeSet<FullyQualifiedJavaType>();

        Method method = new Method();
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setReturnType(new FullyQualifiedJavaType("List<" + domainObjectName + ">"));
        method.setName("selectByPage");
        method.addParameter(new Parameter(new FullyQualifiedJavaType("Long"), "offset")); //$NON-NLS-1$
        method.addParameter(new Parameter(new FullyQualifiedJavaType("Long"), "limit")); //$NON-NLS-1$

        // addBodyline,必须配置bodyline,方法才有实现体,否则这个方法就是个abstract方法了
        method.addBodyLine("return this." + toLowerCaseFirstOne(domainObjectName) + "Mapper.selectByPage(offset,limit);");

        if (context.getPlugins()
                .clientUpdateByPrimaryKeySelectiveMethodGenerated(method,
                        topLevelClass, introspectedTable)) {
            topLevelClass.addImportedTypes(importedTypes);
            topLevelClass.addMethod(method);
        }
    }

    private void addUpdateByPrimaryKeyMethod4Impl(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        String domainObjectName = introspectedTable.getTableConfiguration().getDomainObjectName();

        Set<FullyQualifiedJavaType> importedTypes = new TreeSet<FullyQualifiedJavaType>();
        FullyQualifiedJavaType parameterType;

        if (introspectedTable.getRules().generateRecordWithBLOBsClass()) {
            parameterType = new FullyQualifiedJavaType(introspectedTable
                    .getRecordWithBLOBsType());
        } else {
            parameterType = new FullyQualifiedJavaType(introspectedTable
                    .getBaseRecordType());
        }

        importedTypes.add(parameterType);

        Method method = new Method();
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setReturnType(FullyQualifiedJavaType.getIntInstance());
        method.setName(introspectedTable
                .getUpdateByPrimaryKeyStatementId());
        method.addParameter(new Parameter(parameterType, "record")); //$NON-NLS-1$

        // addBodyline,必须配置bodyline,方法才有实现体,否则这个方法就是个abstract方法了
        method.addBodyLine("return this." + toLowerCaseFirstOne(domainObjectName) + "Mapper." + introspectedTable.getUpdateByPrimaryKeyStatementId() + "(record);");

        if (context.getPlugins()
                .clientUpdateByPrimaryKeySelectiveMethodGenerated(method,
                        topLevelClass, introspectedTable)) {
            topLevelClass.addImportedTypes(importedTypes);
            topLevelClass.addMethod(method);
        }
    }

    private void addUpdateByPrimaryKeySelectiveMethod4Impl(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        String domainObjectName = introspectedTable.getTableConfiguration().getDomainObjectName();

        Set<FullyQualifiedJavaType> importedTypes = new TreeSet<FullyQualifiedJavaType>();
        FullyQualifiedJavaType parameterType;

        if (introspectedTable.getRules().generateRecordWithBLOBsClass()) {
            parameterType = new FullyQualifiedJavaType(introspectedTable
                    .getRecordWithBLOBsType());
        } else {
            parameterType = new FullyQualifiedJavaType(introspectedTable
                    .getBaseRecordType());
        }

        importedTypes.add(parameterType);

        Method method = new Method();
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setReturnType(FullyQualifiedJavaType.getIntInstance());
        method.setName(introspectedTable
                .getUpdateByPrimaryKeySelectiveStatementId());
        method.addParameter(new Parameter(parameterType, "record")); //$NON-NLS-1$

        // addBodyline,必须配置bodyline,方法才有实现体,否则这个方法就是个abstract方法了
        method.addBodyLine("return this." + toLowerCaseFirstOne(domainObjectName) + "Mapper." + introspectedTable.getUpdateByPrimaryKeySelectiveStatementId() + "(record);");

        if (context.getPlugins()
                .clientUpdateByPrimaryKeySelectiveMethodGenerated(method,
                        topLevelClass, introspectedTable)) {
            topLevelClass.addImportedTypes(importedTypes);
            topLevelClass.addMethod(method);
        }
    }

    private void addSelectByPrimaryKeyMethod4Impl(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        String domainObjectName = introspectedTable.getTableConfiguration().getDomainObjectName();

        Set<FullyQualifiedJavaType> importedTypes = new TreeSet<FullyQualifiedJavaType>();
        Method method = new Method();
        method.setVisibility(JavaVisibility.PUBLIC);

        FullyQualifiedJavaType returnType = introspectedTable.getRules()
                .calculateAllFieldsClass();
        method.setReturnType(returnType);
        importedTypes.add(returnType);

        method.setName(introspectedTable.getSelectByPrimaryKeyStatementId());

        List<IntrospectedColumn> introspectedColumns = introspectedTable
                .getPrimaryKeyColumns();

        for (IntrospectedColumn introspectedColumn : introspectedColumns) {
            FullyQualifiedJavaType type = introspectedColumn
                    .getFullyQualifiedJavaType();
            importedTypes.add(type);
            Parameter parameter = new Parameter(type, introspectedColumn
                    .getJavaProperty());
            method.addParameter(parameter);
        }

        // addBodyline,必须配置bodyline,方法才有实现体,否则这个方法就是个abstract方法了
        List<Parameter> parameters = method.getParameters();
        StringBuilder sb = new StringBuilder();
        for (Parameter parameter : parameters) {
            sb.append(parameter.getName());
            sb.append(",");
        }
        sb.delete(sb.lastIndexOf(","), sb.length());
        method.addBodyLine("return this." + toLowerCaseFirstOne(domainObjectName) + "Mapper." + introspectedTable.getSelectByPrimaryKeyStatementId() + "(" + sb.toString() + ");");

        if (context.getPlugins().clientSelectByPrimaryKeyMethodGenerated(
                method, topLevelClass, introspectedTable)) {
            topLevelClass.addImportedTypes(importedTypes);
            topLevelClass.addMethod(method);
        }
    }

    private void addInsertSelectiveMethod4Impl(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        String domainObjectName = introspectedTable.getTableConfiguration().getDomainObjectName();

        Set<FullyQualifiedJavaType> importedTypes = new TreeSet<FullyQualifiedJavaType>();
        Method method = new Method();

        method.setReturnType(FullyQualifiedJavaType.getIntInstance());
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setName(introspectedTable.getInsertSelectiveStatementId());

        FullyQualifiedJavaType parameterType = introspectedTable.getRules()
                .calculateAllFieldsClass();

        importedTypes.add(parameterType);
        method.addParameter(new Parameter(parameterType, "record")); //$NON-NLS-1$

        // addBodyline,必须配置bodyline,方法才有实现体,否则这个方法就是个abstract方法了
        method.addBodyLine("return this." + toLowerCaseFirstOne(domainObjectName) + "Mapper." + introspectedTable.getInsertSelectiveStatementId() + "(record);");

        if (context.getPlugins().clientInsertSelectiveMethodGenerated(
                method, topLevelClass, introspectedTable)) {
            topLevelClass.addImportedTypes(importedTypes);
            topLevelClass.addMethod(method);
        }
    }

    private void addInsertMethod4Impl(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        String domainObjectName = introspectedTable.getTableConfiguration().getDomainObjectName();

        Set<FullyQualifiedJavaType> importedTypes = new TreeSet<FullyQualifiedJavaType>();
        Method method = new Method();

        method.setReturnType(FullyQualifiedJavaType.getIntInstance());
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setName(introspectedTable.getInsertStatementId());

        FullyQualifiedJavaType parameterType;
        parameterType = new FullyQualifiedJavaType(
                introspectedTable.getBaseRecordType());

        importedTypes.add(parameterType);
        method.addParameter(new Parameter(parameterType, "record")); //$NON-NLS-1$

        // addBodyline,必须配置bodyline,方法才有实现体,否则这个方法就是个abstract方法了
        method.addBodyLine("return this." + toLowerCaseFirstOne(domainObjectName) + "Mapper." + introspectedTable.getInsertStatementId() + "(record);");

        if (context.getPlugins().clientInsertMethodGenerated(method, topLevelClass,
                introspectedTable)) {
            topLevelClass.addImportedTypes(importedTypes);
            topLevelClass.addMethod(method);
        }
    }

    public void addDeleteByPrimarykeyMethod4Impl(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        String domainObjectName = introspectedTable.getTableConfiguration().getDomainObjectName();

        Set<FullyQualifiedJavaType> importedTypes = new TreeSet<FullyQualifiedJavaType>();
        Method method = new Method();
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setReturnType(FullyQualifiedJavaType.getIntInstance());
        method.setName(introspectedTable.getDeleteByPrimaryKeyStatementId());

        List<IntrospectedColumn> introspectedColumns = introspectedTable
                .getPrimaryKeyColumns();

        for (IntrospectedColumn introspectedColumn : introspectedColumns) {
            FullyQualifiedJavaType type = introspectedColumn
                    .getFullyQualifiedJavaType();
            importedTypes.add(type);
            Parameter parameter = new Parameter(type, introspectedColumn
                    .getJavaProperty());
            method.addParameter(parameter);
        }

        // addBodyline,必须配置bodyline,方法才有实现体,否则这个方法就是个abstract方法了
        List<Parameter> parameters = method.getParameters();
        StringBuilder sb = new StringBuilder();
        for (Parameter parameter : parameters) {
            sb.append(parameter.getName());
            sb.append(",");
        }
        sb.delete(sb.lastIndexOf(","), sb.length());
        method.addBodyLine("return this." + toLowerCaseFirstOne(domainObjectName) + "Mapper." + introspectedTable.getDeleteByPrimaryKeyStatementId() + "(" + sb.toString() + ");");

        if (context.getPlugins().clientDeleteByPrimaryKeyMethodGenerated(
                method, topLevelClass, introspectedTable)) {
            topLevelClass.addImportedTypes(importedTypes);
            topLevelClass.addMethod(method);
        }
    }

    /**
     * serviceImpl delete 方法
     *
     * @param topLevelClass
     * @param introspectedTable
     */
    public void serviceImplAddDelete(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        String domainObjectName = introspectedTable.getTableConfiguration().getDomainObjectName();

        Set<FullyQualifiedJavaType> importedTypes = new TreeSet<FullyQualifiedJavaType>();
        Method method = new Method();
        method.addAnnotation("@Override");
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setReturnType(FullyQualifiedJavaType.getBooleanPrimitiveInstance());
        method.setName(BaseServiceMethod.delete);

        List<IntrospectedColumn> introspectedColumns = introspectedTable
                .getPrimaryKeyColumns();
        List<String> methodJavaDocParam = new ArrayList<>();
        for (IntrospectedColumn introspectedColumn : introspectedColumns) {
            FullyQualifiedJavaType type = introspectedColumn
                    .getFullyQualifiedJavaType();
            importedTypes.add(type);
            String paramName = introspectedColumn.getJavaProperty();
            methodJavaDocParam.add(paramName);
            Parameter parameter = new Parameter(type, paramName);
            method.addParameter(parameter);
        }
        method.addJavaDocLine("/**");
        method.addJavaDocLine("* " + method.getName());
        methodJavaDocParam.forEach(paramName -> {
            method.addJavaDocLine("* @param " + paramName);
        });
        method.addJavaDocLine("* @return");
        method.addJavaDocLine("*/");
        // addBodyline,必须配置bodyline,方法才有实现体,否则这个方法就是个abstract方法了
        List<Parameter> parameters = method.getParameters();
        StringBuilder sb = new StringBuilder();
        for (Parameter parameter : parameters) {
            sb.append(parameter.getName());
            sb.append(",");
        }
        sb.delete(sb.lastIndexOf(","), sb.length());
        method.addBodyLine("int c = 0;");
        method.addBodyLine("c = " + toLowerCaseFirstOne(domainObjectName) + "Mapper." + introspectedTable.getDeleteByPrimaryKeyStatementId() + "(" + sb.toString() + ");");
        method.addBodyLine("return c == 1 ? true : false;");

        if (context.getPlugins().clientDeleteByPrimaryKeyMethodGenerated(
                method, topLevelClass, introspectedTable)) {
            topLevelClass.addImportedTypes(importedTypes);
            topLevelClass.addMethod(method);
        }
    }

    /**
     * serviceImpl get 方法
     *
     * @param topLevelClass
     * @param introspectedTable
     */
    public void serviceImplSetGet(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        String domainObjectName = introspectedTable.getTableConfiguration().getDomainObjectName();

        Set<FullyQualifiedJavaType> importedTypes = new TreeSet<FullyQualifiedJavaType>();
        Method method = new Method();
        method.addAnnotation("@Override");
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setReturnType(new FullyQualifiedJavaType(domainObjectName));
        method.setName(BaseServiceMethod.get);

        List<IntrospectedColumn> introspectedColumns = introspectedTable.getPrimaryKeyColumns();
        List<ParameterTypeName> parameterList = new ArrayList<>();
        introspectedColumns.forEach(introspectedColumn ->{
            parameterList.add(new ParameterTypeName("",introspectedColumn.getJavaProperty(),""));
        });
        MethodUtil.addParameterOfPrimaryKey(introspectedTable, importedTypes, method);
        MethodUtil.addJavaDocLine(method, parameterList);
//        method.addJavaDocLine("/**");
//        method.addJavaDocLine("* " + method.getName());
//        parameterList.forEach(parameterTypeName -> {
//            method.addJavaDocLine("* @param " + parameterTypeName.getName() + " " + parameterTypeName.getRemark());
//        });
//        method.addJavaDocLine("* @return");
//        method.addJavaDocLine("*/");

        // addBodyline,必须配置bodyline,方法才有实现体,否则这个方法就是个abstract方法了
        List<Parameter> parameters = method.getParameters();
        StringBuilder sb = new StringBuilder();
        for (Parameter parameter : parameters) {
            sb.append(parameter.getName());
            sb.append(",");
        }
        sb.delete(sb.lastIndexOf(","), sb.length());

        String returnName = toLowerCaseFirstOne(domainObjectName);
        method.addBodyLine(domainObjectName + " " + returnName + ";");
        method.addBodyLine(returnName + " = " + toLowerCaseFirstOne(domainObjectName) + "Mapper." + introspectedTable.getSelectByPrimaryKeyStatementId() + "(" + sb.toString() + ");");
        method.addBodyLine("return " + returnName + ";");

        if (context.getPlugins().clientSelectByPrimaryKeyMethodGenerated(
                method, topLevelClass, introspectedTable)) {
            topLevelClass.addImportedTypes(importedTypes);
            topLevelClass.addMethod(method);
        }
    }

    /**
     * serviceImpl addOrUpdate 方法
     *
     * @param topLevelClass
     * @param introspectedTable
     */
    public void serviceImplSetAddOrUpdate(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        String domainObjectName = introspectedTable.getTableConfiguration().getDomainObjectName();

        Set<FullyQualifiedJavaType> importedTypes = new TreeSet<FullyQualifiedJavaType>();
        Method method = new Method();
        method.addAnnotation("@Override");
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setReturnType(FullyQualifiedJavaType.getBooleanPrimitiveInstance());
        method.setName(BaseServiceMethod.addOrUpdate);

        FullyQualifiedJavaType parameterType = new FullyQualifiedJavaType(domainObjectName);
        importedTypes.add(parameterType);
        String pName = "entity";
        method.addParameter(new Parameter(parameterType, pName));
        List<ParameterTypeName> parameterList = new ArrayList<>();
        parameterList.add(new ParameterTypeName("",pName,""));
        MethodUtil.addJavaDocLine(method, parameterList);

        // addBodyline,必须配置bodyline,方法才有实现体,否则这个方法就是个abstract方法了
        List<Parameter> parameters = method.getParameters();
        StringBuilder sb = new StringBuilder();
        for (Parameter parameter : parameters) {
            sb.append(parameter.getName());
            sb.append(",");
        }
        sb.delete(sb.lastIndexOf(","), sb.length());

        // 增加方法体
        List<String> bodyLines = new ArrayList<>();
        bodyLines.add("int c = 0");
        bodyLines.add("entity.setGmtModified(new Date())");
        bodyLines.add("if (entity.getId() != null) {");
        bodyLines.add("c = " + toLowerCaseFirstOne(domainObjectName) + "Mapper." + introspectedTable.getUpdateByPrimaryKeySelectiveStatementId() + "(" + sb.toString() + ")");
        bodyLines.add("} else {");
        bodyLines.add("entity.setGmtCreate(new Date());");
        bodyLines.add("c = " + toLowerCaseFirstOne(domainObjectName) + "Mapper." + introspectedTable.getInsertSelectiveStatementId() + "(" + sb.toString() + ")");
        bodyLines.add("}");
        bodyLines.add("return c == 1 ? true : false");
        MethodUtil.addBodyLines(method, bodyLines);

        if (context.getPlugins().clientDeleteByPrimaryKeyMethodGenerated(
                method, topLevelClass, introspectedTable)) {
            topLevelClass.addImportedTypes(importedTypes);
            topLevelClass.addMethod(method);
        }
    }

    /**
     * serviceImpl addOrUpdate 方法
     *
     * @param topLevelClass
     * @param introspectedTable
     */
    public void serviceImplSetBatchInsert(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        String domainObjectName = introspectedTable.getTableConfiguration().getDomainObjectName();

        Set<FullyQualifiedJavaType> importedTypes = new TreeSet<FullyQualifiedJavaType>();
        Method method = new Method();
        method.addAnnotation("@Override");
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setReturnType(FullyQualifiedJavaType.getIntInstance());
        method.setName(BaseServiceMethod.batchInsert);

        FullyQualifiedJavaType parameterType = new FullyQualifiedJavaType(domainObjectName);
        importedTypes.add(parameterType);
        String pName = "entitys";
        FullyQualifiedJavaType listInstance = FullyQualifiedJavaType.getNewListInstance();
        listInstance.addTypeArgument( new FullyQualifiedJavaType(introspectedTable.getBaseRecordType()));
        method.addParameter(new Parameter(listInstance, pName));

        List<ParameterTypeName> parameterList = new ArrayList<>();
        parameterList.add(new ParameterTypeName("",pName,""));
        MethodUtil.addJavaDocLine(method, parameterList);

        // addBodyline,必须配置bodyline,方法才有实现体,否则这个方法就是个abstract方法了
        List<Parameter> parameters = method.getParameters();
        StringBuilder sb = new StringBuilder();
        for (Parameter parameter : parameters) {
            sb.append(parameter.getName());
            sb.append(",");
        }
        sb.delete(sb.lastIndexOf(","), sb.length());

        // 增加方法体
        List<String> bodyLines = new ArrayList<>();
        bodyLines.add("if (entitys == null || entitys.size() == 0) {");
        bodyLines.add("return 0");
        bodyLines.add("}");
        bodyLines.add("int c = " + toLowerCaseFirstOne(domainObjectName) + "Mapper.batchInsert(" + sb.toString() + ")");
        bodyLines.add("return c");
        MethodUtil.addBodyLines(method, bodyLines);

        if (context.getPlugins().clientDeleteByPrimaryKeyMethodGenerated(
                method, topLevelClass, introspectedTable)) {
            topLevelClass.addImportedTypes(importedTypes);
            topLevelClass.addMethod(method);
        }
    }

    /**
     * serviceImpl list 方法
     *
     * @param topLevelClass
     * @param introspectedTable
     */
    public void serviceImplSetList2(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        String domainObjectName = introspectedTable.getTableConfiguration().getDomainObjectName();

        Set<FullyQualifiedJavaType> importedTypes = new TreeSet<FullyQualifiedJavaType>();
        Method method = new Method();
        method.addAnnotation("@Override");
        method.setVisibility(JavaVisibility.PUBLIC);
        String returnType = "List<" + domainObjectName + ">";
        method.setReturnType(new FullyQualifiedJavaType(returnType));
        method.setName(BaseServiceMethod.list);
        List<String> methodJavaDocParam = new ArrayList<>();

        FullyQualifiedJavaType parameterType = new FullyQualifiedJavaType(domainObjectName);
        importedTypes.add(parameterType);
        String pName = "query";
        method.addParameter(new Parameter(parameterType, pName));
        methodJavaDocParam.add(pName);
        method.addJavaDocLine("/**");
        method.addJavaDocLine("* " + method.getName());
        methodJavaDocParam.forEach(paramName -> {
            method.addJavaDocLine("* @param " + paramName);
        });
        method.addJavaDocLine("* @return");
        method.addJavaDocLine("*/");
        // addBodyline,必须配置bodyline,方法才有实现体,否则这个方法就是个abstract方法了
        List<Parameter> parameters = method.getParameters();
        StringBuilder sb = new StringBuilder();
        for (Parameter parameter : parameters) {
            sb.append(parameter.getName());
            sb.append(",");
        }
        sb.delete(sb.lastIndexOf(","), sb.length());
        String returnName = "list";

        // 增加方法体
        List<String> bodyLines = new ArrayList<>();
        bodyLines.add(returnType + " " + returnName);
        bodyLines.add(returnName + " = " + toLowerCaseFirstOne(domainObjectName) + "Mapper.selectBySelective" + "(" + sb.toString() + ")");
        bodyLines.add("return " + returnName);
        MethodUtil.addBodyLines(method, bodyLines);

        if (context.getPlugins().clientDeleteByPrimaryKeyMethodGenerated(
                method, topLevelClass, introspectedTable)) {
            topLevelClass.addImportedTypes(importedTypes);
            topLevelClass.addMethod(method);
        }
    }

    /**
     * serviceImpl list 方法：分页
     *
     * @param topLevelClass
     * @param introspectedTable
     */
    public void serviceImplSetList(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        String domainObjectName = introspectedTable.getTableConfiguration().getDomainObjectName();

        Set<FullyQualifiedJavaType> importedTypes = new TreeSet<FullyQualifiedJavaType>();
        Method method = new Method();
        method.addAnnotation("@Override");
        method.setVisibility(JavaVisibility.PUBLIC);
        String returnType = "List<" + domainObjectName + ">";
        method.setReturnType(new FullyQualifiedJavaType(returnType));
        method.setName(BaseServiceMethod.list);
//        List<String> methodJavaDocParam = new ArrayList<>();

        FullyQualifiedJavaType parameterType = new FullyQualifiedJavaType(domainObjectName);
        importedTypes.add(parameterType);
        List<ParameterTypeName> parameterList = new ArrayList<>();
        parameterList.add(new ParameterTypeName("int", "pageNum", "分页：当前页"));
        parameterList.add(new ParameterTypeName("int", "pageSize", "分页：每页大小"));
        parameterList.add(new ParameterTypeName("String", "sidx", "排序字段"));
        parameterList.add(new ParameterTypeName("String", "sord", "desc / asc"));
        String pName = "query";
        parameterList.add(new ParameterTypeName(domainObjectName, pName, "查询类"));
        parameterList.forEach(parameterTypeName -> {
            method.addParameter(new Parameter(new FullyQualifiedJavaType(parameterTypeName.getType()), parameterTypeName.getName()));
        });

        method.addJavaDocLine("/**");
        method.addJavaDocLine("* " + method.getName());
        parameterList.forEach(parameterTypeName -> {
            method.addJavaDocLine("* @param " + parameterTypeName.getName() + " " + parameterTypeName.getRemark());
        });
        method.addJavaDocLine("* @return 查询列表");
        method.addJavaDocLine("*/");

        // 增加方法体
        String returnName = "list";
        List<String> bodyLines = new ArrayList<>();
        bodyLines.add(returnType + " " + returnName + ";");
        bodyLines.add("PageHelper.startPage(pageNum, pageSize, sidx + \" \" + sord);");
        bodyLines.add(returnName + " = " + toLowerCaseFirstOne(domainObjectName) + "Mapper.selectBySelective" + "(" + pName + ");");
        bodyLines.add("return " + returnName + ";");
        MethodUtil.addBodyLines(method, bodyLines);

        if (context.getPlugins().clientDeleteByPrimaryKeyMethodGenerated(
                method, topLevelClass, introspectedTable)) {
            topLevelClass.addImportedTypes(importedTypes);
            topLevelClass.addMethod(method);
        }
    }

    /**
     * serviceImpl list 方法：分页，返回分页信息
     *
     * @param topLevelClass
     * @param introspectedTable
     */
    public void serviceImplSetListOfPaging(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        String domainObjectName = introspectedTable.getTableConfiguration().getDomainObjectName();

        Set<FullyQualifiedJavaType> importedTypes = new TreeSet<FullyQualifiedJavaType>();
        Method method = new Method();
        method.addAnnotation("@Override");
        method.setVisibility(JavaVisibility.PUBLIC);
        String returnType = "PageInfo";
        method.setReturnType(new FullyQualifiedJavaType(returnType));
        method.setName(BaseServiceMethod.listOfPaging);
//        List<String> methodJavaDocParam = new ArrayList<>();

        FullyQualifiedJavaType parameterType = new FullyQualifiedJavaType(domainObjectName);
        importedTypes.add(parameterType);
        List<ParameterTypeName> parameterList = new ArrayList<>();
        parameterList.add(new ParameterTypeName("int", "pageNum", "分页：当前页"));
        parameterList.add(new ParameterTypeName("int", "pageSize", "分页：每页大小"));
        parameterList.add(new ParameterTypeName("String", "sidx", "排序字段"));
        parameterList.add(new ParameterTypeName("String", "sord", "desc / asc"));
        String pName = "query";
        parameterList.add(new ParameterTypeName(domainObjectName, pName, "查询类"));
        parameterList.forEach(parameterTypeName -> {
            method.addParameter(new Parameter(new FullyQualifiedJavaType(parameterTypeName.getType()), parameterTypeName.getName()));
        });

        method.addJavaDocLine("/**");
        method.addJavaDocLine("* " + method.getName());
        parameterList.forEach(parameterTypeName -> {
            method.addJavaDocLine("* @param " + parameterTypeName.getName() + " " + parameterTypeName.getRemark());
        });

        method.addJavaDocLine("* @return 携带分页信息 pageInfo");
        method.addJavaDocLine("*/");
        // addBodyline,必须配置bodyline,方法才有实现体,否则这个方法就是个abstract方法了
        List<Parameter> parameters = method.getParameters();
        StringBuilder sb = new StringBuilder(pName);

        // 增加方法体
        List<String> bodyLines = new ArrayList<>();
        bodyLines.add("PageHelper.startPage(pageNum, pageSize, sidx + \" \" + sord)");
        bodyLines.add("List<" + domainObjectName + "> list = " + toLowerCaseFirstOne(domainObjectName) + "Mapper.selectBySelective" + "(" + sb.toString() + ")");
        bodyLines.add("return PageInfo.of(list)");
        MethodUtil.addBodyLines(method, bodyLines);

        // 将方法添加到class中
        if (context.getPlugins().clientDeleteByPrimaryKeyMethodGenerated(
                method, topLevelClass, introspectedTable)) {
            topLevelClass.addImportedTypes(importedTypes);
            topLevelClass.addMethod(method);
        }
    }

    /**
     * 首字母转小写
     * @param s
     * @return
     */
    private static String toLowerCaseFirstOne(String s) {
        if (Character.isLowerCase(s.charAt(0))) {
            return s;
        } else {
            return (new StringBuilder()).append(Character.toLowerCase(s.charAt(0))).append(s.substring(1)).toString();
        }
    }

    private static class BaseServiceMethod {
        public static String get = "get";
        public static String addOrUpdate = "addOrUpdate";
        public static String delete = "delete";
        public static String list = "list";
        public static String listOfPaging = "listOfPaging";
        public static String batchInsert = "batchInsert";
    }

    /**
     * 方法参数类: 参数类型，参数名称，参数注释
     */
    class ParameterTypeName {
        private String type;
        private String name;
        private String remark;

        private ParameterTypeName() {
        }

        public ParameterTypeName(String type, String name, String remark) {
            this.type = type;
            this.name = name;
            this.remark = remark;
        }

        public String getType() {
            return type;
        }

        public String getName() {
            return name;
        }

        public String getRemark() {
            return remark;
        }
    }

    static class MethodUtil{
        /**
         * 添加方法体
         * @param method
         * @param line
         */
        public static void addBodyLine(Method method, String line){
            method.addBodyLine(line);
        }

        /**
         * 批量添加方法体
         * @param method
         * @param lines
         */
        public static void addBodyLines(Method method, List<String> lines){
            lines.forEach(line -> {
                String suffix = ";";
                String lastStr = line.substring(line.length()-1);
                if("{".equalsIgnoreCase(lastStr) || "}".equalsIgnoreCase(lastStr) || ";".equalsIgnoreCase(lastStr)){
                    suffix = "";
                }
                addBodyLine(method, line + suffix);
            });
        }

        /**
         * 为方法添加主键id
         * @param introspectedTable
         * @param importedTypes
         * @param method
         */
        public static void addParameterOfPrimaryKey(IntrospectedTable introspectedTable, Set<FullyQualifiedJavaType> importedTypes, Method method){
            List<IntrospectedColumn> introspectedColumns = introspectedTable
                    .getPrimaryKeyColumns();
            for (IntrospectedColumn introspectedColumn : introspectedColumns) {
                FullyQualifiedJavaType type = introspectedColumn
                        .getFullyQualifiedJavaType();
                importedTypes.add(type);
                String paramName = introspectedColumn.getJavaProperty();
//                parameterList.add(new ParameterTypeName("","",""));
                Parameter parameter = new Parameter(type, paramName);
                method.addParameter(parameter);
            }
        }

        /**
         * 为方法添加注释javadoc
         * @param method
         * @param parameterList
         */
        public static void addJavaDocLine(Method method, List<ParameterTypeName> parameterList){
            method.addJavaDocLine("/**");
            method.addJavaDocLine("* " + method.getName());
            parameterList.forEach(parameterTypeName -> {
                method.addJavaDocLine("* @param " + parameterTypeName.getName() + " " + parameterTypeName.getRemark());
            });
            if(method.getReturnType() != null){
                method.addJavaDocLine("* @return");
            }
            method.addJavaDocLine("*/");
        }
    }
    }

