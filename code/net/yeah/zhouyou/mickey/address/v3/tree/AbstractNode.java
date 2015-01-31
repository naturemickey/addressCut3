package net.yeah.zhouyou.mickey.address.v3.tree;

public abstract class AbstractNode implements INode {

	private INode parent;

	@Override
	public void setParent(INode node) {
		// 以下两行仅为防止不必要的重复，算法完成之后即没有必要。
		// if (this.parent != null)
		// throw new RuntimeException();
		this.parent = node;
	}

	@Override
	public INode getParent() {
		return this.parent;
	}

	@Override
	public String createString() {
		return this.toString();
	}
}
