package org.example.elastic;

import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.support.ActiveShardCount;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.action.support.replication.ReplicationResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.VersionType;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptType;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.Map;

import static java.util.Collections.singletonMap;

@SpringBootTest
@RunWith(SpringRunner.class)
public class GetTest {
    @Autowired
    RestHighLevelClient client;
    @Test
    public void test() throws IOException {
        GetRequest getRequest1 = new GetRequest("posts", "_doc", "1").version(2);
        try {
            GetResponse getResponse = client.get(getRequest1, RequestOptions.DEFAULT);
        } catch (ElasticsearchException e) {
            if (e.status() == RestStatus.CONFLICT) {
                // 索引不存在，抛出异常
                System.out.println("索引不存在"+e);
                return;
            }
        }
        GetRequest getRequest = new GetRequest(
                "posts", // 索引
                "_doc",   // 类型
                "1");    // 文档id
        getRequest.fetchSourceContext(FetchSourceContext.DO_NOT_FETCH_SOURCE); // 禁用数据源检索

        String[] includes = new String[]{"message", "*Date"};
        String[] excludes = Strings.EMPTY_ARRAY;
        FetchSourceContext fetchSourceContext =
                new FetchSourceContext(true, includes, excludes);
        getRequest.fetchSourceContext(fetchSourceContext); // 配置特定字段的检索

//        String[] includes = Strings.EMPTY_ARRAY;
//        String[] excludes = new String[]{"message"};
//        FetchSourceContext fetchSourceContext =
//                new FetchSourceContext(true, includes, excludes);
//        getRequest.fetchSourceContext(fetchSourceContext); // 配置特定字段的检索

        getRequest.storedFields("message"); // 为特定存储字段配置检索(要求字段单独存储在映射中)
        GetResponse getResponse = client.get(getRequest, RequestOptions.DEFAULT);
        Map<String, Object> source = getResponse.getSource();
        String message = String.valueOf(source.get("message")); // 检索存储的message字段(要求字段单独存储在映射中)

        getRequest.routing("routing"); // 设置分片路由

//        getRequest.parent("parent"); // 设置父文档id

        getRequest.preference("preference"); // 设置偏向

        getRequest.realtime(false); // 将实时标志设置为false(默认为true)

        getRequest.refresh(true); // 在检索文档之前执行一次刷新(默认为false)

        getRequest.version(2); // 设置版本

        getRequest.versionType(VersionType.EXTERNAL); // 设置版本类型

        ActionListener<GetResponse> listener= new ActionListener<GetResponse>() {
            @Override
            public void onResponse(GetResponse getResponse) {
                System.out.println("异步调用成功"+getResponse.toString());
                String index = getResponse.getIndex(); // 获的索引
                String type = getResponse.getType();   // 获得类型
                String id = getResponse.getId();       // 获得文档id
                if (getResponse.isExists()) {
                    long version = getResponse.getVersion(); // 获得版本
                    String sourceAsString = getResponse.getSourceAsString(); // 检索文档并生成字符串
                    Map<String, Object> sourceAsMap = getResponse.getSourceAsMap(); // 检索文档并生成map
                    byte[] sourceAsBytes = getResponse.getSourceAsBytes(); // 检索文档并生成byte数组
                } else {
                    // 没有找到文档，isExists返回false
                }
            }

            @Override
            public void onFailure(Exception e) {
                System.out.println("异步调用失败"+e);
            }
        };
        client.getAsync(getRequest, RequestOptions.DEFAULT, listener);

    }
    @Test
    public void test2() throws IOException {
        DeleteRequest deleteRequest = new DeleteRequest(
                "posts", // 索引
                "_doc",   // 类型
                "1");    // 文档id
//        deleteRequest.routing("routing"); // 设置分片路由

//        deleteRequest.timeout(TimeValue.timeValueMinutes(2)); // 等待主分片可访问的超时时间
//        deleteRequest.timeout("2m");                          // 等待主分片可访问的超时时间
//
//        deleteRequest.setRefreshPolicy(WriteRequest.RefreshPolicy.WAIT_UNTIL); // 设置刷新策略
//        deleteRequest.setRefreshPolicy("wait_for");                            // 设置刷新策略

//        deleteRequest.version(2); // 设置版本

//        deleteRequest.versionType(VersionType.EXTERNAL); // 设置版本类型
        DeleteResponse deleteResponse = client.delete(deleteRequest, RequestOptions.DEFAULT);
        ActionListener<DeleteResponse>listener = new ActionListener<DeleteResponse>() {
            @Override
            public void onResponse(DeleteResponse deleteResponse) {
                // 成功
                String index = deleteResponse.getIndex(); // 获得索引
                String type = deleteResponse.getType();   // 获得类型
                String id = deleteResponse.getId();       // 获得文档id
                long version = deleteResponse.getVersion(); // 获得版本
                ReplicationResponse.ShardInfo shardInfo = deleteResponse.getShardInfo(); // 获得分片信息
                if (shardInfo.getTotal() != shardInfo.getSuccessful()) {
                    // 当成功的分片数量小于总数量时，执行的操作
                }
                if (shardInfo.getFailed() > 0) {
                    for (ReplicationResponse.ShardInfo.Failure failure :
                            shardInfo.getFailures()) {
                        String reason = failure.reason(); // 获取失败原因
                    }
                }
            }

            @Override
            public void onFailure(Exception e) {
                // 失败
            }
        };
        client.deleteAsync(deleteRequest, RequestOptions.DEFAULT, listener);

    }
    @Test
    public void test3() throws IOException {
        UpdateRequest updateRequest = new UpdateRequest(
                "posts", // 索引
                "_doc",   // 类型
                "1");    // 文档id
        Map<String, Object> parameters = singletonMap("count", 4); // 用map保存脚本中的参数

        Script inline = new Script(ScriptType.INLINE, "painless",
                "ctx._source.field += params.count", parameters);  // 创建内联脚本
        updateRequest.script(inline);  // 应用到updateRequest中
        BulkRequest bulkRequest = new BulkRequest();
        bulkRequest.setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE);
//        updateRequest.routing("routing"); // 设置分片路由


//        updateRequest.timeout(TimeValue.timeValueSeconds(1)); // 设置等待主分片可检索的超时时间
//        updateRequest.timeout("1s");                          // 设置等待主分片可检索的超时时间
//
//        updateRequest.setRefreshPolicy(WriteRequest.RefreshPolicy.WAIT_UNTIL); // 设置刷新策略
//        updateRequest.setRefreshPolicy("wait_for");                            // 设置刷新策略

