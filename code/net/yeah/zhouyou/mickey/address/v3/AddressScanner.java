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

		if (addrList.size() > 6)
			addrList = addrList.subList(0, 6);

		return new Address(txt, addrList);
	}
}
