package net.yeah.zhouyou.mickey.address.v3;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;

public class CityBasedataReader implements Iterable<String>, Iterator<String> {
	private final InputStream fis;
	private final BufferedReader br;
	private String line;

	public CityBasedataReader() {
		try {
			fis = DataCache.class.getClassLoader().getResourceAsStream("citybasedata_v3.config");
			br = new BufferedReader(new InputStreamReader(fis, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean hasNext() {
		try {
			if (br != null)
				line = br.readLine();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		boolean res = line != null;
		if (!res) {
			closeAll();
		}
		return res;
	}

	@Override
	public String next() {
		return line;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Iterator<String> iterator() {
		return this;
	}

	@Override
	protected void finalize() {
		closeAll();
	}

	private void closeAll() {
		if (br != null)
			try {
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		if (fis != null)
			try {
				fis.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
	}
}
