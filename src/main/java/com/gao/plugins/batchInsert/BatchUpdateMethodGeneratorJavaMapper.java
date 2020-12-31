package com.gao.plugins.batchInsert;

import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.codegen.mybatis3.javamapper.elements.AbstractJavaMapperMethodGenerator;

import java.util.Set;
import java.util.TreeSet;

/**
 * 生成自定义方法java：selectBySelective
 * @author skyjilygao
 * @since 20181106
 */
public class BatchUpdateMethodGeneratorJavaMapper extends AbstractJavaMapperMethodGenerator {
    private String methodName;

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public BatchUpdateMethodGeneratorJavaMapper() {
    }

    @Override
    public void addInterfaceElements(Interface interfaze) {
        Set<FullyQualifiedJavaType> importedTypes = new TreeSet();
        Method method = new Method();
        method.setVisibility(JavaVisibility.PUBLIC);
        FullyQualifiedJavaType returnType = FullyQualifiedJavaType.getIntInstance();
        FullyQualifiedJavaType listType;
        listType = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());
//        listType.addTypeArgument(FullyQualifiedJavaType.getNewListInstance());

        FullyQualifiedJavaType newListInstance = FullyQualifiedJavaType.getNewListInstance();
        newListInstance.addTypeArgument( new FullyQualifiedJavaType(introspectedTable.getBaseRecordType()));
        importedTypes.add(listType);
//        returnType.addTypeArgument(listType);
        method.setReturnType(returnType);
        method.setName(methodName);
        FullyQualifiedJavaType parameterType = this.introspectedTable.getRules().calculateAllFieldsClass();
//        parameterType.addTypeArgument(listType);
        String paramName = "list";
//        method.addParameter(new Parameter(parameterType, paramName, "@Param(\""+paramName+"\")"));
        method.addParameter(new Parameter(newListInstance, paramName));
        importedTypes.add(parameterType);
        importedTypes.add(new FullyQualifiedJavaType("org.apache.ibatis.annotations.Param"));
        importedTypes.add(new FullyQualifiedJavaType("org.springframework.stereotype.Repository"));
        importedTypes.add(new FullyQualifiedJavaType("org.apache.ibatis.annotations.Mapper"));
        importedTypes.add(new FullyQualifiedJavaType("java.util.List"));
        this.context.getCommentGenerator().addGeneralMethodComment(method, this.introspectedTable);
        this.addMapperAnnotations(interfaze, method);
        if (this.context.getPlugins().clientUpdateByExampleSelectiveMethodGenerated(method, interfaze, this.introspectedTable)) {
            interfaze.addImportedTypes(importedTypes);
            interfaze.addMethod(method);
        }
    }

    public void addMapperAnnotations(Interface interfaze, Method method) {
//        interfaze.addAnnotation("@Mapper");
//        interfaze.addAnnotation("@Repository");
    }
}
