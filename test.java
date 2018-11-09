package com.eleme;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import com.eleme.biz.FreightEfsManager;
import com.eleme.biz.FreightEfsManagerImpl;
import com.eleme.domain.ElemeProcessFindData;
import com.eleme.domain.ElemeProcessGetData;
import com.eleme.request.*;
import com.eleme.response.*;
import com.xiniunet.foundation.service.FoundationService;
import com.xiniunet.framework.base.BaseResponse;
import com.xiniunet.framework.exception.ErrorType;
import com.xiniunet.framework.security.Passport;
import com.xiniunet.framework.util.PropertiesPool;
import com.xiniunet.framework.util.SpringContext;
import com.xiniunet.master.domain.system.User;
import com.xiniunet.master.request.humanresource.ShopGetRequest;
import com.xiniunet.master.request.system.UserFindRequest;
import com.xiniunet.master.request.system.UserGetRequest;
import com.xiniunet.master.response.humanresource.ShopGetResponse;
import com.xiniunet.master.response.system.UserFindResponse;
import com.xiniunet.master.response.system.UserGetResponse;
import com.xiniunet.master.service.MasterService;
import com.xiniunet.postoffice.message.FreightCreateMessage;
import com.xiniunet.postoffice.request.CacheGetRequest;
import com.xiniunet.postoffice.request.FreightAttributeUpdateRequest;
import com.xiniunet.postoffice.request.MyPhoneFindRequest;
import com.xiniunet.postoffice.response.MyPhoneFindResponse;
import com.xiniunet.postoffice.service.PostofficeService;
import com.xiniunet.sf.express.OrderClinet;
import com.xiniunet.sf.express.request.CargoRequest;
import com.xiniunet.sf.express.request.OrderRequest;
import com.xiniunet.zto.express.ZTOClient;
import com.xiniunet.zto.express.domain.SenderAndReceiver;
import com.xiniunet.zto.express.request.BillCodeGetRequest;
import com.xiniunet.zto.express.request.MarkGetRequest;
import com.xiniunet.zto.express.response.BillCodeGetResponse;
import com.xiniunet.zto.express.response.MarkGetResponse;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Created by YQ on 2016/12/8.
 */
public class ElemePost {

    static private org.slf4j.Logger logger = LoggerFactory.getLogger(ElemePost.class);

    static private MasterService masterService  =  SpringContext.getApplicationContext().getBean(MasterService.class);

    static private FoundationService foundationService =  SpringContext.getApplicationContext().getBean(FoundationService.class);

    static private FreightEfsManager freightEfsManager = SpringContext.getApplicationContext().getBean(FreightEfsManager.class);

