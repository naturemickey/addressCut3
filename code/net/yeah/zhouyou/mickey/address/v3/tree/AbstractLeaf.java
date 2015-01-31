package net.yeah.zhouyou.mickey.address.v3.tree;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public abstract class AbstractLeaf extends AbstractNode {

	public Set<AbstractLeaf> followpos() {
		Node parent = (Node) this.getParent();
		INode current = this;

		while (parent != null && (parent.getType() == Node.Type.OR || parent.getLeft() != current)) {
			current = parent;
			parent = (Node) current.getParent();
		}

		// 以下一行代码成立，必须先证明：this必然在parent.getLeft().lastPos()中。
		return (parent != null) ? parent.getRight().firstpos() : Collections.<AbstractLeaf> emptySet();
	}

	private Set<AbstractLeaf> fp = new HashSet<AbstractLeaf>();
	{
		// 叶子节点的firstpos中一定只有自己，所以这里就直接在构造中加好。
		fp.add(this);
	}

	@Override
	final public Set<AbstractLeaf> firstpos() {
		return fp;
	}

	@Override
	public String toString() {
		String ss = super.toString();
		return ss.substring(ss.lastIndexOf('.') + 1);
	}

	@Override
	public String createString() {
		return this.toString() + psToString() + ".followpos:" + this.followpos();
	}

	abstract protected String psToString();
}
