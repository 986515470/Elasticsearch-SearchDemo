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
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.search.suggest.Suggest;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.elasticsearch.search.suggest.completion.CompletionSuggestion;
import org.elasticsearch.search.suggest.completion.CompletionSuggestionBuilder;
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
                HttpHost.create("http://yourIp:9200")
        ));
    }

    @AfterEach
    void tearDown() throws IOException {
        this.client.close();
    }

    @Test
    void testMatchAll() throws IOException {
        //??????request
        SearchRequest request = new SearchRequest("hotel");
//        ??????dsl
        request.source().query(QueryBuilders.matchAllQuery());
        //????????????
        SearchResponse search = client.search(request, RequestOptions.DEFAULT);

        //????????????
        SearchHits searchHits=search.getHits();
        long value = searchHits.getTotalHits().value;
        //????????????
        SearchHit[] hits=searchHits.getHits();
        for (SearchHit hit:hits){
            String json=hit.getSourceAsString();
            //????????????
            HotelDoc jsonObject = JSON.parseObject(json,HotelDoc.class);
            System.out.println("hotel:"+jsonObject);
        }
        System.out.println("???????????????"+value+"?????????");
    }
    @Test
    void testMatch() throws IOException {
        //??????request
        SearchRequest request = new SearchRequest("hotel");
//        ??????dsl
        request.source().query(QueryBuilders.matchQuery("all","??????"));
        //????????????
        SearchResponse search = client.search(request, RequestOptions.DEFAULT);

        handleResponse(search);
    }

    @Test
    void testBool() throws IOException {
        //??????request
        SearchRequest request = new SearchRequest("hotel");

        BoolQueryBuilder booleanQuery=QueryBuilders.boolQuery();
        booleanQuery.must(QueryBuilders.termQuery("city","??????"));
        booleanQuery.filter(QueryBuilders.rangeQuery("price").lte(200));

        //        ??????dsl
        request.source().query(booleanQuery);
        //????????????
        SearchResponse search = client.search(request, RequestOptions.DEFAULT);

        handleResponse(search);
    }

    @Test
    void testPageAndSort() throws IOException {

        int page=1,size=5;
        //??????request
        SearchRequest request = new SearchRequest("hotel");

        //sort from
        request.source().query(QueryBuilders.matchAllQuery());
        request.source().sort("price", SortOrder.ASC );
        request.source().from((page-1)*size).size(size);
        //????????????
        SearchResponse search = client.search(request, RequestOptions.DEFAULT);

        handleResponse(search);
    }

    @Test
    void testHeightLight() throws IOException {

        int page=1,size=5;
        //??????request
        SearchRequest request = new SearchRequest("hotel");

        //sort from
        request.source().query(QueryBuilders.matchQuery("all","??????"));
        request.source().highlighter(new HighlightBuilder().field("name").requireFieldMatch(false));

        //????????????
        SearchResponse search = client.search(request, RequestOptions.DEFAULT);

        handleResponse(search);
    }


    @Test
    void testAgg() throws IOException {
        //??????request
        SearchRequest request = new SearchRequest("hotel");

        //sort from
        request.source().size(0);
        request.source().aggregation(AggregationBuilders.terms("brandAgg").field("brand").size(10));

        //????????????
        SearchResponse search = client.search(request, RequestOptions.DEFAULT);

        //????????????
        Aggregations aggregations = search.getAggregations();
        Terms brandAgg = aggregations.get("brandAgg");
        List<? extends Terms.Bucket> buckets = brandAgg.getBuckets();
        for (Terms.Bucket bucket : buckets) {
            System.out.println(bucket.getKeyAsString()+" : "+bucket.getDocCount());
        }
        handleResponse(search);
    }

    @Test
    void testSuggestion() {
        //??????request
        SearchRequest request = new SearchRequest("hotel");

        //sort from

        request
                .source()
                .suggest(new SuggestBuilder()
                        .addSuggestion("suggestions", SuggestBuilders
                                .completionSuggestion("suggestion")
                                .prefix("hz")
                                .skipDuplicates(true)
                                .size(10)
                               ));
        //????????????
        try {
            SearchResponse search = client.search(request, RequestOptions.DEFAULT);
            Suggest suggest = search.getSuggest();
            CompletionSuggestion suggestions = suggest.getSuggestion("suggestions");
            for (CompletionSuggestion.Entry entry : suggestions.getEntries()) {
                for (CompletionSuggestion.Entry.Option option : entry) {
                    System.out.println(option.getText().string());
                }
            }
            System.out.println(search);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleResponse(SearchResponse search) {
        //????????????
        SearchHits searchHits= search.getHits();
        long value = searchHits.getTotalHits().value;
        //????????????
        SearchHit[] hits=searchHits.getHits();
        for (SearchHit hit:hits){
            String json=hit.getSourceAsString();
            //????????????
            HotelDoc jsonObject = JSON.parseObject(json,HotelDoc.class);
            //??????????????????
            Map<String, HighlightField> highlightFields = hit.getHighlightFields();
            if(!CollectionUtils.isEmpty(highlightFields)){
                HighlightField name = highlightFields.get("name");
                if(name!=null) {
                    //???????????????
                    String str = name.getFragments()[0].string();
                    jsonObject.setName(str);
                }
            }
            System.out.println("hotel:"+jsonObject);
        }
        System.out.println("???????????????"+value+"?????????");
    }

}
