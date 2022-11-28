package com.deski.ddns.ddns;


import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.dnspod.v20210323.DnspodClient;
import com.tencentcloudapi.dnspod.v20210323.models.*;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class TencentClient {
    /** 腾讯云API ID*/
    private static final String SECRET_ID = "xxxxxxxxxxxxxxxxxxx";
    /** 腾讯云API 密钥*/
    private static final String SECRET_KEY = "xxxxxxxxxxxxxxxxxxxx";

    private static final Credential CRED = new Credential(SECRET_ID, SECRET_KEY);

    private static final DnspodClient client = new DnspodClient(CRED, "");

    /** 免费版解析套餐TTL最低支持600 */
    public static void register(String domain, String recordType, String recordLine, String value, String subDomain) {

        if (null == value){
            log.error("无法获取外网IP地址");
            return;
        }
        try {
            CreateRecordRequest req = new CreateRecordRequest();
            req.setDomain(domain);
            req.setRecordType(recordType);
            req.setRecordLine(recordLine);
            req.setValue(value);
            req.setSubDomain(subDomain);
            req.setTTL(600L);
            CreateRecordResponse resp = client.CreateRecord(req);
            log.info("新增记录成功：" + resp.getRecordId());

        } catch (TencentCloudSDKException ex) {
            log.error("与腾讯云服务器通信失败：" + ex.getMessage());
        }

    }


    public static void refresh(String domain, String recordType, String recordLine, String value, String subDomain) {

        if (null == value){
            log.error("无法获取外网IP地址");
            return;
        }

        RecordListItem[] recordList = new RecordListItem[0];
        try {
            DescribeRecordListRequest desReq = new DescribeRecordListRequest();
            desReq.setDomain("deski.cn");
            desReq.setRecordType(recordType);
            desReq.setSubdomain(subDomain);
            DescribeRecordListResponse recordListResp = client.DescribeRecordList(desReq);
            recordList = recordListResp.getRecordList();
            ModifyRecordRequest req = new ModifyRecordRequest();
            req.setDomain(domain);
            req.setRecordType(recordType);
            req.setRecordLine(recordLine);
            req.setValue(value);
            req.setSubDomain(subDomain);
            req.setRecordId(recordList[0].getRecordId());
            req.setTTL(600L);
            ModifyRecordResponse resp = client.ModifyRecord(req);
            log.info("更新记录成功：" + resp.getRecordId());
        } catch (TencentCloudSDKException e) {
            log.warn("解析记录不存在、新建记录");
            register(domain, recordType, recordLine, value, subDomain);
        }
    }


}
