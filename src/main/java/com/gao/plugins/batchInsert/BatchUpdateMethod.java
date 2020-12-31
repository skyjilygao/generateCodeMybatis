package com.gao.plugins.batchInsert;

import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.api.dom.xml.Document;

import java.util.List;

/**
 * 自定义方法
 *
 * @author skyjilygao
 * @since 20201202
 */
public class BatchUpdateMethod extends PluginAdapter {
	public static String methodName = "batchUpdate";

	@Override
	public boolean validate(List<String> warnings) {
		return true;
	}

	/**
	 * 配置xml，如果有多个自定义，直接累计即可
	 * @param document
	 * @param introspectedTable
	 * @return
	 */
	@Override
	public boolean sqlMapDocumentGenerated(Document document, IntrospectedTable introspectedTable) {
		BatchUpdateMethodGeneratorXmlMapper generator = new BatchUpdateMethodGeneratorXmlMapper();
		generator.setMethodName(methodName);
		generator.setContext(context);
		generator.setIntrospectedTable(introspectedTable);
		generator.addElements(document.getRootElement());
		return super.sqlMapDocumentGenerated(document, introspectedTable);
	}

	/**
	 * 配置java mapper，如果有多个自定义，直接累计即可
	 * @param interfaze
	 * @param topLevelClass
	 * @param introspectedTable
	 * @return
	 */
	@Override
	public boolean clientGenerated(Interface interfaze, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
		BatchUpdateMethodGeneratorJavaMapper generator = new BatchUpdateMethodGeneratorJavaMapper();
		generator.setMethodName(methodName);
		generator.setContext(context);
		generator.setIntrospectedTable(introspectedTable);
		generator.addInterfaceElements(interfaze);

		return super.clientGenerated(interfaze, topLevelClass, introspectedTable);
	}

}
