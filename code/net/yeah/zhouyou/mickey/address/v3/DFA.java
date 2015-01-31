package net.yeah.zhouyou.mickey.address.v3;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import net.yeah.zhouyou.mickey.address.v3.tree.AbstractLeaf;
import net.yeah.zhouyou.mickey.address.v3.tree.AcceptLeaf;
import net.yeah.zhouyou.mickey.address.v3.tree.INode;
import net.yeah.zhouyou.mickey.address.v3.tree.Leaf;

public class DFA implements Serializable {

	private static final long serialVersionUID = -5466833365291742215L;

	private DFAState startState;

	private DFA(DFAState start) {
		this.startState = start;
	}

	public static DFA create(INode tree) {
		// 龙书3：算法3.36
		Dstates dstates = new Dstates();
		Dstate start = new Dstate(tree.firstpos());
		dstates.addIfNotContains(start);
		for (Dstate s : dstates) {
			s.marked = true;

			for (Map.Entry<Character, List<Leaf>> e : s.allInputs().entrySet()) {
				char a = e.getKey();
				Set<AbstractLeaf> u = new HashSet<AbstractLeaf>();
				for (Leaf leaf : e.getValue()) {
					u.addAll(leaf.followpos());
				}
				if (u.size() > 0) {
					Dstate du = new Dstate(u);
					dstates.addIfNotContains(du);
					s.addPath(a, du);
				}
			}
		}

		Map<Dstate, DFAState> map = new HashMap<Dstate, DFAState>();
		for (Dstate s : dstates.states) {
			DFAState ds = map.get(s);
			if (ds == null) {
				ds = new DFAState();
				ds.key = s.acceptKey();
				map.put(s, ds);
			}
			for (Map.Entry<Character, Dstate> e : s.path.entrySet()) {
				DFAState ds2 = map.get(e.getValue());
				if (ds2 == null) {
					ds2 = new DFAState();
					ds2.key = e.getValue().acceptKey();
					map.put(e.getValue(), ds2);
				}
				ds.path.put(e.getKey(), ds2);
			}
		}
		return new DFA(map.get(start));
	}

	public List<String> scan(String s) {
		DFAState currentState = this.startState;
		int currentIdx = 0;

		DFAState currentAccepted = null;
		int currentAcceptedIdx = 0;

		int fromIdx = 0;

		char[] bl = s.toCharArray();
		List<String> res = new ArrayList<String>();
		for (int len = bl.length; currentIdx < len; ++currentIdx) {
			char a = bl[currentIdx];
			currentState = currentState.tran(a);
			if (currentState == null || currentIdx + 1 == len) {
				if (currentState != null && currentIdx + 1 == len && currentState.isAccepted()) {
					if (!res.contains(currentState.key))
						res.add(currentState.key);
				} else if (currentAccepted != null) {
					if (!res.contains(currentAccepted.key))
						res.add(currentAccepted.key);

					fromIdx = currentAcceptedIdx + 1;
					currentAccepted = null;
					currentIdx = currentAcceptedIdx;
				} else {
					currentIdx = fromIdx;
					fromIdx = fromIdx + 1;
				}
				currentState = this.startState;
			} else if (currentState.isAccepted()) {
				currentAccepted = currentState;
				currentAcceptedIdx = currentIdx;
			}
		}
		return res;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		Set<DFAState> nss = new HashSet<DFAState>();
		Deque<DFAState> stack = new ArrayDeque<DFAState>();
		stack.push(this.startState);

		while (!stack.isEmpty()) {
			DFAState ds = stack.pollFirst();
			nss.add(ds);
			sb.append(ds.createString());
			for (DFAState ds2 : ds.path.values()) {
				if (nss.contains(ds2) == false)
					stack.push(ds2);
			}
		}

		return sb.toString();
	}
}

class Dstates implements Iterable<Dstate> {
	Set<Dstate> states = new HashSet<Dstate>();

	void addIfNotContains(Dstate s) {
		if (!states.contains(s)) {
			states.add(s);
		}
	}

	@Override
	public Iterator<Dstate> iterator() {
		return new Iterator<Dstate>() {

			List<Dstate> cache = new ArrayList<Dstate>();

			@Override
			public boolean hasNext() {
				if (!cache.isEmpty()) {
					return true;
				}
				for (Dstate s : states) {
					if (!s.marked) {
						cache.add(s);
					}
				}
				return !cache.isEmpty();
			}

			@Override
			public Dstate next() {
				return cache.remove(cache.size() - 1);
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}
}

class Dstate {
	boolean marked;
	Set<AbstractLeaf> nodes;
	Map<Character, Dstate> path = new HashMap<Character, Dstate>();

	Dstate(Set<AbstractLeaf> nodes) {
		this.nodes = nodes;
	}

	Map<Character, List<Leaf>> allInputs() {
		Map<Character, List<Leaf>> ai = new HashMap<Character, List<Leaf>>();
		for (AbstractLeaf n : nodes) {
			if (n instanceof Leaf) {
				char input = ((Leaf) n).getInput();
				List<Leaf> leafList = ai.get(input);
				if (leafList == null) {
					leafList = new ArrayList<Leaf>();
					ai.put(input, leafList);
				}
				leafList.add((Leaf) n);
			}
		}
		return ai;
	}

	void addPath(char c, Dstate s) {
		path.put(c, s);
	}

	String acceptKey() {
		for (AbstractLeaf n : nodes) {
			if (n instanceof AcceptLeaf) {
				return ((AcceptLeaf) n).getKey();
			}
		}
		return null;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((nodes == null) ? 0 : nodes.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Dstate other = (Dstate) obj;
		if (nodes == null) {
			if (other.nodes != null)
				return false;
		} else if (!nodes.equals(other.nodes))
			return false;
		return true;
	}

}

class DFAState implements Serializable {

	private static final long serialVersionUID = -6987881824989057409L;

	Map<Character, DFAState> path = new HashMap<Character, DFAState>();;
	String key;

	DFAState tran(char c) {
		return path.get(c);
	}

	boolean isAccepted() {
		return key != null;
	}

	@Override
	public String toString() {
		String ss = super.toString();
		ss = ss.substring(ss.lastIndexOf('.') + 1);
		if (this.key == null)
			return ss;
		return ss + "{" + this.key + "}";
	}

	public String createString() {
		StringBuilder sb = new StringBuilder();
		sb.append('[').append(toString()).append("]\n");
		for (Entry<Character, DFAState> e : this.path.entrySet()) {
			sb.append('\t').append(':').append(e.getKey() == null ? "_e" : e.getKey()).append("->")
					.append(e.getValue()).append('\n');
		}
		return sb.toString();
	}
}
