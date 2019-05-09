package com.gao;
 
 
import static org.mybatis.generator.internal.util.StringUtility.isTrue;


import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.Set;

import org.mybatis.generator.api.CommentGenerator;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.config.MergeConstants;
import org.mybatis.generator.config.PropertyRegistry;
 
/**   
 * @Title: MyCommentGenerator.java 
*/
public class MyCommentGenerator implements CommentGenerator{
 
    /**
      * properties配置文件
     */
    private Properties properties;
    /**
      * properties配置文件
     */
    private Properties systemPro;
    
    /*
     * 父类时间
     */
    private boolean suppressDate;
    
    /**
     * 父类所有注释
     */
    private boolean suppressAllComments;
    
    /**
     * 当前时间
     */
    private String currentDateStr;
    /**
     * 作者
     */
    public static String author;

    public MyCommentGenerator() {
        super();
        properties = new Properties();
        systemPro = System.getProperties();
        suppressDate = false;
        suppressAllComments = false;
        currentDateStr = (new SimpleDateFormat("yyyy-MM-dd")).format(new Date());
        InputStream in;
        try {
            in = GeneratorMain.class.getClassLoader().getResourceAsStream("generator.properties");
            properties.load(in);
            author =properties.getProperty("author");
        }catch (Exception e){

        }
    }
    
    /**
     * Java类的类注释
     */
	@Override
	public void addClassComment(InnerClass innerClass, IntrospectedTable introspectedTable) {
        if (suppressAllComments) {
            return;
        }
 
        StringBuilder sb = new StringBuilder();
        innerClass.addJavaDocLine("/**");
        sb.append(" *");
        sb.append(introspectedTable.getFullyQualifiedTable());
        sb.append(" ");
        sb.append(getDateString());
        innerClass.addJavaDocLine(sb.toString().replace("\n", " "));
        innerClass.addJavaDocLine(" */");
	}
 
	/**
	 * 为类添加注释
	 */
	@Override
	public void addClassComment(InnerClass innerClass, IntrospectedTable introspectedTable, boolean markAsDoNotDelete) {
		if (suppressAllComments) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        innerClass.addJavaDocLine("/**");
        sb.append(" * ");
        sb.append(" * ");
        sb.append(introspectedTable.getFullyQualifiedTable());
        innerClass.addJavaDocLine(sb.toString().replace("\n", " "));
        sb.setLength(0);
        sb.append(" * @author ");
        sb.append(systemPro.getProperty("user.name"));
        sb.append(" ");
        sb.append(currentDateStr);
        innerClass.addJavaDocLine(" */");
	}
 
	
	/**
	 * Mybatis的Mapper.xml文件里面的注释
	 */
	@Override
	public void addComment(XmlElement xmlElement) {
		
	}
 
	/**
	 * 
	 *@Title addConfigurationProperties
	 *@Description: 从该配置中的任何属性添加此实例的属性CommentGenerator配置。
	 *              这个方法将在任何其他方法之前被调用。
	 *@return
	 *@throws
	 */
	@Override
	public void addConfigurationProperties(Properties properties) {
		this.properties.putAll(properties);
        suppressDate = isTrue(properties.getProperty(PropertyRegistry.COMMENT_GENERATOR_SUPPRESS_DATE));
        suppressAllComments = isTrue(properties.getProperty(PropertyRegistry.COMMENT_GENERATOR_SUPPRESS_ALL_COMMENTS));
	}
 
	/**
	 * 
	 *@Title getDateString 
	 *@Description: 此方法返回格式化的日期字符串以包含在Javadoc标记中和XML注释。 如果您不想要日期，则可以返回null在这些文档元素中。
	 *@return
	 *@throws
	 */
    protected String getDateString() {
        String result = null;
        if (!suppressDate) {
            result = currentDateStr;
        }
        return result;
    }
    
    /**
     *
     * 在方法上添加注解
     *@Title addJavadocTag 
     *@Description: 此方法为其添加了自定义javadoc标签。
     *@param javaElement
     *@param markAsDoNotDelete
     *@throws
     */
	protected void addJavadocTag(JavaElement javaElement, boolean markAsDoNotDelete) {
       /* javaElement.addJavaDocLine(" *");
        StringBuilder sb = new StringBuilder();
        sb.append(" * ");
        sb.append(MergeConstants.NEW_ELEMENT_TAG);
        if (markAsDoNotDelete) {
            sb.append(" do_not_delete_during_merge");
        }
        String s = getDateString();
        if (s != null) {
            sb.append(' ');
            sb.append(s);
        }
        javaElement.addJavaDocLine(sb.toString());*/
    }
	
	
	/**
	 * 为枚举添加注释
	 */
	@Override
	public void addEnumComment(InnerEnum innerEnum, IntrospectedTable introspectedTable) {
		if (suppressAllComments) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        innerEnum.addJavaDocLine("/**");
        sb.append(" * ");
        sb.append(introspectedTable.getFullyQualifiedTable());
        innerEnum.addJavaDocLine(sb.toString().replace("\n", " "));
        innerEnum.addJavaDocLine(" */");
	}
 
