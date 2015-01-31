package net.yeah.zhouyou.mickey.address.v3;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CollUtils {

	public static <T> Set<T> union(Set<T> c1, Set<T> c2) {
		Set<T> res = new HashSet<T>(c1);
		res.addAll(c2);
		return res;
	}

	public static <T, E> void setMapValue(Map<T, List<E>> map, T key, E value) {
		List<E> lst = map.get(key);
		if (lst == null) {
			lst = new ArrayList<E>();
			map.put(key, lst);
		}
		lst.add(value);
	}
}
