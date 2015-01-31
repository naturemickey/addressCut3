package net.yeah.zhouyou.mickey.address.v3.tree;

import java.util.Set;

public interface INode {

	Set<AbstractLeaf> firstpos();

	// 对于简化的情况，不需要这个方法
	// Set<AbstractLeaf> lastpos();

	/**
	 * 只能set一次非空值。
	 */
	void setParent(INode node);

	INode getParent();

	/**
	 * 测试打印结果使用。
	 */
	String createString();
}
