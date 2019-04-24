package com.gao.plugins;

import java.util.Iterator;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.codegen.mybatis3.MyBatis3FormattingUtilities;
import org.mybatis.generator.codegen.mybatis3.xmlmapper.elements.AbstractXmlElementGenerator;

/**
 * 生成自定义方法xml：selectBySelective
 * @author skyjilygao
 * @since 20181106
 */
public class SelectBySelectiveMethodGeneratorXmlMapper extends AbstractXmlElementGenerator {
    private String methodName;

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }
    public SelectBySelectiveMethodGeneratorXmlMapper() {
    }

    @Override
    public void addElements(XmlElement parentElement) {
        XmlElement answer = new XmlElement("select");
        answer.addAttribute(new Attribute("id", methodName));
        answer.addAttribute(new Attribute("resultMap", "BaseResultMap"));
        answer.addAttribute(new Attribute("parameterType", introspectedTable.getBaseRecordType()));
        /*answer.addAttribute(new Attribute("useGeneratedKeys", "true"));
        answer.addAttribute(new Attribute("keyProperty", "id"));*/
        this.context.getCommentGenerator().addComment(answer);
        StringBuilder sb = new StringBuilder();
        sb.append("select ");
        answer.addElement(new TextElement(sb.toString()));
        answer.addElement(this.getBaseColumnListElement());
        sb.setLength(0);
        sb.append("from ").append(introspectedTable.getFullyQualifiedTableNameAtRuntime());
        answer.addElement(new TextElement(sb.toString()));

        XmlElement dynamicElement = new XmlElement("where");
        answer.addElement(dynamicElement);
        Iterator i$ = this.introspectedTable.getAllColumns().iterator();

        while(i$.hasNext()) {
            IntrospectedColumn introspectedColumn = (IntrospectedColumn)i$.next();
            String col = introspectedColumn.getJavaProperty();
            if ("gmtCreate".equalsIgnoreCase(col) || "gmtModified".equalsIgnoreCase(col)) {
                System.out.println("忽略" + col);
                continue;
            }
            XmlElement isNotNullElement = new XmlElement("if");
            sb.setLength(0);
            sb.append(col);
            sb.append(" != null");
            isNotNullElement.addAttribute(new Attribute("test", sb.toString()));
            dynamicElement.addElement(isNotNullElement);
            sb.setLength(0);
            sb.append("and ");
            sb.append(MyBatis3FormattingUtilities.getAliasedEscapedColumnName(introspectedColumn));
            sb.append(" = ");
            sb.append(MyBatis3FormattingUtilities.getParameterClause(introspectedColumn));
            isNotNullElement.addElement(new TextElement(sb.toString()));
        }

        if (this.context.getPlugins().sqlMapUpdateByExampleSelectiveElementGenerated(answer, this.introspectedTable)) {
            parentElement.addElement(answer);
        }

    }
}
