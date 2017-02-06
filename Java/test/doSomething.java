package com.questfree.jiameng.ui;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bouncycastle.asn1.ocsp.Request;

import com.questfree.jiameng.service.impl.JiaMengPageManagePriv;
import com.zving.contentcore.bl.SiteBL;
import com.zving.contentcore.code.ContentStatus;
import com.zving.contentcore.config.ResourceRoot;
import com.zving.framework.Current;
import com.zving.framework.UIFacade;
import com.zving.framework.User;
import com.zving.framework.annotation.Alias;
import com.zving.framework.annotation.Priv;
import com.zving.framework.collection.Mapx;
import com.zving.framework.core.handler.ZAction;
import com.zving.framework.data.DataTable;
import com.zving.framework.data.Q;
import com.zving.framework.data.Transaction;
import com.zving.framework.orm.DAOSet;
import com.zving.framework.ui.control.DataGridAction;
import com.zving.framework.ui.control.LongTimeTask;
import com.zving.framework.utility.DateUtil;
import com.zving.framework.utility.IOUtil;
import com.zving.framework.utility.LogUtil;
import com.zving.framework.utility.NumberUtil;
import com.zving.framework.utility.StringUtil;
import com.zving.schema.ZCCatalog;
import com.zving.schema.ZCContent;
import com.zving.schema.ZCSite;
import com.zving.schema.qf_jm_page;

/**
 * 全站页面管理
 */
@Alias("JMPageManage")
public class JMPageManageUI extends UIFacade {
	@Priv(JiaMengPageManagePriv.MenuID)
	public void bindListGrid(DataGridAction dga) {
		Q q = new Q("select * from qf_jm_page where 1=1");
		if (StringUtil.isNotEmpty($V("Url"))) {
			q.and().eq("Url", $V("Url"));
		}
		if (StringUtil.isNotEmpty($V("Title"))) {
			q.and().like("Title", $V("Title"));
		}

		dga.setTotal(q);
		DataTable dt = q.fetch(dga.getPageSize(), dga.getPageIndex());
		dga.bindData(dt);
	}

	// public static void main(String[] args) {
	// String url =
	// "F:\\work\\七风\\workspace\\qfcms\\wwwroot_release\\km28\\project\\cyyl\\xc1\\641684.shtml";
	// System.out.println(url.replaceAll("\\\\", "/"));
	// }

	private static String getH5Path(String path) {
		if (path.contains("www")) {
			return path.replace("www", "m");
		}
		if (path.startsWith("http://")) {
			return path.substring(0, 7) + "m." + path.substring(7);
		}
		if (path.startsWith("https://")) {
			return path.substring(0, 8) + "m." + path.substring(8);
		}
		return "m." + path;
	}

