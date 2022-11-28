package com.deski.ddns.ddns;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;


@Component
@Slf4j
@EnableScheduling
public class ScheduleTask {

    /**
     * 中科大测速网站获取外网IP
     */
    private static final String IPV4_URL = "https://test.ustc.edu.cn/backend/getIP.php";
    private static final String IPV6_URL = "https://test6.ustc.edu.cn/backend/getIP.php";

    /** 域名*/
    private static final String DOMAIN = "example.cn";
    /** 二级域名*/
    private static final String SUB_DOMAIN = "rdp";

    @Scheduled(fixedDelay = 6 * 60 * 1000)
    public void run() {
        try {
            /** 解析二级域名*/
            InetAddress[] deski = InetAddress.getAllByName(SUB_DOMAIN + "." + DOMAIN);
            if (deski.length > 0) {
                /** 存在IPV4记录 刷新IPV4 */
                String externalIpv4 = getExternalIpv4();
                if (!StrUtil.isEmpty(externalIpv4)) {
                    for (InetAddress inetAddress : deski) {
                        if (inetAddress.getClass().isAssignableFrom(Inet4Address.class)) {
                            if (!externalIpv4.equals(inetAddress.getHostAddress())) {
                                TencentClient.refresh(DOMAIN, "A", "默认", externalIpv4, SUB_DOMAIN);
                            }
                        }
                    }
                }

                /** 存在IPV6记录 刷新IPV6 */
                String externalIpv6 = getExternalIpv6();
                if (!StrUtil.isEmpty(externalIpv6)) {
                    for (InetAddress inetAddress : deski) {
                        if (inetAddress.getClass().isAssignableFrom(Inet6Address.class)) {
                            if (!externalIpv4.equals(inetAddress.getHostAddress())) {
                                TencentClient.refresh(DOMAIN, "AAAA", "默认", externalIpv6, SUB_DOMAIN);
                            }
                        }
                    }
                }
            }
        } catch (UnknownHostException e) {
            log.info("域名解析无结果，添加记录");
            TencentClient.register(DOMAIN, "A", "默认", getExternalIpv4(), SUB_DOMAIN);
            TencentClient.register(DOMAIN, "AAAA", "默认", getExternalIpv6(), SUB_DOMAIN);
            e.printStackTrace();
        }


    }

    private String getExternalIpv4() {
        return getString(IPV4_URL);
    }

    private String getExternalIpv6() {
        return getString(IPV6_URL);
    }

    private String getString(String urlType) {
        String ip = null;
        try {
            OkHttpClient okHttpClient = new OkHttpClient();
            Request request = new Request.Builder().get().url(urlType).build();
            Call call = okHttpClient.newCall(request);
            Response execute = call.execute();
            ip = JSON.parseObject(execute.body().string()).getString("processedString");
        } catch (Exception e) {
            log.error("访问外网失败，请检查是否连接互联网");
            e.printStackTrace();
        } finally {
            log.info("获取外网地址：" + ip);
        }
        return ip;
    }
}
