package net.yeah.zhouyou.mickey.address.v3.tree;

import java.util.Set;

import net.yeah.zhouyou.mickey.address.v3.CollUtils;

public class Node extends AbstractNode implements INode {

	public static enum Type {
		// 对于地址的基础数据，每个地址内部只有CAT，多个地址之间是OR，不存在kleen closure类型。
		CAT('+'), OR('|');

		final char sign;

		private Type(char c) {
			this.sign = c;
		}
	};

	private Type type;
	private INode left;
	private INode right;

	public Node(Type type, INode left, INode right) {
		this.type = type;
		this.left = left;
		this.right = right;

		this.left.setParent(this);
		this.right.setParent(this);
	}

	public Type getType() {
		return type;
	}

	public INode getLeft() {
		return left;
	}

	public INode getRight() {
		return right;
	}

	public String createString() {
		return createString(0);
	}

	private String createString(int level) {
		StringBuilder sb = new StringBuilder();
		String bs = blks(level);
		sb.append(bs).append("Node[").append(this.type).append(']').append(":\n");
		sb.append(childToString(left, level)).append('\n');
		sb.append(childToString(right, level));
		return sb.toString();
	}

	private String childToString(INode node, int level) {
		StringBuilder sb = new StringBuilder();
		int lvl = level + 1;
		if (node instanceof Node) {
			sb.append(((Node) node).createString(lvl));
		} else {
			sb.append(blks(lvl)).append(node.createString());
		}
		return sb.toString();
	}

	private String blks(int c) {
		String stp = "  ";
		StringBuilder sb = new StringBuilder(c * stp.length());
		while (--c >= 0)
			sb.append(stp);
		return sb.toString();
	}

	@Override
	public Set<AbstractLeaf> firstpos() {
		if (this.type == Type.CAT) {
			return left.firstpos();
		} else {
			Set<AbstractLeaf> lf = left.firstpos();
			Set<AbstractLeaf> rf = right.firstpos();
			return CollUtils.union(lf, rf);
		}
	}

}
