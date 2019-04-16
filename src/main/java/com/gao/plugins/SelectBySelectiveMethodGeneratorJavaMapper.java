package com.gao.plugins;

import java.util.Set;
import java.util.TreeSet;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.JavaVisibility;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.codegen.mybatis3.javamapper.elements.AbstractJavaMapperMethodGenerator;

/**
 * 生成自定义方法java：selectBySelective
 * @author skyjilygao
 * @since 20181106
 */
public class SelectBySelectiveMethodGeneratorJavaMapper extends AbstractJavaMapperMethodGenerator {
    private String methodName;

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public SelectBySelectiveMethodGeneratorJavaMapper() {
    }

    @Override
    public void addInterfaceElements(Interface interfaze) {
        Set<FullyQualifiedJavaType> importedTypes = new TreeSet();
        Method method = new Method();
        method.setVisibility(JavaVisibility.PUBLIC);
        FullyQualifiedJavaType returnType = FullyQualifiedJavaType.getNewListInstance();
        FullyQualifiedJavaType listType;
        listType = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());
        importedTypes.add(listType);
        returnType.addTypeArgument(listType);
        method.setReturnType(returnType);
        method.setName(methodName);
        FullyQualifiedJavaType parameterType = this.introspectedTable.getRules().calculateAllFieldsClass();
        String paramName = "record";
//        method.addParameter(new Parameter(parameterType, paramName, "@Param(\""+paramName+"\")"));
        method.addParameter(new Parameter(parameterType, paramName));
        importedTypes.add(parameterType);
        importedTypes.add(new FullyQualifiedJavaType("org.apache.ibatis.annotations.Param"));
        importedTypes.add(new FullyQualifiedJavaType("org.springframework.stereotype.Repository"));
        importedTypes.add(new FullyQualifiedJavaType("org.apache.ibatis.annotations.Mapper"));
        importedTypes.add(new FullyQualifiedJavaType("java.util.List;"));
        this.context.getCommentGenerator().addGeneralMethodComment(method, this.introspectedTable);
        this.addMapperAnnotations(interfaze, method);
        if (this.context.getPlugins().clientUpdateByExampleSelectiveMethodGenerated(method, interfaze, this.introspectedTable)) {
            interfaze.addImportedTypes(importedTypes);
            interfaze.addMethod(method);
        }
    }

    public void addMapperAnnotations(Interface interfaze, Method method) {
        interfaze.addAnnotation("@Mapper");
        interfaze.addAnnotation("@Repository");
    }
}
