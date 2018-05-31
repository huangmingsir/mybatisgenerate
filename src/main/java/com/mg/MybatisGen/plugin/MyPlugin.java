package com.mg.MybatisGen.plugin;

import java.util.List;

import org.mybatis.generator.api.CommentGenerator;
import org.mybatis.generator.api.GeneratedXmlFile;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.JavaVisibility;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.Document;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.codegen.mybatis3.MyBatis3FormattingUtilities;

public class MyPlugin extends PluginAdapter {

	/**
	 * 生成dao
	 */
	@Override
	public boolean clientGenerated(Interface interfaze, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
		FullyQualifiedJavaType fqjt = new FullyQualifiedJavaType("BaseMapper<" + introspectedTable.getBaseRecordType()	+ ">");
		FullyQualifiedJavaType imp = new FullyQualifiedJavaType("com.mg.MybatisGen.common.BaseMapper");
		interfaze.addSuperInterface(fqjt);// 添加 extends BaseDao<User>
		interfaze.addImportedType(imp);// 添加import common.BaseDao;
		interfaze.getMethods().clear();
		return true;
	}

	/**
	 * 生成实体中每个属性
	 */
	@Override
	public boolean modelGetterMethodGenerated(Method method, TopLevelClass topLevelClass,
			IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable, ModelClassType modelClassType) {
		return true;
	}

	/**
	 * 生成实体
	 */
	@Override
	public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
		addSerialVersionUID(topLevelClass, introspectedTable);
		return super.modelBaseRecordClassGenerated(topLevelClass, introspectedTable);
	}
	
	private void addSerialVersionUID(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
		CommentGenerator commentGenerator = context.getCommentGenerator();
		Field field = new Field();
		field.setVisibility(JavaVisibility.PRIVATE);
		field.setType(new FullyQualifiedJavaType("long"));
		field.setStatic(true);
		field.setFinal(true);
		field.setName("serialVersionUID");
		field.setInitializationString("1L");
		commentGenerator.addFieldComment(field, introspectedTable);
		topLevelClass.addField(field);
	}

	/**
	 * 生成mapping
	 */
	@Override
	public boolean sqlMapGenerated(GeneratedXmlFile sqlMap, IntrospectedTable introspectedTable) {
		return super.sqlMapGenerated(sqlMap, introspectedTable);
	}
	
	/**
	 * 生成mapping 添加自定义sql
	 */
	@Override
	public boolean sqlMapDocumentGenerated(Document document, IntrospectedTable introspectedTable) {
		String tableName = introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime();// 数据库表名
		List<IntrospectedColumn> columns = introspectedTable.getAllColumns();
		XmlElement parentElement = document.getRootElement();
		
		// 添加sql——where
		XmlElement sql = new XmlElement("sql");
		sql.addAttribute(new Attribute("id", "sql_where"));
		XmlElement where = new XmlElement("where");
		StringBuilder sb = new StringBuilder();
		for (IntrospectedColumn introspectedColumn : introspectedTable.getNonPrimaryKeyColumns()) {
            XmlElement isNotNullElement = new XmlElement("if"); //$NON-NLS-1$
            sb.setLength(0);
            sb.append(introspectedColumn.getJavaProperty());
            sb.append(" != null"); //$NON-NLS-1$
            isNotNullElement.addAttribute(new Attribute("test", sb.toString())); //$NON-NLS-1$
            where.addElement(isNotNullElement);

            sb.setLength(0);
            sb.append(" and ");
            sb.append(MyBatis3FormattingUtilities.getEscapedColumnName(introspectedColumn));
            sb.append(" = "); //$NON-NLS-1$
            sb.append(MyBatis3FormattingUtilities.getParameterClause(introspectedColumn));
            isNotNullElement.addElement(new TextElement(sb.toString()));
        }
		sql.addElement(where);
		parentElement.addElement(2,sql);
		
		//添加getList
		XmlElement select = new XmlElement("select");
		select.addAttribute(new Attribute("id", "getList"));
		select.addAttribute(new Attribute("resultMap", "BaseResultMap"));
		select.addAttribute(new Attribute("parameterType", introspectedTable.getBaseRecordType()));
		select.addElement(new TextElement(" select "));
		XmlElement include2 = new XmlElement("include");
		include2.addAttribute(new Attribute("refid", "Base_Column_List"));
		select.addElement(include2);
		select.addElement(new TextElement(" from " + introspectedTable.getFullyQualifiedTableNameAtRuntime()));
		XmlElement include = new XmlElement("include");
		include.addAttribute(new Attribute("refid", "sql_where"));
		select.addElement(include);
		parentElement.addElement(select);
		
		//添加getListByPage
		XmlElement select2 = new XmlElement("select");
		select2.addAttribute(new Attribute("id", "getListByPage"));
		select2.addAttribute(new Attribute("resultMap", "BaseResultMap"));
		select2.addAttribute(new Attribute("parameterType", "java.util.Map"));
		select2.addElement(new TextElement(" select "));
		XmlElement include3 = new XmlElement("include");
		include3.addAttribute(new Attribute("refid", "Base_Column_List"));
		select2.addElement(include3);
		select2.addElement(new TextElement(" from " + introspectedTable.getFullyQualifiedTableNameAtRuntime()));
		XmlElement include4 = new XmlElement("include");
		include4.addAttribute(new Attribute("refid", "sql_where"));
		select2.addElement(include4);
		XmlElement isNotNullElement = new XmlElement("if"); //$NON-NLS-1$
        isNotNullElement.addAttribute(new Attribute("test", "begin >= 0 AND pageSize > 0"));
        isNotNullElement.addElement(new TextElement("limit #{begin} , #{pageSize}"));
        select2.addElement(isNotNullElement);
		parentElement.addElement(select2);
		
		//添加getList
		XmlElement select3 = new XmlElement("select");
		select3.addAttribute(new Attribute("id", "getCount"));
		select3.addAttribute(new Attribute("resultType", "java.lang.Long"));
		//select3.addAttribute(new Attribute("parameterType", introspectedTable.getBaseRecordType()));
		select3.addAttribute(new Attribute("parameterType", "java.util.Map"));
		select3.addElement(new TextElement(" select count(ID) "));
		select3.addElement(new TextElement(" from " + introspectedTable.getFullyQualifiedTableNameAtRuntime()));
		XmlElement include5 = new XmlElement("include");
		include5.addAttribute(new Attribute("refid", "sql_where"));
		select3.addElement(include5);
		parentElement.addElement(select3);
		
		return super.sqlMapDocumentGenerated(document, introspectedTable);
	}

	@Override
	public boolean validate(List<String> arg0) {
		return true;
	}

	
}
