package com.questfree.jiameng.crawl;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import bsh.This;

import com.ibm.db2.jcc.c.sb;
import com.sun.org.apache.xpath.internal.operations.Div;
import com.zving.contentcore.util.CatalogUtil;
import com.zving.framework.Config;
import com.zving.framework.Current;
import com.zving.framework.data.Q;
import com.zving.framework.data.Transaction;
import com.zving.framework.extend.ExtendManager;
import com.zving.framework.orm.DAOSet;
import com.zving.framework.ui.control.LongTimeTask;
import com.zving.framework.utility.DateUtil;
import com.zving.framework.utility.IOUtil;
import com.zving.framework.utility.StringUtil;
import com.zving.schema.ZCCatalog;
import com.zving.schema.qf_jm_crawldp;
import com.zving.schema.qf_jm_page;

public class TitleCrawlFromDianping {

	// public static final String suffix =
	// "?aid=19097077%2C67997136%2C67678275%2C2212147%2C65583740%2C68946010%2C69895065%2C57427640%2C73611741%2C72447645&cpt=19097077%2C67997136%2C67678275%2C2212147%2C65583740%2C68946010%2C69895065%2C57427640%2C73611741%2C72447645&tc=3";

	public static void main(String[] args) throws IOException {
		Config.isPluginContext = true;
		Config.loadConfig();
		Config.setValue("App.ContextPath", "/cmsjiameng/");
		ExtendManager.getInstance().start();
		// String url = "https://www.dianping.com/search/Category/8/10/g110p1" +
		// suffix;
		String url = "https://www.dianping.com/search/Category/1/1/g101p1";
		ZCCatalog catalog = CatalogUtil.getDAO(17090);
		doTraversal(url, catalog, null);
		// crawlFromDP(url, catalog, null);
		// String[][] test = getCategorySet();
		// doSomething(test);
		// System.out.println(Arrays.deepToString(test));
		// for (int i = 0; i < test.length; i++) {
		// for (int j = 0; j < test[i].length; j++) {
		// System.out.println(test[i][j].toString());
		// }
		// }
	}

	public static StringBuilder crawlFromDP(Document doc, ZCCatalog catalog, LongTimeTask ltt, int page)
			throws IOException {

		// Document doc = null;

		Elements selects = null;

		StringBuilder sb = new StringBuilder();

		// for (int i = 1; i < 51; i++) {
		//
		// url = url.replaceFirst("g110p\\d+\\?", "g110p" + i + "?");
		// // System.out.println(url);
		// doc = doConnect(url);
		//
		// selects = doc.select("div.tit a h4");
		//
		// for (Element select : selects) {
		// sb.append(getItemName(select) + "\r\n");
		// System.out.println(select.text());
		// }
		// sb.append(">>>>>>Page " + i + " done!<<<<<<\r\n\r\n");
		// // exportToFile(sb.toString());
		// // sb = new StringBuilder();
		// // System.out.println("Page " + i + " done!");
		// }

		// doc = doConnect(url);

		selects = doc.select("div.tit a h4");

		for (Element select : selects) {
			sb.append(getItemName(select) + "\r\n");
			// System.out.println(select.text());
		}
		sb.append(">>>>>>Page g" + page + " done!<<<<<<\r\n\r\n");
		// exportToFile(sb.toString(), doc);
		return sb;
	}

	public static boolean exportToFile(StringBuilder sb, Document doc, boolean flag) throws IOException {

		if (!flag) {

			return false;
		}

		File file = new File("C:\\Users\\Administrator\\Desktop\\crawl\\crawlFromDP_" + getCity(doc) + "_"
				+ getCurCategory(doc) + "_" + DateUtil.getCurrentDate("yyyyMMddHHmmss") + ".txt");

		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		BufferedWriter bw = new BufferedWriter(new FileWriter(file, true));
		bw.write(sb.toString());
		// bw.write("\r\n");
		bw.flush();
		bw.close();

		return true;
	}

	public static String getCity(Document doc) {

		Elements elements = doc.select("a.J-city");

		// for(Element element : elements){
		// System.out.println(element.text());
		// }

		return elements.text();
	}

	public static String getAllCategory(Document doc) {

		Elements elements = doc.select("div#classfy a span");
		for (Element element : elements) {
			System.out.println(element.text());
		}

		return null;
	}

	public static String getCurCategory(Document doc) {

		if (doc.select("div.container a.J-current-category").text() == "全部分类") {
			return null;
		}
		Elements elements = doc.select("div#classfy a.cur span");
		// for (Element element : elements) {
		// System.out.println(element.text());
		// }

		return elements.text();
	}

	public static String getItemName(Element element) {

		return element.text();
	}

