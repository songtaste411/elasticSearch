package org.example.elastic;

import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.DocWriteRequest;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.action.support.replication.ReplicationResponse;
import org.elasticsearch.client.IndicesClient;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.VersionType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.Date;

@RunWith(SpringRunner.class)
@SpringBootTest
public class IndexTest {

    @Autowired
    RestHighLevelClient  client;
    @Test
    public void test() throws IOException {
            // 创建一个索引创建请求对象
            CreateIndexRequest createIndexRequest = new CreateIndexRequest("elasticsearch_test");
            //设置映射
            XContentBuilder builder = XContentFactory.jsonBuilder()
                    .startObject()
                    .field("properties")
                    .startObject()
                    .field("description").startObject().field("type", "text").endObject()
                    .field("name").startObject().field("type", "keyword").endObject()
                    .field("pic").startObject().field("type", "text").field("index", "false").endObject()
                    .field("studymodel").startObject().field("type", "keyword").endObject()
                    .endObject()
                    .endObject();
            createIndexRequest.mapping(builder);
            // 操作索引的客户端
            IndicesClient indicesClient = client.indices();

        CreateIndexResponse createIndexResponse = indicesClient.create(createIndexRequest, RequestOptions.DEFAULT);
        // 得到响应
            boolean acknowledged = createIndexResponse.isAcknowledged();
            System.out.println(acknowledged);
    }
    @Test
    public void test2() throws IOException {
        // 创建一个索引创建请求对象
        CreateIndexRequest createIndexRequest = new CreateIndexRequest("elasticsearch_test2");
//设置映射
        createIndexRequest.mapping("{\n" +
                "        \"properties\": {\n" +
                "          \"description\": {\n" +
                "            \"type\": \"text\"},\n" +
                "          \"name\": {\n" +
                "            \"type\": \"keyword\"\n" +
                "          },\n" +
                "          \"pic\": {\n" +
                "            \"type\": \"text\",\n" +
                "            \"index\": false\n" +
                "          },\n" +
                "          \"studymodel\": {\n" +
                "            \"type\": \"keyword\"\n" +
                "          }\n" +
                "        }\n" +
                "      }", XContentType.JSON);
// 操作索引的客户端
        IndicesClient indicesClient = client.indices();

        CreateIndexResponse createIndexResponse = indicesClient.create(createIndexRequest, RequestOptions.DEFAULT);
// 得到响应
        boolean acknowledged = createIndexResponse.isAcknowledged();
        System.out.println(acknowledged);
    }
    @Test
    public void testDeleteIndex() throws IOException {
        // 构建 删除索引库的请求对象
        DeleteIndexRequest deleteIndexRequest = new DeleteIndexRequest("elasticsearch_test2");
        IndicesClient indicesClient = client.indices();

        AcknowledgedResponse deleteResponse = indicesClient.delete(deleteIndexRequest, RequestOptions.DEFAULT);
        // 得到响应
        boolean acknowledge = deleteResponse.isAcknowledged();
        System.out.println(acknowledge);
    }
    @Test
    public void ExistIndex() throws IOException {
        GetIndexRequest request = new GetIndexRequest("elasticsearch_test");
        boolean exists = client.indices().exists(request, RequestOptions.DEFAULT);
        System.out.println(exists);
    }
    @Test
    public void ExistIndex2() throws IOException {
        IndexRequest indexRequest = new IndexRequest(
                "posts", // 索引名
                "_doc",   // 类型
                "1" // 文档id
              );
        // 文档数据
//        String jsonString = "{" +
//                "\"user\":\"kimchy\"," +
//                "\"postDate\":\"2013-01-30\"," +
//                "\"message\":\"trying out Elasticsearch\"" +
//                "}";
//        Map<String, Object> jsonMap = new HashMap<>();
//        jsonMap.put("user", "kimchy");
//        jsonMap.put("postDate", new Date());
//        jsonMap.put("message", "trying out Elasticsearch");
//        indexRequest.source(jsonString,XContentType.JSON);
        XContentBuilder builder = XContentFactory.jsonBuilder();
        builder.startObject();
        {
            builder.field("user", "kimchy");
            builder.timeField("postDate", new Date());
            builder.field("message", "trying out Elasticsearch");
        }
        builder.endObject();
        indexRequest.source(builder);
//        IndexRequest indexRequest2 = new IndexRequest("posts", "doc", "1")
//                .source("user", "kimchy",
//                        "postDate", new Date(),
//                        "message", "trying out Elasticsearch"); // (1)

//        indexRequest.routing("routing");
        indexRequest.timeout(TimeValue.timeValueSeconds(1));
        indexRequest.setRefreshPolicy(WriteRequest.RefreshPolicy.WAIT_UNTIL);
//        indexRequest.version(0);
//        indexRequest.versionType(VersionType.EXTERNAL);
        indexRequest.opType(DocWriteRequest.OpType.CREATE);
//        indexRequest.setPipeline("pipeline");
        try{
            IndexResponse indexResponse = client.index(indexRequest, RequestOptions.DEFAULT);
        }catch (Exception exception){
            if(!(exception.getMessage()).contains("OK")){
                System.out.println(" spring-boot 版本低了，没有做这方面的处理,但是索引创建成功了！");
                throw exception;
            }
            System.out.println("200 ok");
        }

        ActionListener<IndexResponse> listener = new ActionListener<IndexResponse>() {
            @Override
            public void onResponse(IndexResponse indexResponse) {
                // 成功
                String index = indexResponse.getIndex();
                String id = indexResponse.getId();
                long version = indexResponse.getVersion();
                if (indexResponse.getResult() == DocWriteResponse.Result.CREATED) {
                    // 当文档第一次创建后处理
                } else if (indexResponse.getResult() == DocWriteResponse.Result.UPDATED) {
                    // 当文档已存在，更新后处理
                }
                ReplicationResponse.ShardInfo shardInfo = indexResponse.getShardInfo();
                if (shardInfo.getTotal() != shardInfo.getSuccessful()) {
                    // 当处理成功的芬片数量不等于总数量时处理
                }
                if (shardInfo.getFailed() > 0) {
                    for (ReplicationResponse.ShardInfo.Failure failure :
                            shardInfo.getFailures()) {
                        String reason = failure.reason(); // 获取失败原因，并处理
                    }
                }


            }

            @Override
            public void onFailure(Exception e) {
                // 失败
                System.out.println("调用失败"+e.getMessage());
            }
        };
//        client.indexAsync(indexRequest, RequestOptions.DEFAULT, listener);
    }
}
