package cn.wcy.hotel;

import cn.wcy.hotel.pojo.Hotel;
import cn.wcy.hotel.pojo.HotelDoc;
import cn.wcy.hotel.service.IHotelService;
import com.alibaba.fastjson.JSON;
import org.apache.http.HttpHost;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;

import static cn.wcy.hotel.constans.HotelContants.MAPPING_TEMPLATE;

/**
 * @author Wcy
 * @Date 2022/4/4 14:17
 */
@SpringBootTest
public class HotalIndexTest {
    private RestHighLevelClient client;

    @Resource
    private IHotelService iHotelService;

    @Test
    void testInit() {
        System.out.println(client);
    }

    @Test
    void creatHotelIndex() throws IOException {
        //创建Request索引
        CreateIndexRequest request = new CreateIndexRequest("hotel");
        //创建请求参数
        request.source(MAPPING_TEMPLATE, XContentType.JSON);
        //发送请求
        client.indices().create(request, RequestOptions.DEFAULT);
    }

    @Test
    void deleteHotelIndex() throws IOException {
        DeleteIndexRequest request = new DeleteIndexRequest("hotel");
        client.indices().delete(request, RequestOptions.DEFAULT);
    }

    @Test
    void existHotelIndex() throws IOException {
        GetIndexRequest request = new GetIndexRequest("hotel");
        boolean exists = client.indices().exists(request, RequestOptions.DEFAULT);
        System.out.println(exists ? "索引库已存在" : "索引库不存在");
    }


    @Test
    void testIndexDocument() throws IOException {

        Hotel byId = iHotelService.getById(61083L);
        HotelDoc hotelDoc = new HotelDoc(byId);
        //创建request对象
        IndexRequest request = new IndexRequest("hotel").id(hotelDoc.getId().toString());
//        准备json文档
        request.source(JSON.toJSONString(hotelDoc),XContentType.JSON);
//        发送请求
        client.index(request, RequestOptions.DEFAULT);

    }

    @Test
    void tetGetDocumentById() throws IOException{
        GetRequest request=new GetRequest("hotel","61083");

        String json= client.get(request,RequestOptions.DEFAULT).getSourceAsString();

        System.out.println(json);
    }

    @Test
    void tetDeleteDocumentById() throws IOException{

        DeleteRequest request=new DeleteRequest("hotel","61083");
        client.delete(request, RequestOptions.DEFAULT);

    }

    @Test
    void testUpdateDocumentById() throws IOException{
        UpdateRequest request=new UpdateRequest("hotel","61083");
        //准备参数
        request.doc(
                "price","666","starName","1钻"
        );
        //更新文档
        client.update(request,RequestOptions.DEFAULT);

    }

    @BeforeEach
    void setUp() {
        this.client = new RestHighLevelClient(RestClient.builder(
                HttpHost.create("http://yourIp:9200")
        ));
    }

    @AfterEach
    void tearDown() throws IOException {
        this.client.close();
    }

    @Test
    void testBulkRequest() throws IOException {
        List<Hotel> list = iHotelService.list();
        //转换为文档类型
        //创建request
        BulkRequest request=new BulkRequest();
        //添加多个新增的request
        for (Hotel hotel:list){
            HotelDoc hotelDoc = new HotelDoc(hotel);
            request.add(new IndexRequest("hotel")
                    .id(hotelDoc.getId().toString())
                    .source(JSON.toJSONString(hotelDoc),XContentType.JSON));
        }
        client.bulk(request,RequestOptions.DEFAULT);
    }


}
