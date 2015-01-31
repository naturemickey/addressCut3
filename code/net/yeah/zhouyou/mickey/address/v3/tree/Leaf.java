package net.yeah.zhouyou.mickey.address.v3.tree;


public final class Leaf extends AbstractLeaf implements INode {

	private char input;

	public Leaf(char input) {
		this.input = input;
	}

	public char getInput() {
		return input;
	}

	@Override
	public String psToString() {
		return "[input=" + input + "]";
	}

}
