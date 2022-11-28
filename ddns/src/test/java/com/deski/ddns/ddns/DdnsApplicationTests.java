package com.deski.ddns.ddns;

import org.junit.jupiter.api.Test;

import java.net.SocketException;

//@SpringBootTest
class DdnsApplicationTests {

    @Test
    void contextLoads() throws SocketException {
        TencentClient.register("deski.cn","AAAA","默认","2409:8a20:4e84:6140:3831:e6fb:dad:2507","rdp");
    }

}
