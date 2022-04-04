package cn.wcy.hotel;

import cn.wcy.hotel.pojo.Hotel;
import cn.wcy.hotel.pojo.HotelDoc;
import cn.wcy.hotel.service.IHotelService;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.http.HttpHost;
import org.apache.lucene.queryparser.xml.builders.BooleanQueryBuilder;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.util.CollectionUtil;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.StringUtils;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import javax.swing.*;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static cn.wcy.hotel.constans.HotelContants.MAPPING_TEMPLATE;

/**
 * @author Wcy
 * @Date 2022/4/4 14:17
 */
@SpringBootTest
public class HotalSearchTest {

    private RestHighLevelClient client;

    @Resource
    private IHotelService iHotelService;

    @BeforeEach
    void setUp() {
        this.client = new RestHighLevelClient(RestClient.builder(
                HttpHost.create("http://ip:9200")
        ));
    }

    @AfterEach
    void tearDown() throws IOException {
        this.client.close();
    }

    @Test
    void testMatchAll() throws IOException {
        //准备request
        SearchRequest request = new SearchRequest("hotel");
//        准备dsl
        request.source().query(QueryBuilders.matchAllQuery());
        //发送请求
        SearchResponse search = client.search(request, RequestOptions.DEFAULT);

        //解析响应
        SearchHits searchHits=search.getHits();
        long value = searchHits.getTotalHits().value;
        //获取数组
        SearchHit[] hits=searchHits.getHits();
        for (SearchHit hit:hits){
            String json=hit.getSourceAsString();
            //反序列化
            HotelDoc jsonObject = JSON.parseObject(json,HotelDoc.class);
            System.out.println("hotel:"+jsonObject);
        }
        System.out.println("共搜索到："+value+"条数据");
    }
    @Test
    void testMatch() throws IOException {
        //准备request
        SearchRequest request = new SearchRequest("hotel");
//        准备dsl
        request.source().query(QueryBuilders.matchQuery("all","如家"));
        //发送请求
        SearchResponse search = client.search(request, RequestOptions.DEFAULT);

        handleResponse(search);
    }

    @Test
    void testBool() throws IOException {
        //准备request
        SearchRequest request = new SearchRequest("hotel");

        BoolQueryBuilder booleanQuery=QueryBuilders.boolQuery();
        booleanQuery.must(QueryBuilders.termQuery("city","杭州"));
        booleanQuery.filter(QueryBuilders.rangeQuery("price").lte(200));

        //        准备dsl
        request.source().query(booleanQuery);
        //发送请求
        SearchResponse search = client.search(request, RequestOptions.DEFAULT);

        handleResponse(search);
    }

    @Test
    void testPageAndSort() throws IOException {

        int page=1,size=5;
        //准备request
        SearchRequest request = new SearchRequest("hotel");

        //sort from
        request.source().query(QueryBuilders.matchAllQuery());
        request.source().sort("price", SortOrder.ASC );
        request.source().from((page-1)*size).size(size);
        //发送请求
        SearchResponse search = client.search(request, RequestOptions.DEFAULT);

        handleResponse(search);
    }

    @Test
    void testHeightLight() throws IOException {

        int page=1,size=5;
        //准备request
        SearchRequest request = new SearchRequest("hotel");

        //sort from
        request.source().query(QueryBuilders.matchQuery("all","如家"));
        request.source().highlighter(new HighlightBuilder().field("name").requireFieldMatch(false));

        //发送请求
        SearchResponse search = client.search(request, RequestOptions.DEFAULT);

        handleResponse(search);
    }



    private void handleResponse(SearchResponse search) {
        //解析响应
        SearchHits searchHits= search.getHits();
        long value = searchHits.getTotalHits().value;
        //获取数组
        SearchHit[] hits=searchHits.getHits();
        for (SearchHit hit:hits){
            String json=hit.getSourceAsString();
            //反序列化
            HotelDoc jsonObject = JSON.parseObject(json,HotelDoc.class);
            //获取高亮结果
            Map<String, HighlightField> highlightFields = hit.getHighlightFields();
            if(!CollectionUtils.isEmpty(highlightFields)){
                HighlightField name = highlightFields.get("name");
                if(name!=null) {
                    //获取高亮值
                    String str = name.getFragments()[0].string();
                    jsonObject.setName(str);
                }
            }
            System.out.println("hotel:"+jsonObject);
        }
        System.out.println("共搜索到："+value+"条数据");
    }

}