	/**
	 * Java属性注释
	 */
	@Override
	public void addFieldComment(Field field, IntrospectedTable introspectedTable) {
		if (suppressAllComments) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        field.addJavaDocLine("/**");
        sb.append(" * ");
        sb.append(introspectedTable.getFullyQualifiedTable());
        field.addJavaDocLine(sb.toString().replace("\n", " "));
        field.addJavaDocLine(" */");
        
        
        
	}
 
	/**
	 * 为字段添加注释
	 */
	@Override
	public void addFieldComment(Field field, IntrospectedTable introspectedTable,IntrospectedColumn introspectedColumn) {
		if (suppressAllComments) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        field.addJavaDocLine("/**");
        sb.append(" * ");
        sb.append(introspectedColumn.getRemarks());
        field.addJavaDocLine(sb.toString().replace("\n", " "));
        field.addJavaDocLine(" */");
	}
 
	/**
	 * 普通方法的注释，这里主要是XXXMapper.java里面的接口方法的注释
	 */
	@Override
	public void addGeneralMethodComment(Method method, IntrospectedTable introspectedTable) {
//	  if (suppressAllComments) {
//        return;
//      }
//      method.addJavaDocLine("/**");
//      addJavadocTag(method, false);
//      method.addJavaDocLine(" */");
	}
 
	
	/**
	 * 给getter方法加注释
	 */
	@Override
	public void addGetterComment(Method method, IntrospectedTable introspectedTable,IntrospectedColumn introspectedColumn) {
	    if (suppressAllComments) {
            return;
        }
        method.addJavaDocLine("/**");
        StringBuilder sb = new StringBuilder();
        sb.append(" * ");
        sb.append(introspectedColumn.getRemarks());
        method.addJavaDocLine(sb.toString().replace("\n", " "));
        sb.setLength(0);
        sb.append(" * @return ");
        sb.append(introspectedColumn.getActualColumnName());
        sb.append(" ");
        sb.append(introspectedColumn.getRemarks());
        method.addJavaDocLine(sb.toString().replace("\n", " "));
        method.addJavaDocLine(" */");
	}
 
	/**
	 * 给Java文件加注释，这个注释是在文件的顶部，也就是package上面。
	 */
	@Override
	public void addJavaFileComment(CompilationUnit compilationUnit) {
    	SimpleDateFormat sdf=new SimpleDateFormat("yyyyMMdd");
    	compilationUnit.addFileCommentLine("/**");
    	compilationUnit.addFileCommentLine("*");
    	compilationUnit.addFileCommentLine("* "+compilationUnit.getType().getShortName()+".java");
    	compilationUnit.addFileCommentLine("* @author " + author);
    	compilationUnit.addFileCommentLine("* @date "+sdf.format(new Date())+"");
    	compilationUnit.addFileCommentLine("*/");
	}
 
	/**
	 * 为模型类添加注释
	 */
	@Override
	public void addModelClassComment(TopLevelClass arg0, IntrospectedTable arg1) {
		
	}
 
	/**
	 * 为调用此方法作为根元素的第一个子节点添加注释。
	 */
	@Override
	public void addRootComment(XmlElement arg0) {
		
	}

    @Override
    public void addGeneralMethodAnnotation(Method method, IntrospectedTable introspectedTable, Set<FullyQualifiedJavaType> set) {

    }

    @Override
    public void addGeneralMethodAnnotation(Method method, IntrospectedTable introspectedTable, IntrospectedColumn introspectedColumn, Set<FullyQualifiedJavaType> set) {

    }

    @Override
    public void addFieldAnnotation(Field field, IntrospectedTable introspectedTable, Set<FullyQualifiedJavaType> set) {

    }

    @Override
    public void addFieldAnnotation(Field field, IntrospectedTable introspectedTable, IntrospectedColumn introspectedColumn, Set<FullyQualifiedJavaType> set) {

    }

    @Override
    public void addClassAnnotation(InnerClass innerClass, IntrospectedTable introspectedTable, Set<FullyQualifiedJavaType> set) {

    }


    /**
	 * 给setter方法加注释
	 */
	@Override
	public void addSetterComment(Method method, IntrospectedTable introspectedTable,IntrospectedColumn introspectedColumn) {
		if (suppressAllComments) {
            return;
        }
        method.addJavaDocLine("/**");
        StringBuilder sb = new StringBuilder();
        sb.append(" * ");
        sb.append(introspectedColumn.getRemarks());
        method.addJavaDocLine(sb.toString().replace("\n", " "));
        Parameter parm = method.getParameters().get(0);
        sb.setLength(0);
        sb.append(" * @param ");
        sb.append(parm.getName());
        sb.append(" ");
        sb.append(introspectedColumn.getRemarks());
        method.addJavaDocLine(sb.toString().replace("\n", " "));
        method.addJavaDocLine(" */");
	}
 
}
