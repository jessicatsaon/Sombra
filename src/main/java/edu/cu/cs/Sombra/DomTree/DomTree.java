package edu.cu.cs.Sombra.DomTree;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import edu.cu.cs.Sombra.Config;
import edu.cu.cs.Sombra.Tree.BaseTreeNode;

public class DomTree {

	private DomTreeNode root;
	private int nodenum;
	private String title;
	private List<DomTreeNode> goodNodes;

	public DomTree(String filename) {
		
		try {
			File input = new File(Config.FILE_PATH + filename);
			File output = new File(Config.TEMP_PATH + "modified_" + filename);
			PrintWriter writer = new PrintWriter(output,"UTF-8");
			
			Document doc = Jsoup.parse(input, "UTF-8");
			doc.getElementsByTag("script").remove();
			doc.getElementsByTag("noscript").remove();
			this.title = doc.title();
			this.goodNodes = new ArrayList<DomTreeNode>();
			
			Element cur = doc.body();
			this.root = new DomTreeNode(null, cur.tagName(), cur.id(), cur.className(), cur.html(), cur.ownText());
			this.nodenum = 0;
			this.addChildrenNode(this.root, cur);
			
			writer.write(doc.html()) ;
			writer.flush();
			writer.close();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void addChildrenNode(DomTreeNode curnode, Element cur) {
		cur.attr("sombraid", Integer.toString(this.nodenum));
		curnode.setSombraid(this.nodenum);
		this.nodenum ++;
		if (cur.children() == null || cur.children().size() == 0) { 	
			return;
		}
		for (Element e : cur.children()) {
			DomTreeNode tmpnode = new DomTreeNode(curnode, e.tagName(), e.id(), e.className(), e.html(), e.ownText());
			curnode.addChild((DomTreeNode)tmpnode);
			this.addChildrenNode(tmpnode, e);
		}
	}
	
	public void addGoodNodes(DomTreeNode node) {
		if (node.getContent()==null || node.getContent().isEmpty()) return;
		Pattern p = Pattern.compile("[a-zA-Z0-9]");
		boolean hasAlphaNumeric = p.matcher(node.getContent()).find();
		if (! hasAlphaNumeric) return;
		this.goodNodes.add(node);
	}
	
	public List<DomTreeNode> getGoodNodes() {
		return this.goodNodes;
	}
	
	public void setGoodNodes(List<DomTreeNode> newgood) {
		this.goodNodes = newgood;
	}
	
	public void traverse() {
		this.root.printSubTree();
	}
	
	public List<BaseTreeNode> getLeafNodes() {
		return this.root.getLeafNodes();
	}
	
	public List<BaseTreeNode> getNodes() {
		return this.root.getNodes();
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		//DomTree domtree = new DomTree("amazon2.html");
		//domtree.traverse();
		//for (BaseTreeNode node : domtree.root.getLeafNodes()) {
			//((DomTreeNode)node).print();
		//}
		String s = "(((0";
		Pattern p = Pattern.compile("[a-zA-Z0-9]");
		boolean hasSpecialChar = p.matcher(s).find();
		System.out.println(hasSpecialChar);

	}

}
