package org.example.elastic;

import org.apache.lucene.search.TotalHits;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.ShardSearchFailure;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.ScoreSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.elasticsearch.search.suggest.SuggestionBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SearchTest {
    @Autowired
    RestHighLevelClient client;
    @Test
    public void test1() throws IOException {
        SearchRequest searchRequest = new SearchRequest();
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);// 添加 match_all 查询


        searchRequest.source(searchSourceBuilder); // 将 SearchSourceBuilder  添加到 SeachRequest 中
        SearchRequest searchRequest2 = new SearchRequest("posts");  // 设置搜索的 index
        searchRequest2.types("doc");

        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();  // 默认配置
        sourceBuilder.query(QueryBuilders.termQuery("user", "kimchy")); // 设置搜索，可以是任何类型的 QueryBuilder
        sourceBuilder.from(0); // 起始 index
        sourceBuilder.size(5); // 大小 size
        sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS)); // 设置搜索的超时时间

        SearchRequest searchRequest3 = new SearchRequest();
        searchRequest3.source(sourceBuilder);

        MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder("user", "kimchy");
        matchQueryBuilder.fuzziness(Fuzziness.AUTO);  // 模糊查询
        matchQueryBuilder.prefixLength(3); // 前缀查询的长度
        matchQueryBuilder.maxExpansions(10); // max expansion 选项，用来控制模糊查询

        QueryBuilder matchQueryBuilder2 = QueryBuilders.matchQuery("user", "kimchy")
                .fuzziness(Fuzziness.AUTO)
                .prefixLength(3)
                .maxExpansions(10);

        searchSourceBuilder.query(matchQueryBuilder);

        sourceBuilder.sort(new ScoreSortBuilder().order(SortOrder.DESC)); // 根据分数 _score 降序排列 (默认行为)
        sourceBuilder.sort(new FieldSortBuilder("_uid").order(SortOrder.ASC));  // 根据 id 降序排列
        sourceBuilder.fetchSource(false);
        String[] includeFields = new String[] {"title", "user", "innerObject.*"};
        String[] excludeFields = new String[] {"_type"};
        sourceBuilder.fetchSource(includeFields, excludeFields);

        SearchSourceBuilder searchSourceBuilder2 = new SearchSourceBuilder();
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        HighlightBuilder.Field highlightTitle =
                new HighlightBuilder.Field("title"); // title 字段高亮
        highlightTitle.highlighterType("unified");  // 配置高亮类型
        highlightBuilder.field(highlightTitle);  // 添加到 builder
        HighlightBuilder.Field highlightUser = new HighlightBuilder.Field("user");
        highlightBuilder.field(highlightUser);
        searchSourceBuilder.highlighter(highlightBuilder);


        SearchSourceBuilder searchSourceBuilder3 = new SearchSourceBuilder();
        TermsAggregationBuilder aggregation = AggregationBuilders.terms("by_company")
                .field("company.keyword");
        aggregation.subAggregation(AggregationBuilders.avg("average_age")
                .field("age"));
        searchSourceBuilder.aggregation(aggregation);

        SearchSourceBuilder searchSourceBuilder4 = new SearchSourceBuilder();
        SuggestionBuilder termSuggestionBuilder =
                SuggestBuilders.termSuggestion("user").text("kmichy");
        SuggestBuilder suggestBuilder = new SuggestBuilder();
        suggestBuilder.addSuggestion("suggest_user", termSuggestionBuilder);
        searchSourceBuilder.suggest(suggestBuilder);

        ActionListener<SearchResponse> listener = new ActionListener<SearchResponse>() {
            @Override
            public void onResponse(SearchResponse searchResponse) {
                // 查询成功
                RestStatus status = searchResponse.status(); // HTTP 状态码
                TimeValue took = searchResponse.getTook(); // 查询占用的时间
                Boolean terminatedEarly = searchResponse.isTerminatedEarly(); // 是否由于 SearchSourceBuilder 中设置 terminateAfter 而过早终止
                boolean timedOut = searchResponse.isTimedOut(); // 是否超时
                int totalShards = searchResponse.getTotalShards();
                int successfulShards = searchResponse.getSuccessfulShards();
                int failedShards = searchResponse.getFailedShards();
                for (ShardSearchFailure failure : searchResponse.getShardFailures()) {
                    // failures should be handled here
                }
            }

            @Override
            public void onFailure(Exception e) {
                // 查询失败
            }
        };


        SearchHits hits = searchResponse.getHits();
        TotalHits totalHits = hits.getTotalHits();
        float maxScore = hits.getMaxScore();
        SearchHit[] searchHits = hits.getHits();
        for (SearchHit hit : searchHits) {
            // do something with the SearchHit
            String index = hit.getIndex();
            String type = hit.getType();
            String id = hit.getId();
            float score = hit.getScore();
            String sourceAsString = hit.getSourceAsString();
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            String documentTitle = (String) sourceAsMap.get("title");
            List<Object> users = (List<Object>) sourceAsMap.get("user");
            Map<String, Object> innerObject =
                    (Map<String, Object>) sourceAsMap.get("innerObject");
            System.out.println(innerObject);
        }
    }
}
