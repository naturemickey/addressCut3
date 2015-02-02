package net.yeah.zhouyou.mickey.address.v3;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class DataCache {

	public static final Map<String, List<CityToken>> nameMap;
	public static final Map<Long, List<CityToken>> idMap;

	static {
		long initStart = System.currentTimeMillis();

		Map<String, List<CityToken>> nm = new HashMap<String, List<CityToken>>();
		Map<Long, List<CityToken>> im = new HashMap<Long, List<CityToken>>();

		for (String line : new CityBasedataReader()) {
			String[] ss = line.split(",");
			if (ss.length <= 2)
				continue;
			Long id = Long.valueOf(ss[0]);
			Long parentId = Long.valueOf(ss[1]);
			String level = ss[2];

			for (int i = 3; i < ss.length; ++i) {
				String name = ss[i];

				if (name == null || name.length() == 0 || "null".equals(name))
					continue;

				CityToken ct = new CityToken(id, parentId, Integer.valueOf(level), name);

				CollUtils.setMapValue(nm, name, ct);
				CollUtils.setMapValue(im, ct.getId(), ct);
			}
		}
		for (Entry<Long, List<CityToken>> e : im.entrySet()) {
			Collections.sort(e.getValue(), new Comparator<CityToken>() {
				@Override
				public int compare(CityToken o1, CityToken o2) {
					return o2.getName().length() - o1.getName().length();
				}
			});
		}
		for (List<CityToken> ctl : nm.values()) {
			for (CityToken ct : ctl) {
				if (ct.getParentId() != null) {
					List<CityToken> pctl = im.get(ct.getParentId());
					if (pctl != null && pctl.size() > 0)
						ct.parent = pctl.get(0);
				}
			}
		}
		nameMap = nm;
		idMap = im;
		System.out.println("DataCache init cost:" + (System.currentTimeMillis() - initStart));
		System.out.println("name count:" + nameMap.size());
		System.out.println("id count:" + idMap.size());
	}
}
