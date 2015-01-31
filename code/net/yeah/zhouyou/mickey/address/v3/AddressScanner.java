package net.yeah.zhouyou.mickey.address.v3;

import static net.yeah.zhouyou.mickey.address.v3.DFAInstance.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class AddressScanner {

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
		//hmtCity.add("臺灣");
	}

	private static Pattern p = Pattern.compile("[\\s　]");

	public static Address scan(String txt) {
		return scan(txt, true);
	}

	public static Address scan(String txt, boolean exactMatch4Level) {
		// 中文地址中的空白是没有意义的
		txt = p.matcher(txt).replaceAll("");

		List<String> addrList = dfa.scan(txt);

		if (addrList.size() == 0)
			return new Address(txt);

		Address res = matchAddress(txt, addrList);

		if (res.getProvince() == null) {
			addrList = DFAInstance.getDFA123().scan(txt);
			if (addrList.size() == 0)
				return new Address(txt);

			res = matchAddress(txt, addrList);
		}

		if (exactMatch4Level && res.getTown() == null && res.getCity() != null) {
			// 用当前城市的DFA再重新匹配一遍
			DFA cityDFA = DFAInstance.getDFA(res.getCity().getId());
			addrList = cityDFA.scan(txt);
			// 此处不需要判断addrList是否为空，因为DFAInstance.dfa匹配不为空，则当前的小dfa的匹配一定不会为空
			res = matchAddress(txt, addrList);
		}

		if (res.getCityAddress() == null && res.getProvinceAddress() != null) {
			CityToken province = res.getProvince();
			if (dCity.contains(res.getProvinceAddress())) {
				// 当只识别到一个地址，并且是直辖市的时候
				List<CityToken> ctl = DataCache.nameMap.get(res.getProvinceAddress() + "市");
				for (int i = 0, n = ctl.size(); i < n; ++i) {
					CityToken ct = ctl.get(i);
					if (ct.getParentId() != null && ct.getParentId().equals(province.getId())) {
						setAddr(res, ct.getId(), null, addrList);
						break;
					}
				}
			} else if (hmtCity.contains(res.getProvinceAddress())) {
				List<CityToken> ctl = DataCache.nameMap.get(res.getProvinceAddress());
				for (int i = 0, n = ctl.size(); i < n; ++i) {
					CityToken ct = ctl.get(i);
					if (ct.getParentId() != null && ct.getParentId().equals(province.getId())) {
						setAddr(res, ct.getId(), null, addrList);
						break;
					}
				}
			}
		}

		return res;
	}

	private static void setAddr(Address res, Long id, String name, List<String> addrList) {
		List<CityToken> ctList = DataCache.idMap.get(id);
		res.setAddr(ctList.get(0), name);

		// 以下移除可能重复识别的地址名称，性能会有影响。
		for (int i = 0, len = ctList.size(); i < len; ++i) {
			String n = ctList.get(i).getName();
			if (n != null) {
				while (addrList.remove(n))
					;
			}
		}
	}

	private static Address matchAddress(String txt, List<String> addrList) {
		Address res = new Address(txt);
		CityToken top = null;
		CityToken bottom = null;

		while (!addrList.isEmpty()) {
			String name = addrList.remove(0);
			CityToken firstct = findTopCT(name);

			// 中国人写地址一般是“省”、“市”、“区”，对于BSP来说，商家也很少会省略“省”和“市”，如果直接写“区”以下的地址，则全国的地址重名的过多了。
			if (firstct.getLevel() > 3) {
				continue;
			}
			setAddr(res, firstct.getId(), name, addrList);
			top = firstct;
			bottom = firstct;
			break;
		}

		while (!addrList.isEmpty()) {
			String name = addrList.remove(0);

			List<CityToken> ccl = getccl(top, bottom, name);

			if (ccl.size() == 1) {
				CityToken ct = ccl.get(0);
				if (ct.getLevel() < top.getLevel()) {
					top = ct;
					setAddr(res, ct.getId(), name, addrList);
				} else {
					if (ct.getLevel() <= 2 // 当前识别到的为省级或市级
							|| bottom.getLevel() >= 3 // bottom在三级地址以下，则ct的范围比较小
							|| ct.getLevel() - bottom.getLevel() <= 2 // bottom为二级时，当前识别到的与bottom相差在两级以内
							|| name.length() >= 3 // 当前识别到的地址文字长度至少为3个字
							|| DataCache.idMap.get(ct.getId()).get(0).getName().endsWith(name) // 当前识别到的地址是一个全称
					) {
						bottom = ct;
						setAddr(res, ct.getId(), name, addrList);
					} else {
						bottom = getNextBottom(addrList, res, top, bottom, name, ccl);
					}
				}
			} else if (ccl.size() > 1) {
				bottom = getNextBottom(addrList, res, top, bottom, name, ccl);
			}
		}

		if (bottom != null) {
			// 把上级为空的补上.
			CityToken ct = bottom;
			while (ct.getLevel() > 0) {
				CityToken pct = ct.getParent();
				if (pct == null)
					break;
				if (res.getAddr(pct.getLevel()) == null) {
					setAddr(res, pct.getId(), null, addrList);
				}
				ct = pct;
			}
		}
		return res;
	}

	private static CityToken getNextBottom(List<String> addrList, Address res, CityToken top, CityToken bottom,
			String name, List<CityToken> ccl) {
		if (!addrList.isEmpty()) {
			String name2 = addrList.remove(0);
			for (int i = 0, n = ccl.size(); i < n; ++i) {
				CityToken cct = ccl.get(i);
				List<CityToken> ccl2 = getccl(top, cct, name2);
				if (!ccl2.isEmpty()) {
					// 两级关联之后，就随意取一个。因为此时至少已是三级地址，在同一个区或县内部的冲突就比较小了。
					CityToken ct2 = ccl2.get(0);
					if (ct2.getLevel() > cct.getLevel()) {
						bottom = ct2;
						setAddr(res, cct.getId(), name, addrList);
						setAddr(res, ct2.getId(), name2, addrList);
					}
				}
			}
		}
		return bottom;
	}

	/**
	 * 获取能匹配name的，所有可能合法的数据。
	 */
	private static List<CityToken> getccl(CityToken top, CityToken bottom, String name) {
		List<CityToken> ccl = new ArrayList<CityToken>();
		List<CityToken> nctl = DataCache.nameMap.get(name);
		for (int i = 0, n = nctl.size(); i < n; ++i) {
			CityToken ct = nctl.get(i);
			if (ct.getLevel() < top.getLevel()) {
				if (hasRelationship(ct, top)) {
					ccl.clear();
					ccl.add(ct);
					break;
				}
			} else if (ct.getLevel() > bottom.getLevel()) {
				if (hasRelationship(bottom, ct)) {
					if (!ccl.isEmpty()) {
						if (ccl.get(0).getLevel() > ct.getLevel()) {
							ccl.clear();
							ccl.add(ct);
						} else if (ccl.get(0).getLevel() == ct.getLevel()) {
							ccl.add(ct);
						}
					} else
						ccl.add(ct);
				}
			}
		}
		return ccl;
	}

	private static CityToken findTopCT(String name) {
		CityToken top = null;
		List<CityToken> nctl = DataCache.nameMap.get(name);
		for (int i = 0, n = nctl.size(); i < n; ++i) {
			CityToken ct = nctl.get(i);
			if (top == null || ct.getLevel() < top.getLevel()) {
				top = ct;
			}
		}
		return top;
	}

	private static boolean hasRelationship(CityToken pct, CityToken ct) {
		if (ct.getParentId() == null || ct.getLevel() <= pct.getLevel() ||
		// 大于两个级别差的关联，相对来说准确率比较低。
				ct.getLevel() - pct.getLevel() > 2)
			return false;
		boolean res = ct.getParentId().equals(pct.getId());
		if (!res) {
			CityToken parentCt = ct.getParent();
			if (parentCt != null) {
				// 如果与ct的parent有关系，则与ct有关系
				return hasRelationship(pct, parentCt);
			}
		}
		return res;
	}
}
