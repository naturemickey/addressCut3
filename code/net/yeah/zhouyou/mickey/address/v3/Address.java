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
			ct = DataCache.idMap.get(ct.getId()).get(0);
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
		if (ct1.getLevel() == ct2.getLevel() || ct1.getName().equals(ct2.getName())) {
			// 1.同一级别不可能是上下级关系。
			// 2.同一名称则以高级的为准，不再识别上下级关系。
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
		return this.originalAddress + '\n' + this.addrList.toString() + '\n' + toString(this, -1);
	}

	private static String toString(Address addr, int step) {
		StringBuilder sb = new StringBuilder();
		if (step >= 0) {
			StringBuilder blank = new StringBuilder();
			for (int i = 0; i < step; ++i) {
				blank.append("    ");
			}
			sb.append(blank).append(addr.value).append('\n');
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
		return breakTreeRecu();
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
				for (List<Address> fl : c.breakTree()) {
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
		for (List<Address> l : this.breakTree()) {
			double sc = calScope(l);
			if (res[0] == null || sc > ((Number) res[0]).doubleValue()) {
				Object[] fs = fixToToken(l);
				res[0] = sc;
				res[1] = fs[0];
				res[2] = fs[1];
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
			double sc = calScope(l);
			System.out.print(sc);
			for (Address a : l) {
				System.out.print(a.value);
			}
			System.out.println();
		}
	}

	/**
	 * <pre>
	 * @param al
	 * @return scope值
	 * </pre>
	 */
	private double calScope(List<Address> al) {
		double s = 0;
		Set<String> set = new HashSet<String>();
		Address p = null;
		for (int i = 0, len = al.size(); i < len; ++i) {
			Address a = al.get(i);

			if (set.contains(a.addrReal))
				continue;
			else
				set.add(a.addrReal);

			CityToken ct = a.value;

			if (p == null)
				p = a;
			else if (ct.getLevel() - p.value.getLevel() > 2) // 间隔超过两级的认为过于不准。
				return 0;

			double x = 0;
			switch (ct.getLevel()) {
			case 1:
			case 2:
				x = a.addrReal.equals(ct.getName()) ? 10 : 8;
				break;
			case 3:
				x = a.addrReal.equals(ct.getName()) ? 6 : 4.5;
				break;
			case 4:
				x = a.addrReal.equals(ct.getName()) ? 3 : 2;
				break;
			case 5:
				x = a.addrReal.equals(ct.getName()) ? 1.5 : 1;
				break;
			default:
				continue;
			}
			s += x;
			double idx = this.addrList.indexOf(a.addrReal) * 2;
			if (idx > x)
				idx = x;
			s -= idx;
		}
		return s;
	}

	@SuppressWarnings("unchecked")
	public Info info() {
		Info res = new Info();
		Object[] cr = this.getCutRes();
		List<CityToken> ctList = (List<CityToken>) cr[1];
		res.detailAddress = (String) cr[2];
		if (ctList != null)
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
