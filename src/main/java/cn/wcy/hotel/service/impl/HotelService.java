package cn.wcy.hotel.service.impl;

import cn.wcy.hotel.mapper.HotelMapper;
import cn.wcy.hotel.pojo.Hotel;
import cn.wcy.hotel.pojo.HotelDoc;
import cn.wcy.hotel.pojo.PageResult;
import cn.wcy.hotel.pojo.RequestParams;
import cn.wcy.hotel.service.IHotelService;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.unit.DistanceUnit;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class HotelService extends ServiceImpl<HotelMapper, Hotel> implements IHotelService {

    @Resource
    private RestHighLevelClient client;

    @Override
    public PageResult search(RequestParams requestParams)  {
        try {
            SearchRequest request = new SearchRequest("hotel");




            SearchSourceBuilder boolQueryBuilder1 = getBoolQueryBuilder(requestParams, request);
            Integer page = requestParams.getPage();
            Integer size = requestParams.getSize();
            request.source().from((page - 1) * size).size(size);
            //位置排序
            String location = requestParams.getLocation();
            if(!StringUtils.isEmpty(location)){
                request.source().sort(SortBuilders
                        .geoDistanceSort("location",new GeoPoint(location))
                        .order(SortOrder.ASC)
                        .unit(DistanceUnit.KILOMETERS));
            }
            //发送请求
            SearchResponse search = client.search(request, RequestOptions.DEFAULT);
            return handleResponse(search);
        }catch (IOException e){
            throw new RuntimeException(e);
        }
    }

    private SearchSourceBuilder getBoolQueryBuilder(RequestParams requestParams, SearchRequest request) {

        //准备dsl query 分页
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        //must
        String key = requestParams.getKey();
        if (!StringUtils.isEmpty(key)) {
           boolQueryBuilder.must(QueryBuilders.matchQuery("all", key));
        } else {
            boolQueryBuilder.must(QueryBuilders.matchAllQuery());
        }
        //城市条件
        if(!StringUtils.isEmpty(requestParams.getCity())){
            boolQueryBuilder.filter(QueryBuilders.termQuery("city", requestParams.getCity()));
        }
        //品牌条件
        if(!StringUtils.isEmpty(requestParams.getBrand())){
            boolQueryBuilder.filter(QueryBuilders.termQuery("brand", requestParams.getBrand()));
        }
        //星级条件
        if(!StringUtils.isEmpty(requestParams.getStarName())){
            boolQueryBuilder.filter(QueryBuilders.termQuery("starName", requestParams.getStarName()));
        }
        //价格条件
        if(!StringUtils.isEmpty(requestParams.getMinPrice())&&!StringUtils.isEmpty(requestParams.getMaxPrice())){
            boolQueryBuilder.filter(QueryBuilders.rangeQuery("price").gte(requestParams.getMinPrice()).lte(requestParams.getMaxPrice()));
        }

        //算分
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

        return  request.source().query(functionScoreQueryBuilder);
    }


    private PageResult handleResponse(SearchResponse search) {
        //解析响应
        SearchHits searchHits= search.getHits();
        long total = searchHits.getTotalHits().value;
        //获取数组
        SearchHit[] hits=searchHits.getHits();
        List<HotelDoc> hotelDocs=new ArrayList<>();
        for (SearchHit hit:hits){
            String json=hit.getSourceAsString();
            //反序列化
            HotelDoc jsonObject = JSON.parseObject(json,HotelDoc.class);

            //获取排序值
            Object[] sortValues=hit.getSortValues();
            if(sortValues.length>0){
                Object sortValue = sortValues[0];
                jsonObject.setDistance(sortValue);
            }

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


            hotelDocs.add(jsonObject);
            System.out.println("hotel:"+jsonObject);
        }
       return new PageResult(total,hotelDocs);

    }
}