	public static String doTraversal(String url, ZCCatalog Category, LongTimeTask ltt) throws IOException {

		// Q q = new Q("TRUNCATE TABLE qf_jm_crawldp");
		// q.execute();

		for (int i = 7; i < 100; i++) {// 已经执行到苏州(CODE:6)
			url = url.replaceFirst("Category/\\d+/\\d+/g\\d+p\\d+", "Category/" + i + "/10/g101p1");
			if (!isAvailable(doConnect(url))) {
				System.out.println("页面不可用！");
				continue;
			}
			for (int j = 10; j < 11; j++) {
				url = url.replaceFirst("\\d+/\\d+/g\\d+p\\d+", i + "/" + j + "/g101p1");
				if (!isAvailable(doConnect(url))) {
					System.out.println("页面不可用！");
					continue;
				}
				for (int k = 101; k < 3500; k++) {
					url = url.replaceFirst("\\d+/g\\d+p\\d+", j + "/g" + k + "p1");
					if (!isAvailable(doConnect(url))) {
						System.out.println("页面不可用！");
						continue;
					}

					// StringBuilder sb = new StringBuilder();
					Document doc = null;
					int l;
					final Transaction tran = Current.getTransaction();
					// boolean isUseful = true;
					for (l = 1; l < 50; l++) {

						url = url.replaceFirst("g\\d+p\\d+", "g" + k + "p" + l);
						doc = doConnect(url);
						if (!isAvailable(doc)) {
							System.out.println("页面不可用！");
							break;
						}
						// if (!isNeededPage(doc)) {
						// isUseful = false;
						// System.out.println("页面不可用！");
						// break;
						// }

						// isUseful = true;
						System.out.println(url);

						getSomeThing(doc, tran);

						// Elements elements = doc.select("div.tit a h4");
						// for (Element element : elements) {
						// executeSQL(tran, getSomeThing(doc, element));
						// }

						// sb.append(crawlFromDP(doc, Category, ltt,
						// l).toString());
						// exportToFile(crawlFromDP(doc, Category, ltt, l),
						// doc);
						// System.out.println("page_" + i + "_" + j + "_" + "g"
						// + k + "_" + "p" + l + " done!");
					}
					// exportToFile(sb, doc, isUseful);
				}
			}
		}
		return null;
	}

	public static boolean isAvailable(Document doc) {

		if (!(doc == null)) {
			Elements elements = doc.select("title");
			if (elements.text().equals("抱歉-大众点评网")) {
				return false;
			}
			return true;
		}
		return false;
	}

	public static boolean isNeededPage(Document doc) {

		if (doc.select("a.current-category.J-current-category").text().equals("全部分类")) {
			return false;
		}
		if (doc.select("div#classfy a.cur span").equals(null)) {
			return false;
		}
		return true;
	}

	public static Document doConnect(String url) {

		String ip = "111.76.129.117";
		System.getProperties().setProperty("http.proxyHost", ip);
		System.getProperties().setProperty("http.proxyPort", "808");

		Document doc = null;

		try {
			doc = Jsoup
					.connect(url)
					.timeout(50000)
					.ignoreContentType(true)
					.followRedirects(true)
					.userAgent(
							"Mozilla/5.0 (Windows NT 5.1; zh-CN) AppleWebKit/535.12 (KHTML, like Gecko) Chrome/22.0.1229.79 Safari/535.12")
					.get();

		} catch (IOException e) {
			try {
				// 重试一遍
				Thread.sleep(2000);
				doc = Jsoup
						.connect(url)
						.timeout(50000)
						.ignoreContentType(true)
						.followRedirects(true)
						.userAgent(
								"Mozilla/5.0 (Windows NT 5.1; zh-CN) AppleWebKit/535.12 (KHTML, like Gecko) Chrome/22.0.1229.79 Safari/535.12")
						.get();
			} catch (Exception e2) {
				return null;
				// e2.printStackTrace();
			}
		}
		return doc;
	}

	public static qf_jm_crawldp getSomeThing(Document doc, Transaction tran) {

		qf_jm_crawldp crawldp = new qf_jm_crawldp();
		Elements elements = doc.select("div.txt");
		for (Element element : elements) {
			crawldp.setCategory(element.select("div.tag-addr a span.tag").text());
			crawldp.setName(element.select("div.tit a").attr("title").toString());
			// crawldp.setName(element.select("div.tit a").first().attr("href").toString());
			crawldp.setCity(doc.select("a.city.J-city").text());
			crawldp.setisBranch(element.select("a.shop-branch").text());
			// crawldp.setID(element.select("div.tit").first().attr("href"));
			tran.add(crawldp, Transaction.INSERT);
			// if (tran.getOperateList().size() >= 1000) {
			// tran.commit();
			// System.out.println(">---commit---<");
			// }
			tran.commit();
		}
		return crawldp;
	}

	/**
	 * @Title: getCategory
	 * @Description: 获取所有的项目类别
	 * @return: String[][]
	 */

	public static String[][] getCategorySet() {
		Document document = doConnect("http://www.dianping.com/search/category/1/10/g110");
		Elements elements = document
				.select("li.primary-category.J-primary-category div.secondary-category.J-secondary-category");
		// Elements tElements = document.select("div.group");
		// System.out.println("tElements isEmpty:"+tElements.isEmpty());
		// System.out.println("elements isEmpty:"+elements.isEmpty());
		String[][] categorySet = new String[elements.size()][];
		for (Element element : elements) {
			// System.out.println(element.attr("data-key").toString());
			Elements elements2 = element.select("a");
			categorySet[elements.indexOf(element)] = new String[elements2.size()];
			// System.out.println("elements2 isEmpty:"+elements2.isEmpty());
			for (Element element2 : elements2) {
				// System.out.println(element2.attr("data-key").toString());
				categorySet[elements.indexOf(element)][elements2.indexOf(element2)] = element2.attr("href").toString();
				// System.out.println(elements.indexOf(element));
				// System.out.println(elements2.indexOf(element2));
				// categorySet[elements.indexOf(element)][elements2.indexOf(element2)]
				// = "haha";
			}
		}
		return categorySet;
	}

	public static void doSomething(String[][] str) {
		for (String[] tStr1 : str) {
			for (String tStr2 : tStr1) {
				Document document = doConnect(tStr2);
				final Transaction tran = Current.getTransaction();
				getSomeThing(document, tran);
			}
		}
	}
}
