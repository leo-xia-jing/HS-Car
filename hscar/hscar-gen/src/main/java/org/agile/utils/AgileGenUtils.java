package org.agile.utils;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.agile.common.exception.RRException;
import org.agile.common.utils.AgileDateUtils;
import org.agile.entity.ColumnEntity;
import org.agile.entity.TableEntity;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

/**
 * 代码生成器   工具类
 * 
 * @author zhangmm
 */
public class AgileGenUtils {
	
	public static List<String> getTemplates(){
		List<String> templates = new ArrayList<String>();
		templates.add("template/agile/Entity.java.vm");
		templates.add("template/agile/Dao.java.vm");
		templates.add("template/agile/Dao.xml.vm");
		templates.add("template/agile/ServiceApi.java.vm");
		templates.add("template/agile/ServiceImpl.java.vm");
		templates.add("template/agile/Controller.java.vm");
		templates.add("template/agile/list.html.vm");
		templates.add("template/agile/list.js.vm");
		templates.add("template/agile/menu.sql.vm");
		return templates;
	}
	
	/**
	 * 生成代码
	 */
	public static void generatorCode(Map<String, String> table, 
			List<Map<String, String>> columns, ZipOutputStream zip){
		//配置信息
		Configuration config = getConfig();
		
		//表信息
		TableEntity tableEntity = new TableEntity();
		tableEntity.setTableName(table.get("tableName"));
		tableEntity.setComments(table.get("tableComment"));
		//表名转换成Java类名
		String className = tableToJava(tableEntity.getTableName(), config.getString("tablePrefix"));
		tableEntity.setClassName(className);
		tableEntity.setClassname(StringUtils.uncapitalize(className));
		
		//列信息
		List<ColumnEntity> columsList = new ArrayList<>();
		for(Map<String, String> column : columns){
			ColumnEntity columnEntity = new ColumnEntity();
			columnEntity.setColumnName(column.get("columnName"));
			columnEntity.setDataType(column.get("dataType"));
			columnEntity.setComments(column.get("columnComment"));
			columnEntity.setExtra(column.get("extra"));
			
			//列名转换成Java属性名
			String attrName = columnToJava(columnEntity.getColumnName());
			columnEntity.setAttrName(attrName);
			columnEntity.setAttrname(StringUtils.uncapitalize(attrName));
			
			//列的数据类型，转换成Java类型
			String attrType = config.getString(columnEntity.getDataType(), "unknowType");
			columnEntity.setAttrType(attrType);
			
			//是否主键
			if("PRI".equalsIgnoreCase(column.get("columnKey")) && tableEntity.getPk() != null){
				tableEntity.setPk(columnEntity);
			}
			
			columsList.add(columnEntity);
		}
		tableEntity.setColumns(columsList);
		
		//没主键，则第一个字段为主键
		if(tableEntity.getPk() == null){
			tableEntity.setPk(tableEntity.getColumns().get(0));
		}
		
		//设置velocity资源加载器
		Properties prop = new Properties();  
		prop.put("file.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");  
		Velocity.init(prop);
		
		//封装模板数据
		String packagePath = config.getString("package");
		String[] packageArr = packagePath.split("\\.");
		String controllerName = "";
		for (int i = 2; i < packageArr.length; i++) {
			controllerName += "/" + packageArr[i];
		}
		controllerName += "/" + tableEntity.getClassname();
		String shiroPermissionsPrefix = "";
		for (int i = 2; i < packageArr.length; i++) {
			shiroPermissionsPrefix += packageArr[i] + ":";
		}
		shiroPermissionsPrefix += tableEntity.getClassname();
		String pagePath = "";
		for (int i = 2; i < packageArr.length; i++) {
			pagePath += "/" + packageArr[i];
		}
		Map<String, Object> map = new HashMap<>();
		map.put("tableName", tableEntity.getTableName());
		map.put("comments", tableEntity.getComments());
		map.put("pk", tableEntity.getPk());
		map.put("className", tableEntity.getClassName());
		map.put("classname", tableEntity.getClassname());
		map.put("pathName", tableEntity.getClassname());
		map.put("columns", tableEntity.getColumns());
		map.put("package", config.getString("package"));
		map.put("controllerName", controllerName);
		map.put("shiroPermissionsPrefix", shiroPermissionsPrefix);
		map.put("pagePath", pagePath);
		map.put("author", config.getString("author"));
		map.put("email", config.getString("email"));
		map.put("datetime", AgileDateUtils.dateToString(new Date(), AgileDateUtils.DATE_EXPRESSION_GENERAL));
        VelocityContext context = new VelocityContext(map);
        
        //获取模板列表
		List<String> templates = getTemplates();
		for(String template : templates){
			//渲染模板
			StringWriter sw = new StringWriter();
			Template tpl = Velocity.getTemplate(template, "UTF-8");
			tpl.merge(context, sw);
			
			try {
				//添加到zip
				zip.putNextEntry(new ZipEntry(getFileName(template, tableEntity.getClassName(), tableEntity.getClassname(), config.getString("package"))));  
				IOUtils.write(sw.toString(), zip, "UTF-8");
				IOUtils.closeQuietly(sw);
				zip.closeEntry();
			} catch (IOException e) {
				throw new RRException("渲染模板失败，表名：" + tableEntity.getTableName(), e);
			}
		}
	}
	
	
	/**
	 * 列名转换成Java属性名
	 */
	public static String columnToJava(String columnName) {
		return WordUtils.capitalizeFully(columnName, new char[]{'_'}).replace("_", "");
	}
	
	/**
	 * 表名转换成Java类名
	 */
	public static String tableToJava(String tableName, String tablePrefix) {
		if(StringUtils.isNotBlank(tablePrefix)){
			tableName = tableName.replace(tablePrefix, "");
		}
		return columnToJava(tableName);
	}
	
	/**
	 * 获取配置信息
	 */
	public static Configuration getConfig(){
		try {
			return new PropertiesConfiguration("generator.properties");
		} catch (ConfigurationException e) {
			throw new RRException("获取配置文件失败，", e);
		}
	}
	
	/**
	 * 获取文件名
	 */
	public static String getFileName(String template, String className, String classname, String packageName){
		String packagePath = "";
		if(StringUtils.isNotBlank(packageName)){
			packagePath = packageName.replace(".", File.separator) + File.separator;
		}
		
		if(template.contains("Entity.java.vm")){
			return packagePath + "entity" + File.separator + className + "Entity.java";
		}
		
		if(template.contains("Dao.java.vm")){
			return packagePath + "dao" + File.separator + className + "Dao.java";
		}
		
		if(template.contains("Dao.xml.vm")){
			return "resources" + File.separator + packagePath + "dao" + File.separator + className + "Base_Dao.xml";
		}
		
		if(template.contains("ServiceApi.java.vm")){
			return packagePath + "service" + File.separator + "api" + File.separator + "I" + className + "Service.java";
		}
		
		if(template.contains("ServiceImpl.java.vm")){
			return packagePath + "service" + File.separator + "impl" + File.separator + className + "Service.java";
		}
		
		if(template.contains("Controller.java.vm")){
			return packagePath + "controller" + File.separator + className + "Controller.java";
		}
		
		if(template.contains("list.html.vm")){
			return "html" + File.separator + classname + ".html";
		}
		
		if(template.contains("list.js.vm")){
			return "js" + File.separator + classname + ".js";
		}
		
		if(template.contains("menu.sql.vm")){
			return className.toLowerCase() + "_menu.sql";
		}
		
		return null;
	}
}
