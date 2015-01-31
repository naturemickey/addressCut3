package net.yeah.zhouyou.mickey.address.v3;

import net.yeah.zhouyou.mickey.address.v3.tree.NodeCreater;

public class DFAInstance {

	public static final DFA dfa;
	static {
		long initStart = System.currentTimeMillis();

		String cacheName = "dfaObj_v3.cache";
		DFA fa = SerializeUtil.read(cacheName);
		if (fa == null) {
			dfa = DFA.create(NodeCreater.create(DataCache.nameMap.keySet()));
			SerializeUtil.write(dfa, cacheName);
		} else {
			dfa = fa;
		}
		System.out.println("DFA init cost:" + (System.currentTimeMillis() - initStart));
	}

}
