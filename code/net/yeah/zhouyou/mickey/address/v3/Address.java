package net.yeah.zhouyou.mickey.address.v3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class Address {

	private String originalAddress;

	public String getOriginalAddress() {
		return originalAddress;
	}

	private CityToken value;
	private String addrReal;
	private List<String> addrList;
	private List<Address> children = new ArrayList<Address>();
	private Address parent;

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
			ct = DataCache.idMap.get(ct.getId()).get(0);
			this.add(ct, addrStr);
		}
	}

	private void add(CityToken ct, String addrStr) {
		boolean hasRelationship = false;
		for (int i = 0, len = children.size(); i < len; ++i) {
			Address addr = children.get(i);
			if (ct.getId().equals(addr.value.getId())) {
				hasRelationship = true;
				if (addrStr.equals(ct.getName())) {
					addr.value = ct;
					addr.addrReal = addrStr;
				}
				break;
			} else {
				int relationship = getRelationship(ct, addr.value);
				if (relationship != 0) {
					hasRelationship = true;
					if (relationship > 0) {
						Address addrNew = new Address(ct, addrStr, this);
						children.set(i, addrNew);
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

	private int getRelationship(CityToken ct1, CityToken ct2) {
		if (ct1.getLevel() == ct2.getLevel() || ct1.getName().equals(ct2.getName())) {
			// 1.同一级别不可能是上下级关系。
			// 2.名字相同的以级别高的为准。
			return 0;
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
		return this.originalAddress + '\n' + (this.addrList == null ? "[]" : this.addrList.toString()) + '\n'
				+ toString(this, -1);
	}

	private static String toString(Address addr, int step) {
		StringBuilder sb = new StringBuilder();
		if (step >= 0) {
			StringBuilder blank = new StringBuilder();
			for (int i = 0; i < step; ++i) {
				blank.append("    ");
			}
			sb.append(blank).append(addr.addrReal).append(addr.value).append('\n');
		}
		for (Address ac : addr.children) {
			sb.append(toString(ac, step + 1));
		}
		return sb.toString();
	}

	private List<List<Address>> breakTree() {
		Iterator<Address> it = this.children.iterator();
		while (it.hasNext()) {
			Address a = it.next();
			// 第一个节点就是4级以下的去掉。
			if (a.value == null || a.value.getLevel() >= 4)
				it.remove();
		}
		List<List<Address>> res = breakTreeRecu();
		Iterator<List<Address>> resIt = res.iterator();
		while (resIt.hasNext()) {
			Set<String> ns = new HashSet<String>();
			it = resIt.next().iterator();
			while (it.hasNext()) {
				String ar = it.next().addrReal;
				if (!ns.add(ar))
					it.remove();
			}
		}
		return res;
	}

	private List<List<Address>> breakTreeRecu() {
		List<List<Address>> res = new ArrayList<List<Address>>();
		if (this.children.size() == 0) {
			List<Address> l = new ArrayList<Address>();
			if (this.value != null)
				l.add(this);
			res.add(l);
		} else {
			for (Address c : this.children) {
				for (List<Address> fl : c.breakTreeRecu()) {
					if (this.value != null)
						fl.add(0, this);
					res.add(fl);
				}
			}
		}
		return res;
	}

	/**
	 * <pre>
	 * @return
	 * [0]: scope
	 * [1]: 标准地址
	 * [2]: 标准地址之后的未识别部分
	 * </pre>
	 */
	public Object[] getCutRes() {
		Object[] res = new Object[4];
		Object[] fs = fixToToken(choose(this.breakTree(), 0));
		res[1] = fs[0];
		res[2] = fs[1];
		return res;
	}

	private List<Address> choose(List<List<Address>> ll, int idx) {
		if (ll.size() == 0)
			return Collections.emptyList();
		if (ll.size() == 1)
			return ll.get(0);
		List<List<Address>> res1 = new ArrayList<List<Address>>();
		int level = Integer.MAX_VALUE;
		for (List<Address> l : ll) {
			if (l.size() > idx) {
				Address a = l.get(idx);
				if (a.value != null) {
					if (a.value.getLevel() < level) {
						res1.clear();
						level = a.value.getLevel();
						res1.add(l);
					} else if (a.value.getLevel() == level) {
						res1.add(l);
					}
				}
			}
		}

		// 测试时使用
		// if (level == Integer.MAX_VALUE)
		// throw new RuntimeException();

		if (res1.size() == 1)
			return res1.get(0);

		boolean isStd = false;
		boolean toRecu = false;
		List<List<Address>> res2 = new ArrayList<List<Address>>();
		for (List<Address> l : res1) {
			Address a = l.get(idx);
			if (a.value.getLevel() < 2) {
				res2 = res1;
				for (List<Address> l2 : res1) {
					toRecu = toRecu || l2.size() > idx + 1;
				}
				break;
			}
			if (isStd) {
				if (a.value.getName().length() == a.addrReal.length()) {
					res2.add(l);
					toRecu = toRecu || l.size() > idx + 1;
				}
			} else {
				if (a.value.getName().length() == a.addrReal.length()) {
					res2.clear();
					toRecu = false;
					isStd = true;
				}
				res2.add(l);
				toRecu = toRecu || l.size() > idx + 1;
			}
		}
		if (toRecu)
			return choose(res2, idx + 1);
		if (res2.size() == 0)
			return Collections.emptyList();
		if (res2.size() == 1)
			return res2.get(0);
		List<Address> res = null;
		for (List<Address> l : res2) {
			if (res == null)
				res = l;
			else {
				Address a1 = res.get(idx);
				Address a2 = l.get(idx);
				if (this.addrList.indexOf(a2.addrReal) < this.addrList.indexOf(a1.addrReal))
					res = l;
			}
		}
		return res;
	}

	private Object[] fixToToken(List<Address> addrList) {
		String detailAddress = originalAddress;
		if (addrList.size() == 0)
			return new Object[] { Collections.emptyList(), detailAddress };
		Address addr = addrList.get(addrList.size() - 1);
		CityToken ct = DataCache.idMap.get(addr.value.getId()).get(0);
		String lastRealAddr = addr.addrReal;
		String lastStandardAddr = ct.getName();
		List<CityToken> ctList = new ArrayList<CityToken>();
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
		for (List<Address> l : this.breakTree()) {
			// double sc = calScope(l);
			// System.out.print(sc);
			for (Address a : l) {
				System.out.print(a.value);
			}
			System.out.println();
		}
	}


	private static final Set<String> dCity;
	private static final Set<String> hmtCity;

	static {
		dCity = new HashSet<String>();
		dCity.add("北京");
		dCity.add("上海");
		dCity.add("天津");
		dCity.add("重庆");

		hmtCity = new HashSet<String>();
		hmtCity.add("香港");
		hmtCity.add("澳門");
		// hmtCity.add("臺灣");
	}
	@SuppressWarnings("unchecked")
	public Info info() {
		Info res = new Info();
		Object[] cr = this.getCutRes();
		List<CityToken> ctList = (List<CityToken>) cr[1];
		res.detailAddress = (String) cr[2];
		if (ctList != null){
			for (CityToken ct : ctList) {
				switch (ct.getLevel()) {
				case 1:
					res.provinceAddress = ct.getName();
					break;
				case 2:
					res.cityAddress = ct.getName();
					break;
				case 3:
					res.areaAddress = ct.getName();
					break;
				case 4:
					res.townAddress = ct.getName();
					break;
				}
			}
			if(ctList.size() == 0) {
				if(dCity.contains(res.provinceAddress)){
					res.cityAddress = res.provinceAddress + '市';
				}else if(hmtCity.contains(res.provinceAddress)){
					res.cityAddress = res.provinceAddress;
				}
			}
		}
		res.originalAddress = this.originalAddress;
		return res;
	}

	public static class Info {
		private String provinceAddress; // 省 level 1
		private String cityAddress; // 市 level 2
		private String areaAddress; // 区 level 3
		private String townAddress; // 镇/街道办 level 4
		private String originalAddress;
		private String detailAddress;

		public String getProvinceAddress() {
			return provinceAddress;
		}

		public String getCityAddress() {
			return cityAddress;
		}

		public String getAreaAddress() {
			return areaAddress;
		}

		public String getTownAddress() {
			return townAddress;
		}

		public String getOriginalAddress() {
			return originalAddress;
		}

		public String getDetailAddress() {
			return detailAddress;
		}
	}
}
