package com.gao.plugins;

import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.api.dom.xml.Document;
import org.mybatis.generator.codegen.mybatis3.javamapper.elements.AbstractJavaMapperMethodGenerator;
import org.mybatis.generator.codegen.mybatis3.xmlmapper.elements.AbstractXmlElementGenerator;

import java.util.List;

/**
 * 自定义方法
 *
 * @author skyjilygao
 * @since 20181106
 */
public class CustomMethod extends PluginAdapter {
	public static String methodName = "selectBySelective";

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
		SelectBySelectiveMethodGeneratorXmlMapper generator = new SelectBySelectiveMethodGeneratorXmlMapper();
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
		SelectBySelectiveMethodGeneratorJavaMapper generator = new SelectBySelectiveMethodGeneratorJavaMapper();
		generator.setMethodName(methodName);
		generator.setContext(context);
		generator.setIntrospectedTable(introspectedTable);
		generator.addInterfaceElements(interfaze);

		return super.clientGenerated(interfaze, topLevelClass, introspectedTable);
	}

}