	@Priv
	@Alias(alone = true, value = "jiameng/exportAllDatas")
	public void exportAllDatas(ZAction za) {
		StringBuilder sb = new StringBuilder();
		DAOSet<qf_jm_page> pages = new qf_jm_page().fetch(new Q("where 1=1 "));
		for (qf_jm_page page : pages) {
			sb.append(page.getUrl() + "\t" + page.getTitle() + "\t" + page.getPageTitle() + "\r\n"); // +++ //
		}

		ByteArrayInputStream fis = null;
		try {
			fis = new ByteArrayInputStream(sb.toString().getBytes());
			IOUtil.download(za.getRequest(), za.getResponse(), DateUtil.getCurrentDate("yyyyMMddHHmmss") + ".txt", fis);
		} catch (Exception e) {
			e.printStackTrace();
			za.writeHTML(StringUtil.htmlEncode(e.getMessage()));
		} finally {
			try {
				if (fis != null) {
					fis.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Priv
	@Alias(alone = true, value = "jiameng/exportOffLineContentPages")
	public void getOffLineContentPages(ZAction za) {
		String realPath = ResourceRoot.getValue();// 获取项目路径
		long currentSite = SiteBL.getCurrentSite();// 125
		ZCSite site = new ZCSite();
		site.setID(currentSite);
		if (!site.fill()) {
			fail("站点不存在");
		}

		final String pcPath = (realPath + site.getPath() + "/");// 扫描路径
		final String h5Path = (realPath + site.getPath() + "_Html5/");

		String pcPrefix = site.getURL();
		String h5Prefix = getH5Path(pcPrefix);

		DAOSet<ZCContent> contents = new ZCContent().fetch(new Q("where 1=1 and SiteID= ?", currentSite));
		Mapx<Long, ZCContent> contentMap = new Mapx<Long, ZCContent>();
		for (ZCContent zcContent : contents) {
			contentMap.put(zcContent.getID(), zcContent);
		}
		StringBuilder sb = new StringBuilder();
		List<String> pcPages = listPages(pcPath, contentMap);
		List<String> h5Pages = listPages(h5Path, contentMap);
		for (String string : pcPages) {
			System.out.println(string);
			String url = string.replaceAll("\\\\", "/").replace(pcPath, pcPrefix);
			sb.append(url + "\r\n");
		}
		for (String string : h5Pages) {
			System.out.println(string);
			String url = string.replaceAll("\\\\", "/").replace(h5Path, h5Prefix);
			sb.append(url + "\r\n");
		}

		ByteArrayInputStream fis = null;
		try {
			fis = new ByteArrayInputStream(sb.toString().getBytes());
			IOUtil.download(za.getRequest(), za.getResponse(), DateUtil.getCurrentDate("yyyyMMddHHmmss") + ".txt", fis);
		} catch (Exception e) {
			e.printStackTrace();
			za.writeHTML(StringUtil.htmlEncode(e.getMessage()));
		} finally {
			try {
				if (fis != null) {
					fis.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private static List<String> listPages(String path, Mapx<Long, ZCContent> contentMap) {
		List<String> rets = new ArrayList<String>();
		File filePath = new File(path);
		File[] listFiles = filePath.listFiles();
		LogUtil.info("正在检索:" + path);
		if (listFiles == null) {
			return rets;
		}
		for (File file : listFiles) {
			if (file.isFile()) {
				if (!file.getName().endsWith(".shtml")) {
					continue;
				}
				String url = file.getPath();
				url = url.replaceAll("\\\\", "/");
				String surl = url.substring(url.lastIndexOf("/") + 1, url.lastIndexOf("."));
				if (!NumberUtil.isInt(surl)) {
					continue;
				}
				if (!contentMap.containsKey(Long.parseLong(surl))) {
					rets.add(url);
					continue;
				}
				ZCContent content = contentMap.get(Long.parseLong(surl));
				if (content.getStatus() == ContentStatus.DRAFT || content.getStatus() == ContentStatus.OFFLINE) {
					/*
					 * try { Document doc = Jsoup.parse(file, "UTF8"); if
					 * (doc.select(".contain-404").size()>0) { continue; } //
					 * System.out.println(doc.getAllElements()); rets.add(url);
					 * } catch (IOException e) { rets.add(url);
					 * e.printStackTrace(); }
					 */
					rets.add(url);
				}
			} else {
				if (file.getPath().contains("include") || file.getPath().contains("template")
						|| file.getPath().contains("js") || file.getPath().contains("css")
						|| file.getPath().contains("upload") || file.getPath().contains("images")) {
					continue;
				}
				rets.addAll(listPages(file.getPath(), contentMap));
			}
		}

		return rets;
	}

	/**
	 * 扫描加载
	 */
	@Priv(JiaMengPageManagePriv.MenuID)
	public void reload() {
		String realPath = ResourceRoot.getValue();// 获取项目路径
		long currentSite = SiteBL.getCurrentSite();// 125
		ZCSite site = new ZCSite();
		site.setID(currentSite);
		if (!site.fill()) {
			fail("站点不存在");
		}

		Q q = new Q("select Properties from ZCPlatformProperty where  SiteID='" + currentSite
				+ "' and PlatformID='Html5' and CatalogID='0'");
		DataTable fetch = q.fetch();
		Object[] columnValues = fetch.getColumnValues("Properties");
		String pro = (String) columnValues[0];

		final String pcPath = (realPath + site.getPath() + "/");// 扫描路径
		final String h5Path = (realPath + site.getPath() + "/" + "_Html5/");

		final String identification1 = site.getPath() + "/";// 标识
		final String identification2 = site.getPath() + "/" + "_Html5/";

		final String contextPathPc = site.getURL();// 获取站点前缀：www.km28.com
		String contextPathH5 = pro.substring(pro.indexOf("SiteUrl=") + 8, pro.indexOf("OtherImageSpecification"));
		final String trim = contextPathH5.trim();
		// 清空表
		Q q2 = new Q("TRUNCATE TABLE qf_jm_page");
		q2.execute();

		final Transaction tran = Current.getTransaction();

		LongTimeTask ltt = LongTimeTask.getInstanceByType("Reload");
		if (ltt != null && ltt.isAlive()) {
			fail("站点导入任务正在进行...");
			return;
		}
		ltt = new LongTimeTask() {
			@Override
			public void execute() {
				long start = System.currentTimeMillis();
				try {
					loadPage(pcPath, contextPathPc, tran, identification1);
					loadPage(h5Path, trim, tran, identification2); // load H5 page
					if (tran.commit()) {
						success("加载成功");
					} else {
						fail("加载失败");
					}
				} catch (Exception e) {
					e.printStackTrace();
					fail("加载异常");
					return;
				}
				LogUtil.info("加载数据完成，耗时(s)：" + (System.currentTimeMillis() - start) / 1000 + " 。");
			}
		};
		ltt.setType("Reload");
		ltt.setUser(User.getCurrent());
		ltt.start();
		$S("TaskID", ltt.getTaskID());
	}

	public void loadPage(String path, String contextPath, Transaction tran, String identification) throws IOException {
		File filePath = new File(path);
		// 本地测试  +++ identification = identification.replace("/", "\\");
		// System.out.println(filePath);
		File[] listFiles = filePath.listFiles();
		if (listFiles != null) {
			for (File file : listFiles) {
				if (file.isFile()) {
					// System.out.println(file.getPath());
					if (file.getName().endsWith(".shtml")) {
						if (file.getName().startsWith("index")) {// 处理列表页
							String path2 = file.getPath();
							if (path2.endsWith("kmway/index.shtml") || path2.endsWith("kmway_Html5/index.shtml")) {
//							if (path2.endsWith("km28\\index.shtml") || path2.endsWith("km28_Html5\\index.shtml")) {
								// 首页单独处理
								qf_jm_page newpage = new qf_jm_page();
								newpage.setTitle("首页");
								newpage.setUrl(contextPath);
								newpage.setPageTitle(getPageTitle(file)); // +++ //
								tran.add(newpage, Transaction.INSERT);
							}
//							else {
//								String title = file.getName().replace(".shtml", "");//zccon.getTitle();
//								String url = contextPath + file.getPath().replace("E:\\qfcms\\wwwroot_release\\km28\\", "").replace("\\", "/");// 项目地址\
//								qf_jm_page newpage = new qf_jm_page();
//								newpage.setTitle(title);
//								newpage.setUrl(url);
//								newpage.setPageTitle(getPageTitle(file)); // +++ //
//								tran.add(newpage, Transaction.INSERT);
//							}
							else {
								if (file.getPath().contains("article")) {// 处理项目文章列表页
									String substring = file.getPath().substring(file.getPath().indexOf("article/"), // 本地测试 +++ article\\
											file.getPath().indexOf("index"));
									System.out.println(substring);

									DAOSet<ZCCatalog> dao = new ZCCatalog().query(new Q("where Path = '" + substring
											+ "'")); //本地测试  substring.replace("\\", "/")
									if (dao.size() != 0) {
										ZCCatalog zc = dao.get(0);
										long parentID = zc.getParentID();
										ZCCatalog parentZc = new ZCCatalog();
										parentZc.setID(parentID);
										if (parentZc.fill()) {
											qf_jm_page newpage = new qf_jm_page();
											newpage.setTitle(parentZc.getName() + "文章列表页");
											newpage.setUrl(contextPath + substring + file.getName());
											newpage.setPageTitle(getPageTitle(file)); // +++ //
											tran.add(newpage, Transaction.INSERT);
//											System.out.println("ok");
										}
									}
								} else {// 处理排行榜，图书馆，新闻资讯等其他列表页
									String substring = file.getPath().substring(
											file.getPath().indexOf(identification) + identification.length(),
											file.getPath().indexOf("index"));
//									System.out.println(file.getPath());
//									System.out.println(identification);
//									System.out.println(substring);

									DAOSet<ZCCatalog> dao = new ZCCatalog().query(new Q("where Path = '" + substring
											+ "'")); //本地测试  substring.replace("\\", "/")
									if (dao.size() != 0) {
										ZCCatalog zc = dao.get(0);
										String name = zc.getName();
										qf_jm_page newpage = new qf_jm_page();
										newpage.setTitle(name + "列表页"); //name + "列表页"
										newpage.setUrl(contextPath + substring + file.getName());
										newpage.setPageTitle(getPageTitle(file)); // +++ //
										tran.add(newpage, Transaction.INSERT);
									}
								}
							}
						}
//						else {
//							String id = file.getName().substring(0, file.getName().indexOf(".shtml"));
//							ZCContent zccon = new ZCContent();
//							zccon.setID(id);
//							String title = file.getName().replace(".shtml", "");//zccon.getTitle();
//							String url = contextPath + file.getPath().replace("E:\\qfcms\\wwwroot_release\\km28\\", "").replace("\\", "/");// 项目地址\
//							qf_jm_page newpage = new qf_jm_page();
//							newpage.setTitle(title);
//							newpage.setUrl(url);
//							newpage.setPageTitle(getPageTitle(file)); // +++//
//							tran.add(newpage, Transaction.INSERT);
//						}
						else if (file.getName().contains("list")) {// 排行榜文章列表，单独处理
							String substring = file.getPath().substring(
									file.getPath().indexOf(identification) + identification.length(),
									file.getPath().indexOf("list"));
							DAOSet<ZCCatalog> dao = new ZCCatalog().query(new Q("where Path = '" + file.getPath() + "'"));
							if (dao.size() != 0) {
								ZCCatalog zc = dao.get(0);
								String name = zc.getName();
								qf_jm_page newpage = new qf_jm_page();
								newpage.setTitle(name + "文章列表页");
								newpage.setUrl(contextPath + substring + file.getName());
								newpage.setPageTitle(getPageTitle(file)); // +++ //
								tran.add(newpage, Transaction.INSERT);
							}
						}
					else {// 处理详情页
//							System.out.println(file.getPath());
							String name = file.getName();
							String substring = file.getPath().substring(
									file.getPath().indexOf(identification) + identification.length());
							String id = name.substring(0, name.indexOf(".shtml"));
							ZCContent zccon = new ZCContent();
							zccon.setID(id);
//							System.out.println(id);
							if (zccon.fill()) {
								String title = file.getName();//zccon.getTitle();
								String url = contextPath + substring;// 项目地址\
								qf_jm_page newpage = new qf_jm_page();
								newpage.setTitle(title);
								newpage.setUrl(url);
								newpage.setPageTitle(getPageTitle(file)); // +++ //
								tran.add(newpage, Transaction.INSERT);
							}
						}
					}

					if (tran.getOperateList().size() >= 1000) {
						tran.commit();
					}
				} else {
					if (!file.getPath().contains("include")) {
						loadPage(file.getPath(), contextPath, tran, identification);
					}
				}
			}
		}
	}

	public String getPageTitle(File file) throws IOException {

		String group = "";

		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
			String line = "";
			String reg = "<title>.*</title>";
			while ((line = br.readLine()) != null) {
				Pattern pattern = Pattern.compile(reg);
				Matcher matcher = pattern.matcher(line);
				if (matcher.find()) {
					group = matcher.group();
					group = group.replaceAll("<.*?>", "");
				}
			}
			br.close();
			return group;
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
}