        updateRequest.retryOnConflict(3); // 发生冲突时重试update的次数

        updateRequest.fetchSource(true); // 启用检索_source, 默认是禁用的
        updateRequest.docAsUpsert(true);

        String[] includes = new String[]{"updated", "r*"};
        String[] excludes = Strings.EMPTY_ARRAY;
        updateRequest.fetchSource(
                new FetchSourceContext(true, includes, excludes)); // 配置检索_source时检索的字段
        String jsonString = "{\"created\":\"2017-01-01\"}";
        updateRequest.upsert(jsonString, XContentType.JSON); // 传递一个json字符
//
//        String[] includes = Strings.EMPTY_ARRAY;
//        String[] excludes = new String[]{"updated"};
//        updateRequest.fetchSource(
//                new FetchSourceContext(true, includes, excludes)); // 配置检索_source时不检索的字段

//        updateRequest.version(2); // 设置版本

        updateRequest.detectNoop(false); // 禁用noop探测

        updateRequest.scriptedUpsert(true); // 指示无论文档是否存在，脚本都必须运行(即如果文档不存在，脚本负责创建文档)

        updateRequest.docAsUpsert(true); // 指定如果文档不存在，则使用upsert模式创建文档

        updateRequest.waitForActiveShards(2); // 设置在执行update操作之前必须存在的活动分片数量
        updateRequest.waitForActiveShards(ActiveShardCount.ALL); // 设置在执行update操作之前必须存在的活动分片数量
        UpdateResponse updateResponse = client.update(
                updateRequest, RequestOptions.DEFAULT);
        System.out.println(updateResponse);
    }
    @Test
    public void test4(){
        String referer="https://baoli.chinagoods.com/test/mrz/zentao/bug-view-6538.html";
        if(referer.contains("mrz")){//微信公众号
            String[] split = referer.split("mrz");
          String  backUrl=split[0]+"mrz/me";
            System.out.println(backUrl);
        }

    }
}
