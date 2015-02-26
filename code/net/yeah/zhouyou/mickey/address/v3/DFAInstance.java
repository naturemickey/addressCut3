package net.yeah.zhouyou.mickey.address.v3;


class DFAInstance {

	public static final DFA dfa;

	static {
		long initStart = System.currentTimeMillis();
		dfa = DFA.create(DataCache.nameMap.keySet());
		System.out.println("DFA init cost:" + (System.currentTimeMillis() - initStart));
	}

}
