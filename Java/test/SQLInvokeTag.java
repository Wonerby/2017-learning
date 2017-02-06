package com.zving.demo.tag;

import com.zving.demo.DemoPlugin;
import com.zving.framework.data.DataTable;
import com.zving.framework.data.DataTypes;
import com.zving.framework.data.QueryBuilder;
import com.zving.framework.template.AbstractTag;
import com.zving.framework.template.TagAttr;
import com.zving.framework.template.exception.TemplateRuntimeException;
import com.zving.framework.utility.StringUtil;

import java.util.ArrayList;
import java.util.List;

import org.omg.CORBA.StringHolder;
import org.omg.stub.java.rmi._Remote_Stub;

public class SQLInvokeTag extends AbstractTag {
	  protected String sql;
	  protected int pageIndex;
	  protected int pageSize;
	  protected DataTable dt;
	  protected int index;

	  public SQLInvokeTag() {
	    super();
	    System.out.println("SQLInvokeTag 构造执行...");
	  }

	  @Override
	  public String getDescription() {
	    return "执行SQL语句，循环输出结果到标签体...";
	  }

	  @Override
	  public String getExtendItemName() {
	    return "SQLInvokeTag";
	  }

	  @Override
	  public String getPluginID() {
	    return DemoPlugin.ID;
	  }

	  @Override
	  public String getPrefix() {
	    return "z";
	  }

	  @Override
	  public List<TagAttr> getTagAttrs() {
	    /*定义标签属性*/
	List<TagAttr> list = new ArrayList<TagAttr>();
	list.add(new TagAttr("sql",false,DataTypes.STRING,"要执行的SQL语句"));
	list.add(new TagAttr("pageIndex",false,DataTypes.INTEGER,"当前页码"));
	list.add(new TagAttr("pageSize",false,DataTypes.INTEGER,"分页大小"));
	    return list;
	  }

	  @Override
	  public int doStartTag() throws TemplateRuntimeException {
	    System.out.println("Do start Tag...");
	QueryBuilder qb = new QueryBuilder(sql);
	if(StringUtil.isNotEmpty(getAttribute("pageSize"))) {
	  dt = qb.executePagedDataTable(pageSize,pageIndex);
	} else {
	  dt = qb.executeDataTable();
	}
	if(dt.getRowCount() > 0) {
	  index = 0;
	  context.addDataVariable("Value",dt.getDataRow(0));
	  context.addDataVariable("Index",index);
	      return EVAL_BODY_INCLUDE;
	    } else {
	      return SKIP_BODY;
	    }
	  }

	  @Override
	  public int doAfterBody() throws TemplateRuntimeException {
	      System.out.println("Do doAfterBody!~!!");
	  index = index + 1;
	  /* 标签体执行完成，数据索引+1，如果索引超出了查询结果数，执行标签后面的页面，否则取出当前索引数据，执行重新执行标签体 */
	  if (index >= dt.getRowCount()) {
	      return EVAL_PAGE;// 执行标签后面的页面内容
	  } else {
	      context.addDataVariable("Value", dt.getDataRow(index));// 当前索引数据
	  context.addDataVariable("Index", index);// 当前索引
	          return EVAL_BODY_AGAIN;
	      }
	  }

	  @Override
	  public int doEndTag() throws TemplateRuntimeException {
	    return super.doEndTag();
	  }

	  public String getSql() {
	    return sql;
	  }

	  public void setSql(String sql) {
	    this.sql = sql;
	  }

	  public int getPageIndex() {
	    return pageIndex;
	  }

	  public void setPageIndex(int pageIndex) {
	    this.pageIndex = pageIndex;
	  }

	  public int getPageSize() {
	    return pageSize;
	  }

	  public void setPageSize(int pageSize) {
	    this.pageSize = pageSize;
	  }

		@Override
		public String getTagName() {
			// TODO Auto-generated method stub
		return null;
	}
}
