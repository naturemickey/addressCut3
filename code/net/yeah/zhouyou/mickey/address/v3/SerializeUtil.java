package net.yeah.zhouyou.mickey.address.v3;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class SerializeUtil {

	public static void write(Object obj, String fileName) {
		FileOutputStream fos = null;
		ObjectOutputStream oos = null;
		try {
			fos = new FileOutputStream(fileName);
			oos = new ObjectOutputStream(fos);
			oos.writeObject(obj);
			oos.flush();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (fos != null)
				try {
					fos.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			if (oos != null)
				try {
					oos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
	}

	@SuppressWarnings("unchecked")
	public static <T> T read(String fileName) {
		InputStream fis = null;
		ObjectInputStream ois = null;
		T t = null;
		try {
			// fis = new FileInputStream(fileName);//本地测试时用文件
			fis = SerializeUtil.class.getClassLoader().getResourceAsStream(fileName);
			ois = new ObjectInputStream(fis);
			t = (T) ois.readObject();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (fis != null)
				try {
					fis.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			if (ois != null)
				try {
					ois.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
		return t;
	}
}
