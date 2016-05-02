package test;

import net.yeah.zhouyou.mickey.address.v3.Address;
import net.yeah.zhouyou.mickey.address.v3.AddressScanner;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by mickey on 5/2/16.
 */
public class Test {

    public static void main(String[] args) throws Exception {
        long tm1 = System.currentTimeMillis();
        long tm2 = System.currentTimeMillis();
        System.out.println("construct scanner use " + (tm2 - tm1) + " ms.");
        Address.Info address = AddressScanner.scan("江西抚州市南昌大学抚州医学分院12级全科2班").info();
        long tm3 = System.currentTimeMillis();
        System.out.println("scan an address use " + (tm3 - tm2) + " ms.");

        System.out.println("province_address : " + address.getProvinceAddress());
        System.out.println("city_address     : " + address.getCityAddress());
        System.out.println("area_address     : " + address.getAreaAddress());
        System.out.println("town_address     : " + address.getTownAddress());
        System.out.println("original_address : " + address.getOriginalAddress());
        System.out.println("detail_address   : " + address.getDetailAddress());


        long tm4 = System.currentTimeMillis();

        List<String> lines = Files.readAllLines(Paths.get("/Users/mickey/github/addressCut3/code/测试地址.txt"));

        long tm5 = System.currentTimeMillis();
        System.out.println("读取所有测试地址消耗时长 " + (tm5 - tm4) + " ms.");

        List<Address.Info> addr_list = new ArrayList<>(250000);
        for (int i = 0; i < lines.size(); ++i) {
            addr_list.add(AddressScanner.scan(lines.get(i)).info());
        }
        long tm6 = System.currentTimeMillis();
        System.out.println("识别所有测试地址消耗时长 " + (tm6 - tm5) + " ms.");
    }
}
