package cn.wcy.hotel.service.impl;

import cn.wcy.hotel.mapper.HotelMapper;
import cn.wcy.hotel.pojo.Hotel;
import cn.wcy.hotel.pojo.HotelDoc;
import cn.wcy.hotel.pojo.PageResult;
import cn.wcy.hotel.pojo.RequestParams;
import cn.wcy.hotel.service.IHotelService;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.unit.DistanceUnit;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.search.suggest.Suggest;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.elasticsearch.search.suggest.completion.CompletionSuggestion;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class HotelService extends ServiceImpl<HotelMapper, Hotel> implements IHotelService {

    @Resource
    private RestHighLevelClient client;

    @Override
    public PageResult search(RequestParams requestParams) {
        try {
            SearchRequest request = new SearchRequest("hotel");

            getBoolQueryBuilder(requestParams,request);

            Integer page = requestParams.getPage();
            Integer size = requestParams.getSize();
            request.source().from((page - 1) * size).size(size);
            //????????????
            String location = requestParams.getLocation();
            if (!StringUtils.isEmpty(location)) {
                request.source().sort(SortBuilders
                        .geoDistanceSort("location", new GeoPoint(location))
                        .order(SortOrder.ASC)
                        .unit(DistanceUnit.KILOMETERS));
            }
            //????????????
            SearchResponse search = client.search(request, RequestOptions.DEFAULT);
            return handleResponse(search);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Map<String, List<String>> filters(RequestParams params) throws IOException {
        //??????request
        SearchRequest request = new SearchRequest("hotel");
        getBoolQueryBuilder(params,request);
        //sort from
        request.source().size(0);
        buildAggregation(request);
        //????????????
        SearchResponse search = client.search(request, RequestOptions.DEFAULT);

        Map<String,List<String>> result = new HashMap<>();

        //????????????
        Aggregations aggregations = search.getAggregations();

        List<String> brands = getStrings(aggregations,"brandAgg");
        //??????map
        result.put("brand",brands);

        List<String> city = getStrings(aggregations,"cityAgg");
        //??????map
        result.put("city",city);

        List<String> starName = getStrings(aggregations,"starAgg");
        //??????map
        result.put("starName",starName);

        return result;

    }

    @Override
    public List<String> suggestions(String key) {
        //??????request
        SearchRequest request = new SearchRequest("hotel");

        //sort from

        request
                .source()
                .suggest(new SuggestBuilder()
                        .addSuggestion("suggestions", SuggestBuilders
                                .completionSuggestion("suggestion")
                                .prefix(key)
                                .skipDuplicates(true)
                                .size(10)
                        ));
        //????????????
        List<String> list = null;
        try {
            SearchResponse search = client.search(request, RequestOptions.DEFAULT);
            Suggest suggest = search.getSuggest();
            CompletionSuggestion suggestions = suggest.getSuggestion("suggestions");
            list = new ArrayList<>();
            for (CompletionSuggestion.Entry entry : suggestions.getEntries()) {
                for (CompletionSuggestion.Entry.Option option : entry) {
//                    System.out.println(option.getText().string());
                    list.add(option.getText().string());
                }
            }
//            System.out.println(search);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public void deleteById(Long id) {
         //??????request
        DeleteRequest request = new DeleteRequest("hotel",id.toString());
        try {
            client.delete(request,RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void insertById(Long id) {
       //??????id????????????
        Hotel hotel = getById(id);
        HotelDoc hotelDoc = new HotelDoc(hotel);
        //??????request
        IndexRequest request = new IndexRequest("hotel").id(hotel.getId().toString());
        request.source(JSON.toJSONString(hotelDoc), XContentType.JSON);
        try {
            client.index(request,RequestOptions.DEFAULT);
        } catch (IOException e) {
           throw new RuntimeException(e);
        }

    }

    private List<String> getStrings(Aggregations aggregations,String aggName) {
        Terms brandAgg = aggregations.get(aggName);
        List<? extends Terms.Bucket> buckets = brandAgg.getBuckets();
        List<String> brands = new ArrayList<>();

        for (Terms.Bucket bucket : buckets) {
            System.out.println(bucket.getKeyAsString() + " : " + bucket.getDocCount());
             brands.add(bucket.getKeyAsString());
        }
        return brands;
    }

    private void buildAggregation(SearchRequest request) {
        request.source().aggregation(AggregationBuilders.terms("brandAgg").field("brand").size(100));
        request.source().aggregation(AggregationBuilders.terms("cityAgg").field("city").size(100));
        request.source().aggregation(AggregationBuilders.terms("starAgg").field("starName").size(100));
    }

    private SearchSourceBuilder getBoolQueryBuilder(RequestParams requestParams, SearchRequest request) {

        //??????dsl query ??????
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        //must
        String key = requestParams.getKey();
        if (!StringUtils.isEmpty(key)) {
            boolQueryBuilder.must(QueryBuilders.matchQuery("all", key));
        } else {
            boolQueryBuilder.must(QueryBuilders.matchAllQuery());
        }
        //????????????
        if (!StringUtils.isEmpty(requestParams.getCity())) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("city", requestParams.getCity()));
        }
        //????????????
        if (!StringUtils.isEmpty(requestParams.getBrand())) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("brand", requestParams.getBrand()));
        }
        //????????????
        if (!StringUtils.isEmpty(requestParams.getStarName())) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("starName", requestParams.getStarName()));
        }
        //????????????
        if (!StringUtils.isEmpty(requestParams.getMinPrice()) && !StringUtils.isEmpty(requestParams.getMaxPrice())) {
            boolQueryBuilder.filter(QueryBuilders.rangeQuery("price").gte(requestParams.getMinPrice()).lte(requestParams.getMaxPrice()));
        }

        //??????
        FunctionScoreQueryBuilder functionScoreQueryBuilder = QueryBuilders
                .functionScoreQuery(
                        boolQueryBuilder,
                        new FunctionScoreQueryBuilder
                                .FilterFunctionBuilder[]{
                                new FunctionScoreQueryBuilder.FilterFunctionBuilder(
                                        QueryBuilders.termQuery("isAD", true),
                                        ScoreFunctionBuilders.weightFactorFunction(10)
                                )
                        });

        return request.source().query(functionScoreQueryBuilder);
    }


    private PageResult handleResponse(SearchResponse search) {
        //????????????
        SearchHits searchHits = search.getHits();
        long total = searchHits.getTotalHits().value;
        //????????????
        SearchHit[] hits = searchHits.getHits();
        List<HotelDoc> hotelDocs = new ArrayList<>();
        for (SearchHit hit : hits) {
            String json = hit.getSourceAsString();
            //????????????
            HotelDoc jsonObject = JSON.parseObject(json, HotelDoc.class);

            //???????????????
            Object[] sortValues = hit.getSortValues();
            if (sortValues.length > 0) {
                Object sortValue = sortValues[0];
                jsonObject.setDistance(sortValue);
            }

            //??????????????????
            Map<String, HighlightField> highlightFields = hit.getHighlightFields();
            if (!CollectionUtils.isEmpty(highlightFields)) {
                HighlightField name = highlightFields.get("name");
                if (name != null) {
                    //???????????????
                    String str = name.getFragments()[0].string();
                    jsonObject.setName(str);
                }
            }


            hotelDocs.add(jsonObject);
            System.out.println("hotel:" + jsonObject);
        }
        return new PageResult(total, hotelDocs);

    }
}
