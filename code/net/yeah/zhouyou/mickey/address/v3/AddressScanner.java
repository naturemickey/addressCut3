package net.yeah.zhouyou.mickey.address.v3;

import static net.yeah.zhouyou.mickey.address.v3.DFAInstance.dfa;

import java.util.List;
import java.util.regex.Pattern;

public class AddressScanner {

	private static Pattern p = Pattern.compile("[\\s　]");

	public static Address scan(String txt) {
		return scan(txt, true);
	}

	public static Address scan(String txt, boolean exactMatch4Level) {
		// 中文地址中的空白是没有意义的
		txt = p.matcher(txt).replaceAll("");

		List<String> addrList = dfa.scan(txt);

		return new Address(txt, addrList);
	}

}
