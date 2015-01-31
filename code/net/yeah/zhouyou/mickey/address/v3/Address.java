package net.yeah.zhouyou.mickey.address.v3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Address {

	private String originalAddress;

	public String getOriginalAddress() {
		return originalAddress;
	}

	private CityToken value;
	private String addrReal;
	private List<String> addrList;
	private Address parent;
	private List<Address> children = new ArrayList<Address>();

	private Address(CityToken ct, String addrStr, Address parent) {
		this.value = ct;
		this.addrReal = addrStr;
		this.parent = parent;
	}

	public Address(String originalAddress, List<String> addrList) {
		this.originalAddress = originalAddress == null ? "" : originalAddress;
		this.addrList = addrList == null ? Collections.<String> emptyList() : addrList;
		for (String addr : this.addrList) {
			this.add(addr);
		}
	}

	private void add(String addrStr) {
		for (CityToken ct : DataCache.nameMap.get(addrStr)) {
			this.add(ct, addrStr);
		}
	}

	private void add(CityToken ct, String addrStr) {
		boolean hasRelationship = false;
		for (int i = 0, len = children.size(); i < len; ++i) {
			Address addr = children.get(i);
			if (!ct.getId().equals(addr.value.getId())) {
				Integer relationship = getRelationship(ct, addr.value);
				if (relationship != null && relationship != 0) {
					hasRelationship = true;
					if (relationship > 0) {
						Address addrNew = new Address(ct, addrStr, this);
						children.set(i, addrNew);
						addr.parent = addrNew;
						addrNew.children.add(addr);
					} else {
						addr.add(ct, addrStr);
					}
					break;
				}
			}
		}
		if (!hasRelationship) {
			children.add(new Address(ct, addrStr, this));
		}
	}

	private Integer getRelationship(CityToken ct1, CityToken ct2) {
		if (ct1.getLevel() == ct2.getLevel()) {
			return 0;// 同一级别不可能是上下级关系。
		}
		if (ct1.getLevel() > ct2.getLevel()) {
			return -1 * getRelationship(ct2, ct1);
		}
		while (ct1.getLevel() < ct2.getLevel() && ct2.parent != null) {
			if (ct2.parent.getId().longValue() == ct1.getId().longValue())
				return 1;
			ct2 = ct2.parent;
		}
		return 0;
	}

	@Override
	public String toString() {
		return this.originalAddress + '\n' + this.addrList.toString() + '\n' + toString(this, -1);
	}

	private static String toString(Address addr, int step) {
		StringBuilder sb = new StringBuilder();
		if (step >= 0) {
			StringBuilder blank = new StringBuilder();
			for(int i = 0; i < step; ++i) {
				blank.append("    ");
			}
			Stream.of(blank, addr.value.toString(), "\n").forEach(sb::append);
		}
		addr.children.stream().map(ac -> toString(ac, step + 1)).forEach(sb::append);
		return sb.toString();
	}

	private List<List<Address>> breakTree() {
		List<List<Address>> res = new ArrayList<>();
		if (this.children.size() == 0) {
			List<Address> l = new ArrayList<>();
			if (this.value != null)
				l.add(this);
			res.add(l);
		} else {
			res = this.children.stream().flatMap(c -> c.breakTree().stream()).map(l -> {
				if (this.value != null)
					l.add(0, this);
				return l;
			}).collect(Collectors.toList());
		}
		return res;
	}

	/**
	 * <pre>
	 * @return
	 * [0]: 网点
	 * [1]: scope
	 * [2]: 标准地址
	 * [3]: 标准地址之后的未识别部分
	 * </pre>
	 */
	public Object[] getCutRes() {
		Object[] res1 = new Object[4];
		Object[] res2 = new Object[4];
		this.breakTree().forEach(l -> {
			Object[] sc = calScope(l);
			Object[] res = (sc[1] != null) ? res1 : res2;
			if (res[1] == null || ((Number) sc[0]).doubleValue() > ((Number) res[1]).doubleValue()) {
				Object[] fs = fixToToken(l);
				res[0] = sc[1];
				res[1] = sc[0];
				res[2] = fs[0];
				res[3] = fs[1];
			}
		});
		return res1[0] == null ? res2 : res1;
	}

	private Object[] fixToToken(List<Address> addrList) {
		String detailAddress = originalAddress;
		if (addrList.size() == 0)
			return new Object[] { Collections.emptyList(), detailAddress };
		Address addr = addrList.get(addrList.size() - 1);
		CityToken ct = DataCache.idMap.get(addr.value.getId()).get(0);
		String lastRealAddr = addr.addrReal;
		String lastStandardAddr = ct.getName();
		List<CityToken> ctList = new ArrayList<>();
		while (ct != null) {
			ctList.add(0, ct);
			ct = ct.parent;
		}
		if (lastRealAddr != null || lastStandardAddr != null) {
			detailAddress = subOrigAddr(lastRealAddr, lastStandardAddr);
		}
		return new Object[] { ctList, detailAddress };
	}

	private String subOrigAddr(String addr, String stdAddr) {
		int idx = -1;
		if (stdAddr != null)
			idx = originalAddress.lastIndexOf(stdAddr);
		if (idx < 0 && addr != null)
			idx = originalAddress.lastIndexOf(addr);
		if (idx > 0)
			return originalAddress.substring(idx);
		return originalAddress;
	}

	public void printBreakTree() {
		this.breakTree().forEach(l -> {
			Object[] sc = calScope(l);
			System.out.print((sc[1] == null ? "_" : sc[1]) + ":" + sc[0] + ":");
			l.forEach(a -> {
				System.out.print(a.value);
			});
			System.out.println();
		});
	}

	/**
	 * <pre>
	 * @param al
	 * @return
	 * [0] 为scope值
	 * [1] 为识别到的网点
	 * </pre>
	 */
	private Object[] calScope(List<Address> al) {
		Object[] res = new Object[2];
		List<Number[]> wordScopeList = new ArrayList<>();

		int wordLen = al.stream().map(a -> {
			Address addrPre = a.parent;
			if (a.value.getCode() != null && a.value.getCode().length() > 0) {
				res[1] = a.value.getCode();
			}
			double wordCountScope;
			if (a.addrReal.equals(a.value.getName()))
				wordCountScope = 1D;
			else {
				int len = a.addrReal.length();
				wordCountScope = 0.8 / 6 * len + 0.2;
				wordCountScope = wordCountScope > 1 ? 1 : wordCountScope;
			}
			double stepScope = 1;
			if (addrPre != null && addrPre.value != null) {
				int step = -1;
				CityToken ct = a.value;
				while (ct != null && !ct.getId().equals(addrPre.value.getId())) {
					step += 1;
					ct = ct.parent;
				}
				stepScope = (step < 0 || step > 10) ? 1 : 1D - step / 10D;
			}
			int level = a.value.getLevel();
			if (level < 2)
				level = 2;
			else if (level > 6)
				level = 6;
			int weigh = 13 - level;
			weigh = weigh < 0 || weigh > 13 ? 0 : weigh;
			wordScopeList.add(new Number[] { wordCountScope, weigh });
			wordScopeList.add(new Number[] { stepScope, weigh });
			return a.addrReal;
		}).mapToInt(String::length).sum();

		Number[] wsNs = wordScopeList.stream().reduce(new Number[] { 0, 0 }, (ns, ns2) -> {
			ns[0] = ns[0].doubleValue() + ns2[0].doubleValue() * ns2[1].intValue();
			ns[1] = ns[1].intValue() + ns2[1].intValue();
			return ns;
		});

		int div = wsNs[1].intValue();
		double wordScope = wsNs[0].doubleValue() / (div == 0 ? 1 : div);

		double r = ((double) wordLen) / this.originalAddress.length();
		double rScope = Math.sin(r * Math.PI / 2);

		int addrCount = al.size();

		double addrCountScope = (addrCount > 5) ? 0.25 : (0.1 * (6 - addrCount) + 0.25);

		res[0] = wordScope * (1 - addrCountScope) + rScope * addrCountScope;
		return res;
	}
}