    static public Boolean isTestData = true;
    static public ElemeBaseResponse loadToken(String token)
    {//495712af-9287-404d-b44e-5fd2adc569b8

        String url;
        if("development".equals(PropertiesPool.get("eleme.deploy.type")))
        {
            url = "https://mobileapprovaluat.rajax.me/mobile_approval_service/mobileflow/loadtoken";
        }
        else
        {
            url = "https://actbpm.rajax.me/actbpm/mobileflow/loadtoken";
        }

        ElemeBaseResponse elemeBaseResponse = new ElemeBaseResponse();

        DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(url);
        httpPost.addHeader("Content-Type", "application/json;charset=utf-8");

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("token", token);
        StringEntity se = new StringEntity(jsonObject.toString(),"UTF-8");
        httpPost.setEntity(se);
        try {
            HttpResponse response = httpClient.execute(httpPost);
            elemeBaseResponse  = JSON.parseObject(EntityUtils.toString(response.getEntity(), "utf-8"),ElemeBaseResponse.class);
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
        return  elemeBaseResponse;
    }
    static public String getShareLink(String token, String orderno)
    {

        String url;
        if("development".equals(PropertiesPool.get("eleme.deploy.type")))
        {
            url = "https://devacting.rajax.me/acting/mobile/getShareLink";
        }
        else
        {
            url = "https://acting.rajax.me/acting/mobile/getShareLink";
        }

        ElemeBaseResponse elemeBaseResponse = new ElemeBaseResponse();

        DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(url);
        httpPost.addHeader("Content-Type", "application/json;charset=utf-8");

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("token", token);
        jsonObject.put("orderno", orderno);
        StringEntity se = new StringEntity(jsonObject.toString(),"UTF-8");
        httpPost.setEntity(se);
        try {
            HttpResponse response = httpClient.execute(httpPost);
           // elemeBaseResponse  = JSON.parseObject(EntityUtils.toString(response.getEntity(), "utf-8"),ElemeBaseResponse.class);
            return JSON.parseObject(EntityUtils.toString(response.getEntity(), "utf-8")).getString("link");
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
        return  "";
    }
    static public void sendProcessStartMessage(ElemeProcessStartRequest request)
    {
        try {
            Passport passport = new Passport();
            passport.setTenantId(512824102474878976L);

            ElemeMessageSendRequest elemeMessageSendRequest = new ElemeMessageSendRequest();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            List<ElemeMessageSendRequest.Item> itemList = new ArrayList<>();
            Date date = new Date();
            int iRow = 1;
            User user = new User();
            List<String> userList = new ArrayList<>();
            userList.add(request.getArgs().getSenderNumber());

                ElemeMessageSendRequest.Payload payload = new ElemeMessageSendRequest.Payload();
                payload.setTitle("饿了么小邮局电子面单通知");
                payload.setDate(simpleDateFormat.format(date));
                payload.setBarcode(request.getArgs().getFreightNumber());
                if("development".equals(PropertiesPool.get("eleme.deploy.type")))
                {
                    payload.setLink("https://mobileapprovaluat.rajax.me/post/index.html?img=" + payload.getBarcode() );
                }
                else
                {

                    payload.setLink("https://imview.rajax.me/im/html/post/index.html?img=" + payload.getBarcode() );
                }



            {
                ElemeMessageSendRequest.Item item = new ElemeMessageSendRequest.Item();
                item.setKey("快递单号");
                item.setRow(iRow++);
                item.setValue(request.getArgs().getFreightNumber());
                itemList.add(item);
            }
            {
                ElemeMessageSendRequest.Item item = new ElemeMessageSendRequest.Item();
                item.setKey("快递公司");
                item.setRow(iRow++);
                item.setValue(request.getArgs().getCarrierName() );
                itemList.add(item);
            }


            {
                ElemeMessageSendRequest.Item item = new ElemeMessageSendRequest.Item();
                item.setKey("收件姓名");
                item.setRow(iRow++);
                item.setValue(request.getArgs().getReceiverName());
                itemList.add(item);
            }
            {
                ElemeMessageSendRequest.Item item = new ElemeMessageSendRequest.Item();
                item.setKey("收件电话");
                item.setRow(iRow++);
                item.setValue(request.getArgs().getReceiverPhone());
                itemList.add(item);
            }


            payload.setBody(itemList);
            elemeMessageSendRequest.setUsers(userList);
            elemeMessageSendRequest.setPayload(payload);

            ElemeMessageSendResponse elemeMessageSendResponse = ElemePost.sendMessage(elemeMessageSendRequest);

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    static public ElemeProcessStartResponse startProcess(ElemeProcessStartRequest request)
    {

        Passport passport = new Passport();
        passport.setUserId(0L);

        request.getArgs().setFn("execute");
        String url;
        if("development".equals(PropertiesPool.get("eleme.deploy.type")))
        {
            request.setProcess_id("d90b9e793c5c4bb1bb129e19de9e2160");
            url = "https://devacting.rajax.me/acting/wfapi/start";
        }
        else
        {
            request.setProcess_id("d90b9e793c5c4bb1bb129e19de9e2160");
            url = "https://acting.rajax.me/acting/wfapi/start";
        }
        FreightEfsCreateRequest freightEfsCreateRequest = new FreightEfsCreateRequest();
        ElemeProcessStartResponse elemeProcessStartResponse = new ElemeProcessStartResponse();

        if(request.getArgs().getPaperType()!= null && (request.getArgs().getPaperType().equals("纸质面单")))
        {
            if(request.getArgs().getFreightNumber() != null)
            {
                FreightEfsFindRequest freightEfsFindRequest = new FreightEfsFindRequest();
                freightEfsFindRequest.setFreightNumber(request.getArgs().getFreightNumber());
                FreightEfsFindResponse freightEfsFindResponse = freightEfsManager.find(freightEfsFindRequest, passport);
                if(freightEfsFindResponse.getResult()!=null && freightEfsFindResponse.getResult().size() > 0)
                {
                    elemeProcessStartResponse.addError(ErrorType.BUSINESS_ERROR, "存在相同快递单号，请使用新的快递单号");
                    return  elemeProcessStartResponse;
                }
            }
            else
            {
                elemeProcessStartResponse.addError(ErrorType.BUSINESS_ERROR, "快递单号未填写");
                return  elemeProcessStartResponse;
            }
        }
       else if(request.getArgs().getPaperType()!= null && (request.getArgs().getPaperType().equals("电子面单")))
        {

            if(request.getArgs().getCarrierId().equals("76"))
            {
                com.xiniunet.zto.express.request.BillCodeGetRequest billCodeGetRequest = new BillCodeGetRequest();
                billCodeGetRequest.setSender(new SenderAndReceiver());
                billCodeGetRequest.setReceiver(new SenderAndReceiver());
                billCodeGetRequest.getSender().setName(request.getArgs().getSenderName());
                billCodeGetRequest.getSender().setMobile(request.getArgs().getSenderPhone());
                billCodeGetRequest.getSender().setAddress(request.getArgs().getSenderAddress());
                billCodeGetRequest.getSender().setCity(request.getArgs().getSenderCity());

                billCodeGetRequest.getReceiver().setName(request.getArgs().getReceiverName());
                billCodeGetRequest.getReceiver().setMobile(request.getArgs().getReceiverPhone());
                billCodeGetRequest.getReceiver().setAddress(request.getArgs().getReceiverAddress());
                billCodeGetRequest.getReceiver().setCity(request.getArgs().getReceiverCity());

                billCodeGetRequest.setOrderType("0");
                billCodeGetRequest.setPartner("ZTO1502161531351");
                billCodeGetRequest.setId(String.valueOf(foundationService.getNewId()));
                billCodeGetRequest.setBranchId("02100");


                ZTOClient ztoClient = new ZTOClient();
                ztoClient.setCompany_id("1c6e5c5ad922401ea9972f56c4c51f8e");
                ztoClient.setKey("964c2c91ad20");


                BillCodeGetResponse billCodeGetResponse =  ztoClient.billCodeGet(billCodeGetRequest, passport);
                if(billCodeGetResponse.hasError() )
                {
                    elemeProcessStartResponse.addErrors(billCodeGetResponse.getErrors());
                    return  elemeProcessStartResponse;
                }
                request.getArgs().setFreightNumber((billCodeGetResponse.getData().getBillCode()));
                elemeProcessStartResponse.setFreightNumber(billCodeGetResponse.getData().getBillCode());
                JSONObject jsonObj = JSON.parseObject(JSON.toJSONString(billCodeGetRequest));
                {
                    com.xiniunet.zto.express.request.MarkGetRequest markGetRequest = new MarkGetRequest();
                    markGetRequest.setReceiveAddress(request.getArgs().getReceiverAddress());
                    markGetRequest.setReceiveCity(request.getArgs().getReceiverCity());
                    markGetRequest.setSendAddress(request.getArgs().getSenderAddress());
                    markGetRequest.setUnionCode(UUID.randomUUID().toString());
                    markGetRequest.setSendCity(request.getArgs().getSenderCity());
                    MarkGetResponse markGetResponse = ztoClient.markGet(markGetRequest, passport);
                    if(!markGetResponse.hasError())
                    {
                        jsonObj.put("mark", markGetResponse.getResult().getMark());
                    }

                }
                jsonObj.put("siteName", billCodeGetResponse.getData().getSiteName());
                jsonObj.put("billCode", billCodeGetResponse.getData().getBillCode());
                freightEfsCreateRequest.setElectronicFreightData(jsonObj.toJSONString());
            }
            else  if(request.getArgs().getCarrierId().equals("80"))
            {

//                OrderClinet orderClinet = new OrderClinet();
//                orderClinet.setUrl("http://218.17.248.244:11080/bsp-oisp/sfexpressService");
//                orderClinet.setDevelopCode("LZSWLKJSH");
//                orderClinet.setCheckWord("CYh38oBvv9W7");
//                orderClinet.setPort(80);


                OrderClinet orderClinet = new OrderClinet();
                orderClinet.setUrl("http://bsp-oisp.sf-express.com/bsp-oisp/sfexpressService");
                orderClinet.setCheckWord("xsBHgF2UUl0FYx1qaqaKIwsA9ESadU1v");
                orderClinet.setPort(80);
                orderClinet.setDevelopCode("0217769315");
                // orderClinet.setPort(443);

                com.xiniunet.sf.express.request.OrderRequest orderRequest = new OrderRequest();

                orderRequest.setJ_contact(request.getArgs().getSenderName());
                orderRequest.setJ_mobile(request.getArgs().getSenderPhone());
                orderRequest.setJ_city(request.getArgs().getSenderCity());
                orderRequest.setJ_address(request.getArgs().getSenderAddress());

                orderRequest.setD_contact(request.getArgs().getReceiverName());
                orderRequest.setD_mobile(request.getArgs().getReceiverPhone());
                orderRequest.setD_city(request.getArgs().getReceiverCity());
                orderRequest.setD_address(request.getArgs().getReceiverAddress());
                orderRequest.setExpress_type ("1");
                if(request.getArgs().getPayType() == "到付")
                {
                    orderRequest.setPay_method ("2");
                }
                else
                {
                    orderRequest.setPay_method ("1");
                }

                orderRequest.setRemark (request.getArgs().getDescription());
                orderRequest.setOrderid(String.valueOf(foundationService.getNewId()));
                orderRequest.setCustid("0217769315");

                {
                    CargoRequest cargoRequest = new CargoRequest();
                    cargoRequest.setName(request.getArgs().getDescription());
//                    cargoRequest.setCount("1");
//                    cargoRequest.setUnit("单位");
//                    cargoRequest.setCurrency("");
//                    cargoRequest.setAmount("");
//                    cargoRequest.setWeight("");
                    List<CargoRequest> cargoRequestList = new ArrayList<>();
                    cargoRequestList.add(cargoRequest);
                    orderRequest.setCargoRequestList(cargoRequestList);

                }


                com.xiniunet.sf.express.response.OrderResponse orderResponse =  orderClinet.placeOrder(orderRequest, OrderRequest.class);
                if(orderResponse.hasError() )
                {
                    elemeProcessStartResponse.addErrors(orderResponse.getErrors());
                    return  elemeProcessStartResponse;
                }
                request.getArgs().setFreightNumber((orderResponse.getMailno()));
                elemeProcessStartResponse.setFreightNumber(orderResponse.getMailno());
                JSONObject jsonObj = JSON.parseObject(JSON.toJSONString(orderRequest));
                jsonObj.put("mailno",orderResponse.getMailno());
                jsonObj.put("destcode",orderResponse.getDestcode());

                freightEfsCreateRequest.setElectronicFreightData(jsonObj.toJSONString());
            }
            else
            {
                elemeProcessStartResponse.addError(ErrorType.BUSINESS_ERROR, "暂不支持该快递公司的电子面单");
                return  elemeProcessStartResponse;
            }
        }

        DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(url);
        httpPost.addHeader("Content-Type", "application/json;charset=utf-8");
        StringEntity se = new StringEntity( JSON.toJSON(request).toString(),"UTF-8");
        httpPost.setEntity(se);
        try {
            HttpResponse response = httpClient.execute(httpPost);
            elemeProcessStartResponse  = JSON.parseObject(EntityUtils.toString(response.getEntity(), "utf-8"), ElemeProcessStartResponse.class);
            if (elemeProcessStartResponse.getCode() == 0)
            {
                freightEfsCreateRequest.setProcessOrderNumber(elemeProcessStartResponse.getData().getOrderNo());
                freightEfsCreateRequest.setCarrierId(Long.valueOf(request.getArgs().getCarrierId()));
                freightEfsCreateRequest.setFreightNumber(request.getArgs().getFreightNumber());
                elemeProcessStartResponse.setFreightNumber(request.getArgs().getFreightNumber());
                FreightEfsCreateResponse freightEfsCreateResponse = freightEfsManager.create(freightEfsCreateRequest, passport);
                sendProcessStartMessage(request);
            }
        }
        catch (Exception e)
        {
            logger.error(e.getMessage());
            elemeProcessStartResponse.addError(ErrorType.SYSTEM_ERROR, e.getMessage());
        }
        return  elemeProcessStartResponse;
    }

    static public ElemeProcessExecuteResponse executeProcess(ElemeProcessExecuteRequest request)
    {

        String url;
        if("development".equals(PropertiesPool.get("eleme.deploy.type")))
        {
            url = "https://devacting.rajax.me/acting/wfapi/execute";
        }
        else
        {
            url = "https://acting.rajax.me/acting/wfapi/execute";
        }

        ElemeProcessExecuteResponse response = new ElemeProcessExecuteResponse();

        DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(url);
        httpPost.addHeader("Content-Type", "application/json;charset=utf-8");

        StringEntity se = new StringEntity( JSON.toJSON(request).toString(),"UTF-8");
        httpPost.setEntity(se);
        try {
            HttpResponse httpResponse = httpClient.execute(httpPost);
            response  = JSON.parseObject(EntityUtils.toString(httpResponse.getEntity(), "utf-8"), ElemeProcessExecuteResponse.class);
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
        return  response;
    }
    static public Long getProcessCount(ElemeProcessInfoFindRequest request)
    {

        ElemeProcessInfoFindResponse response = new ElemeProcessInfoFindResponse();
//        if (isTestData == true)
//        {
//            List<ElemeProcessFindData> elemeProcessFindDataList = new ArrayList<>();
//            for(int i=0; i<8; i++)
//            {
//                ElemeProcessFindData elemeProcessFindData = new ElemeProcessFindData();
//                elemeProcessFindData.setCreatetime("2016-12-03 16:11:11");
//                elemeProcessFindData.setCreator("杨琦");
//                elemeProcessFindData.setLastoperator("殷大可");
//                elemeProcessFindData.setOrdercreatetime("2016-12-02 16:11:11");
//                elemeProcessFindData.setOrderid("730604130451984383");
//                elemeProcessFindData.setOrderno("2016121300008");
//                elemeProcessFindData.setProcessdisplayname("快递申请单");
//                elemeProcessFindData.setReason("发票报销");
//                elemeProcessFindData.setTaskid("730604130451984384");
//                elemeProcessFindDataList.add(elemeProcessFindData);
//            }
//            response.setData(elemeProcessFindDataList);
//            return response;
//        }

        String url;
        if("development".equals(PropertiesPool.get("eleme.deploy.type")))
        {
            url = "https://devacting.rajax.me/acting//wfapi/wfCount";
        }
        else
        {
            url = "https://acting.rajax.me/acting//wfapi/wfCount";
        }

        DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(url);
        httpPost.addHeader("Content-Type", "application/json;charset=utf-8");

        StringEntity se = new StringEntity( JSON.toJSON(request).toString(),"UTF-8");
        httpPost.setEntity(se);
        try {
            HttpResponse httpResponse = httpClient.execute(httpPost);
            response  = JSON.parseObject(EntityUtils.toString(httpResponse.getEntity(), "utf-8"), ElemeProcessInfoFindResponse.class);
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }


        return  response.getCount();
    }
    static public ElemeProcessInfoFindResponse findProcessList(ElemeProcessInfoFindRequest request)
    {

        request.getProcess_names().set(0, "寄件申请");
        ElemeProcessInfoFindResponse response = new ElemeProcessInfoFindResponse();
//        if (isTestData == true)
//        {
//            List<ElemeProcessFindData> elemeProcessFindDataList = new ArrayList<>();
//            for(int i=0; i<8; i++)
//            {
//                ElemeProcessFindData elemeProcessFindData = new ElemeProcessFindData();
//                elemeProcessFindData.setCreatetime("2016-12-03 16:11:11");
//                elemeProcessFindData.setCreator("杨琦");
//                elemeProcessFindData.setLastoperator("殷大可");
//                elemeProcessFindData.setOrdercreatetime("2016-12-02 16:11:11");
//                elemeProcessFindData.setOrderid("730604130451984383");
//                elemeProcessFindData.setOrderno("2016121300008");
//                elemeProcessFindData.setProcessdisplayname("快递申请单");
//                elemeProcessFindData.setReason("发票报销");
//                elemeProcessFindData.setTaskid("730604130451984384");
//                elemeProcessFindDataList.add(elemeProcessFindData);
//            }
//            response.setData(elemeProcessFindDataList);
//            return response;
//        }

        String url;
        if("development".equals(PropertiesPool.get("eleme.deploy.type")))
        {
            url = "https://devacting.rajax.me/acting/wfapi/wfList";
        }
        else
        {
            url = "https://acting.rajax.me/acting/wfapi/wfList";
        }

        DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(url);
        httpPost.addHeader("Content-Type", "application/json;charset=utf-8");

        StringEntity se = new StringEntity( JSON.toJSON(request).toString(),"UTF-8");
        httpPost.setEntity(se);
        try {
            HttpResponse httpResponse = httpClient.execute(httpPost);
            response  = JSON.parseObject(EntityUtils.toString(httpResponse.getEntity(), "utf-8"), ElemeProcessInfoFindResponse.class);
            response.setTotalCount(getProcessCount(request));
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }

        return  response;

    }

    static public ElemeProcessGetResponse getProcess(ElemeProcessGetRequest request)
    {

        ElemeProcessGetResponse response = new ElemeProcessGetResponse();
//        if (isTestData == true)
//        {
////
//            ElemeProcessGetData elemeProcessGetData = new ElemeProcessGetData();
//           // elemeProcessGetData.setTagValue();
//            return response;
//        }

        String url;
        if("development".equals(PropertiesPool.get("eleme.deploy.type")))
        {
            url =  "https://devacting.rajax.me/acting/wfapi/taskInfo";
        }
        else
        {
            url =  "https://acting.rajax.me/acting/wfapi/taskInfo";
        }

        DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(url);
        httpPost.addHeader("Content-Type", "application/json;charset=utf-8");

        StringEntity se = new StringEntity( JSON.toJSON(request).toString(),"UTF-8");
        httpPost.setEntity(se);
        try {
          //  HttpResponse httpResponse = httpClient.execute(httpPost);
          //  response  = JSON.parseObject(EntityUtils.toString(httpResponse.getEntity(), "utf-8"), ElemeProcessGetResponse.class);
            HttpResponse httpResponse = httpClient.execute(httpPost);
            String json = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");
            response  = JSON.parseObject(json, ElemeProcessGetResponse.class);
            response.setJson(json);
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
        return  response;

    }

    static public ElemeTokenDetailGetResponse getTokenDetail(ElemeTokenDetailGetRequest request)
    {
        ElemeTokenDetailGetResponse response = new ElemeTokenDetailGetResponse();

        String url;
        if("development".equals(PropertiesPool.get("eleme.deploy.type")))
        {
            url =  "https://devacting.rajax.me/acting/mobile/getUser?token="+request.getToken();
        }
        else
        {
            url =  "https://acting.rajax.me/acting/mobile/getUser?token="+request.getToken();
        }


        DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(url);
        try {
            HttpResponse httpResponse = httpClient.execute(httpGet);
            response  = JSON.parseObject(EntityUtils.toString(httpResponse.getEntity(), "utf-8"), ElemeTokenDetailGetResponse.class);
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
        return  response;

    }
    static public ElemeMessageSendResponse sendMessage(ElemeMessageSendRequest request)
    {
        ElemeMessageSendResponse response = new ElemeMessageSendResponse();

        String url;
        if("development".equals(PropertiesPool.get("eleme.deploy.type")))
        {
            url = "http://imuat.rajax.me/appservice/thirdParty/msgSend";
            request.setAppKey("你自己的");
        }
        else
        {
            url =  "http://imapp.rajax.me/appservice/thirdParty/msgSend";
            request.setAppKey("你自己的");
        }

        DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(url);
        httpPost.addHeader("Content-Type", "application/json;charset=utf-8");

        StringEntity se = new StringEntity(JSON.toJSON(request).toString(),"UTF-8");
        httpPost.setEntity(se);
        try {
            HttpResponse httpResponse = httpClient.execute(httpPost);
            response  = JSON.parseObject(EntityUtils.toString(httpResponse.getEntity(), "utf-8"), ElemeMessageSendResponse.class);
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
        return  response;
    }
    static public void  post()
    {

    }
}
