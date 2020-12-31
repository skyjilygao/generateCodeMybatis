package com.gao.plugins.batchInsert;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.codegen.mybatis3.MyBatis3FormattingUtilities;
import org.mybatis.generator.codegen.mybatis3.xmlmapper.elements.AbstractXmlElementGenerator;

import java.util.Iterator;
import java.util.List;

/**
 * 生成自定义方法xml：selectBySelective
 *
 * @author skyjilygao
 * @since 20181106
 */
public class BatchInsertMethodGeneratorXmlMapper extends AbstractXmlElementGenerator {
    private String methodName;

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public BatchInsertMethodGeneratorXmlMapper() {
    }

    @Override
    public void addElements(XmlElement parentElement) {
        XmlElement answer = new XmlElement("insert");
        answer.addAttribute(new Attribute("id", methodName));
//        answer.addAttribute(new Attribute("resultMap", "BaseResultMap"));
//        answer.addAttribute(new Attribute("parameterType", introspectedTable.getBaseRecordType()));
        /*answer.addAttribute(new Attribute("useGeneratedKeys", "true"));
        answer.addAttribute(new Attribute("keyProperty", "id"));*/
//
//        insert into person ( <include refid="Base_Column_List" /> )
//        values
//                <foreach collection="list" item="item" index="index" separator=",">
//                (null,#{item.name},#{item.sex},#{item.address})
//    </foreach>
        String tableName = introspectedTable.getFullyQualifiedTableNameAtRuntime();
        this.context.getCommentGenerator().addComment(answer);
        StringBuilder sb = new StringBuilder();
        sb.append("insert into ").append(tableName).append(" ( ");
        answer.addElement(new TextElement(sb.toString()));

        answer.addElement(this.getBaseColumnListElement());
        answer.addElement(new TextElement(")"));

        // forear开始
        sb.setLength(0);
        sb.append("values");
        answer.addElement(new TextElement(sb.toString()));

        XmlElement foreachElm = new XmlElement("foreach");
        foreachElm.addAttribute(new Attribute("collection", "list"));
        foreachElm.addAttribute(new Attribute("item", "item"));
        foreachElm.addAttribute(new Attribute("index", "index"));
        foreachElm.addAttribute(new Attribute("separator", ","));
        answer.addElement(foreachElm);

        // 值
        sb.setLength(0);
        foreachElm.addElement(new TextElement("("));
        List<IntrospectedColumn> allColumns = this.introspectedTable.getAllColumns();
        int size = allColumns.size();
        int a = 0;
        int t = 0;
        for (int i = 0; i < size; i++) {
            IntrospectedColumn introspectedColumn = allColumns.get(i);
            String col = introspectedColumn.getJavaProperty();
            if ("id".equalsIgnoreCase(col)) {
                System.out.println("忽略" + col);
                // 对应主键id 的值为null
                sb.append("null");
            } else {
                if (sb.length() > 0) {
                    sb.append(",");
                }
                sb.append("#{item.").append(col).append("}");
                if ((i + 1) % 5 == 0 || (i + 1)==size) {
                    if (size > (i + 1)) {
                        sb.append(",");
                    }
                    foreachElm.addElement(new TextElement(sb.toString()));
                    sb.setLength(0);
                }

            }
        }

//        forearchElm.addElement(new TextElement(sb.toString()));
        foreachElm.addElement(new TextElement(")"));
//        answer.addElement(new TextElement(sb.toString()));
        // foreach结束
/*


        XmlElement dynamicElement = new XmlElement("where");
        answer.addElement(dynamicElement);
*/


        if (this.context.getPlugins().sqlMapUpdateByExampleSelectiveElementGenerated(answer, this.introspectedTable)) {
            parentElement.addElement(answer);
        }

    }
}
