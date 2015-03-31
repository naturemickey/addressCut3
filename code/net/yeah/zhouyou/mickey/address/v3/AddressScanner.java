package net.yeah.zhouyou.mickey.address.v3;

import static net.yeah.zhouyou.mickey.address.v3.DFAInstance.dfa;

import java.util.List;
import java.util.regex.Pattern;

public class AddressScanner {

	private static Pattern p = Pattern.compile("[\\s　]");

	public static Address scan(String txt) {
		// 中文地址中的空白是没有意义的
		txt = p.matcher(txt).replaceAll("");

		List<String> addrList = dfa.scan(txt);

		if (addrList.size() > 0) {
			String fc = addrList.get(0);
			if (fc.length() == 3) {
				char c = fc.charAt(0);
				if ((c == '北' || c == '上' || c == '天' || c == '重') && fc.charAt(2) == '市') {
					c = fc.charAt(1);
					if (c == '京')
						addrList.add(0, "北京");
					else if (c == '海')
						addrList.add(0, "上海");
					else if (c == '津')
						addrList.add(0, "天津");
					else if (c == '庆')
						addrList.add(0, "重庆");
				}
			}
		}

		return new Address(txt, addrList);
	}
}
