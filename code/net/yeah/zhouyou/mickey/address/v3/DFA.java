package net.yeah.zhouyou.mickey.address.v3;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class DFA {

	private DFA() {
	}

	private DFAState startState = new DFAState();

	private DFA(Iterable<String> names) {
		for (String name : names) {
			DFAState currentState = this.startState;
			for (char c : name.toCharArray()) {
				currentState = currentState.addPath(c);
			}
			currentState.setName(name);
		}
	}

	private DFA(String... names) {
		this(Arrays.asList(names));
	}

	public static DFA create(Iterable<String> names) {
		return new DFA(names);
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
					if (!res.contains(currentState.getName()))
						res.add(currentState.getName());
				} else if (currentAccepted != null) {
					if (!res.contains(currentAccepted.getName()))
						res.add(currentAccepted.getName());

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
			DFAState ns = stack.pollFirst();
			nss.add(ns);
			sb.append(ns.createString());
			for (DFAState ns2 : ns.getPath().values()) {
				if (nss.contains(ns2) == false)
					stack.push(ns2);
			}
		}

		return sb.toString();
	}

	public static void main(String[] args) {
		System.out.println(new DFA("上海市", "上海", "奉贤区"));
	}
}
