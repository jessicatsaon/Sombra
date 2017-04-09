package edu.cu.cs.Sombra.Schema;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.cu.cs.Sombra.DomTree.DomTree;
import edu.cu.cs.Sombra.DomTree.DomTreeNode;
import edu.cu.cs.Sombra.Tree.BaseTreeNode;
import edu.cu.cs.Sombra.VisualTree.VisualTree;
import edu.cu.cs.Sombra.VisualTree.VisualTreeNode;

public class PageStructure {

	private VisualTree VTree;
	private DomTree DomTree;
	public Set<DomTreeNode> nameNodes;
	public Set<DomTreeNode> valueNodes;
	public Map<DomTreeNode, String> V2N;

	public PageStructure(String htmlfile) {
		this.DomTree = new DomTree(htmlfile);
		this.VTree = VisualTree.getVisualTree("modified_" + htmlfile);
		this.nameNodes = new HashSet<DomTreeNode>();
		this.valueNodes = new HashSet<DomTreeNode>();
		this.V2N = new HashMap<DomTreeNode, String>();
		this.treeAlign();
	}

	/*
	 * Align DOM Tree and Visual Tree
	 */
	private void treeAlign() {
		List<BaseTreeNode> domLeafNodes = this.DomTree.getNodes();
		List<BaseTreeNode> vLeafNodes = this.VTree.getLeafNodes();
		for (BaseTreeNode domNode : domLeafNodes) {
			int sombraid = ((DomTreeNode) domNode).getSombraid();
			for (BaseTreeNode vNode : vLeafNodes) {
				List<Integer> list = ((VisualTreeNode) vNode).getSombraIds();
				if (list.contains(sombraid)) {
					((DomTreeNode) domNode).setVPath(((VisualTreeNode) vNode).getID());
					((DomTreeNode) domNode).setVWeight(
							((VisualTreeNode) vNode).getRectHeight() * ((VisualTreeNode) vNode).getRectWidth());
					this.DomTree.addGoodNodes((DomTreeNode) domNode);
					// System.out.println(((DomTreeNode) domNode).getSRC());
					break;
				}
			}
		}
		System.out.println(this.DomTree.getGoodNodes().size());
	}

	public DomTree getDomTree() {
		return this.DomTree;
	}

	public Set<DomTreeNode> value2name() {
		// one-to-one
		List<DomTreeNode> valuelist = new ArrayList<DomTreeNode>();
		valuelist.addAll(valueNodes);
		Collections.sort(valuelist, new Comparator<DomTreeNode>() {
			public int compare(DomTreeNode node1, DomTreeNode node2) {
				if (node1.getSombraid() == node2.getSombraid())
					return 0;
				if (node1.getSombraid() > node2.getSombraid())
					return 1;
				return -1;
			}
		});
		Set<DomTreeNode> matched = this.value2name(valuelist, new HashSet<DomTreeNode>());

		// 2nd round
		valuelist.clear();
		for (DomTreeNode node : nameNodes) {
			if (!matched.contains(node)) {
				valuelist.add(node);
			} else {
				System.out.println(node.getContent());
			}
		}
		
		for (DomTreeNode node : valuelist) {
			nameNodes.remove(node);
		}
		valueNodes.addAll(valuelist);
		matched.addAll(this.value2name(valuelist, matched));
		nameNodes.addAll(matched);
		return matched;
		// if (V2N.containsKey(valuenode)) {
		// continue;
		// }
		// parent node
		/*
		 * DomTreeNode parent = (DomTreeNode) valuenode.getParent(); if
		 * (nameNodes.contains(parent) && !matched.contains(parent)) {
		 * V2N.put(valuenode, parent.getContent()); matched.add(parent); } else
		 * { // sibling nodes List<BaseTreeNode> siblings =
		 * valuenode.getSiblings(); for (BaseTreeNode sibling : siblings) {
		 * DomTreeNode domTreeNode = (DomTreeNode) sibling; if
		 * (nameNodes.contains(domTreeNode) && !matched.contains(domTreeNode)) {
		 * V2N.put(valuenode, domTreeNode.getContent()); matched.add(parent);
		 * break; } } } // default if (!V2N.containsKey(valuenode)) { String id
		 * = valuenode.getId(); DomTreeNode cur = valuenode; while
		 * (id.isEmpty()) { cur = (DomTreeNode) cur.getParent(); id =
		 * cur.getId(); } V2N.put(valuenode, id); matched.add(cur); }
		 */
	}

	public Set<DomTreeNode> value2name(List<DomTreeNode> valuelist, Set<DomTreeNode> matched) {
		// one-to-one
		Collections.sort(valuelist, new Comparator<DomTreeNode>() {
			public int compare(DomTreeNode node1, DomTreeNode node2) {
				if (node1.getSombraid() == node2.getSombraid())
					return 0;
				if (node1.getSombraid() > node2.getSombraid())
					return 1;
				return -1;
			}
		});
		for (DomTreeNode valuenode : valuelist) {
			// sombraid-based
			List<DomTreeNode> sombraList = DomTree.getGoodNodes();
			int pos = sombraList.indexOf(valuenode);
			int back = pos - 1;
			int ford = pos + 1;
			boolean ismatch = false;
			while (back >= 0 && ford < sombraList.size()) {
				DomTreeNode backCandidate = sombraList.get(back);
				DomTreeNode forwCandidate = sombraList.get(ford);
				if (backCandidate.getSombraid() - valuenode.getSombraid() 
						<= forwCandidate.getSombraid() - valuenode.getSombraid()) {
					if (backCandidate.getVPath().equals(valuenode.getVPath()) && nameNodes.contains(backCandidate)
							&& !matched.contains(backCandidate)) {
						matched.add(backCandidate);
						V2N.put(valuenode, backCandidate.getContent());
						ismatch = true;
						break;
					} 
					back--;
				} else {
					if (forwCandidate.getVPath().equals(valuenode.getVPath()) && nameNodes.contains(forwCandidate)
							&& !matched.contains(forwCandidate)) {
						matched.add(forwCandidate);
						V2N.put(valuenode, forwCandidate.getContent());
						ismatch = true;
						break;
					}
					ford++;
				}
			}
			while (!ismatch && back >= 0) {
				DomTreeNode backCandidate = sombraList.get(back);
				if (backCandidate.getVPath().equals(valuenode.getVPath()) && nameNodes.contains(backCandidate)
						&& !matched.contains(backCandidate)) {
					matched.add(backCandidate);
					V2N.put(valuenode, backCandidate.getContent());
					ismatch = true;
					break;
				} 
				back--;
			}
			while (!ismatch && ford < sombraList.size()) {
				DomTreeNode forwCandidate = sombraList.get(ford);
				if (forwCandidate.getVPath().equals(valuenode.getVPath()) && nameNodes.contains(forwCandidate)
						&& !matched.contains(forwCandidate)) {
					matched.add(forwCandidate);
					V2N.put(valuenode, forwCandidate.getContent());
					ismatch = true;
					break;
				}
				ford++;
			}
		}
		return matched;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		PageStructure test = new PageStructure("3.html");
		// test.getDomTree().traverse();
		/*
		 * for (VisualTreeNode vnode : test.getV2D().keySet()) {
		 * System.out.println(vnode.getSRC()); } for (BaseTreeNode node :
		 * test.DomTree.getLeafNodes()) {
		 * System.out.println(((DomTreeNode)node).getSRC()); }
		 */

	}
}
