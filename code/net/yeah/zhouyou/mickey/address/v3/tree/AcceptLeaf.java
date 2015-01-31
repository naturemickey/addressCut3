package net.yeah.zhouyou.mickey.address.v3.tree;

import java.util.Collections;
import java.util.Set;

public final class AcceptLeaf extends AbstractLeaf implements INode {

	private String key;

	public AcceptLeaf(String key) {
		this.key = key;
	}

	public String getKey() {
		return key;
	}

	@Override
	public String psToString() {
		return "[key=" + key + "]";
	}

	@Override
	public Set<AbstractLeaf> followpos() {
		return Collections.emptySet();
	}
}
