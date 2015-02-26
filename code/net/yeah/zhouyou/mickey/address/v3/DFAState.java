package net.yeah.zhouyou.mickey.address.v3;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

class DFAState {
	private String name;
	private Map<Character, DFAState> path = new HashMap<Character, DFAState>();

	public boolean isAccepted() {
		return name != null;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public DFAState tran(Character a) {
		return path.get(a);
	}

	public DFAState addPath(Character c) {
		DFAState state = tran(c);
		if (state == null) {
			state = new DFAState();
			path.put(c, state);
		}
		return state;
	}

	public Map<Character, DFAState> getPath() {
		return Collections.unmodifiableMap(this.path);
	}

	public String createString() {
		StringBuilder sb = new StringBuilder();
		sb.append('[').append(super.toString()).append("]\n");
		for (Entry<Character, DFAState> e : this.path.entrySet()) {
			sb.append('\t').append(':').append(e.getKey() == null ? "_e" : e.getKey()).append("->")
					.append(e.getValue()).append('\n');
		}
		return sb.toString();
	}
}
