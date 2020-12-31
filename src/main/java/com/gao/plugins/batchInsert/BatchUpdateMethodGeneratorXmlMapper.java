package com.gao.plugins.batchInsert;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.codegen.mybatis3.MyBatis3FormattingUtilities;
import org.mybatis.generator.codegen.mybatis3.xmlmapper.elements.AbstractXmlElementGenerator;

import java.util.Iterator;

/**
 * 生成自定义方法xml：selectBySelective
 * @author skyjilygao
 * @since 20181106
 */
public class BatchUpdateMethodGeneratorXmlMapper extends AbstractXmlElementGenerator {
    private String methodName;

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }
    public BatchUpdateMethodGeneratorXmlMapper() {
    }

    @Override
    public void addElements(XmlElement parentElement) {
        // 最外层标签
        XmlElement answer = new XmlElement("update");
        answer.addAttribute(new Attribute("id", methodName));
        answer.addAttribute(new Attribute("parameterType", "java.util.List"));

        String tableName = introspectedTable.getFullyQualifiedTableNameAtRuntime();
        this.context.getCommentGenerator().addComment(answer);

        // addElement 顺序就是整个xml sql顺序
        // 1. 追加 事物开始
        answer.addElement(new TextElement("START TRANSACTION;"));
        // 2. 追加 foreach遍历 先不管foreach里的逻辑，先把整个框架搞定
        XmlElement foreachElm = new XmlElement("foreach");
        answer.addElement(foreachElm);
        // 2.1. foreachElm 增加 属性
        appendForeachAttribute(foreachElm);
        // 2.2. foreachElm 填充内容：update xxx
        foreachElm.addElement(new TextElement(String.format("update %s", tableName)));
        // 2.3. foreach标签内的set标签
        XmlElement dynamicElement = new XmlElement("set");
        foreachElm.addElement(dynamicElement);


        // 2.3.1 开始对foreach里面的set标签填充内容

        StringBuilder sb = new StringBuilder();
        Iterator i$ = this.introspectedTable.getAllColumns().iterator();
        while(i$.hasNext()) {
            IntrospectedColumn introspectedColumn = (IntrospectedColumn)i$.next();
            String col = introspectedColumn.getJavaProperty();
            if ("id".equalsIgnoreCase(col) || "gmtCreate".equalsIgnoreCase(col) || "create_time".equalsIgnoreCase(col)) {
                System.out.println("忽略" + col);
                continue;
            }
            // 2.3.2 set标签内的if标签
            XmlElement ifElement = new XmlElement("if");
            sb.setLength(0);
            sb.append("item.").append(col).append(" != null");
            ifElement.addAttribute(new Attribute("test", sb.toString()));
            sb.setLength(0);
            String join = String.join(" ",
                    MyBatis3FormattingUtilities.getAliasedEscapedColumnName(introspectedColumn),
                    "=",
                    MyBatis3FormattingUtilities.getParameterClause(introspectedColumn,"item."),",");
            // 2.3.3 追加if标签内容
            ifElement.addElement(new TextElement(join));

            // 2.3.4 将if标签追加到set标签内
            dynamicElement.addElement(ifElement);
        }

        foreachElm.addElement(new TextElement("where id = #{item.id,jdbcType=BIGINT}"));

        // 3. 事物结束
        answer.addElement(new TextElement("COMMIT;"));

        if (this.context.getPlugins().sqlMapUpdateByExampleSelectiveElementGenerated(answer, this.introspectedTable)) {
            parentElement.addElement(answer);
        }

    }

    /**
     * 给foreach标签增加属性
     * @param foreachElm
     */
    private void appendForeachAttribute(XmlElement foreachElm) {
        foreachElm.addAttribute(new Attribute("collection", "list"));
        foreachElm.addAttribute(new Attribute("item", "item"));
        foreachElm.addAttribute(new Attribute("index", "index"));
        foreachElm.addAttribute(new Attribute("separator", ";"));
        foreachElm.addAttribute(new Attribute("close", ";"));
    }
}
