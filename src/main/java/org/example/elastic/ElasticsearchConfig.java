/*
package org.example.elastic;

import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.elasticsearch.client.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

@Configuration
public class ElasticsearchConfig {
    @Value("${mylasticsearch.elasticsearch.hostlist}")
    private String hostlist;

    @Bean
    public RestHighLevelClient restHighLevelClient() {
        // 解析hostlist 信息
        String[] split = hostlist.split(",");
        // 创建HttpHost数组  封装es的主机和端口
        HttpHost[] httpHosts = new HttpHost[split.length];
        for (int i = 0; i < split.length; i++) {
            String iterm = split[i];
            httpHosts[i] = new HttpHost(iterm.split(":")[0], Integer.parseInt(iterm.split(":")[1]), "http");
        }
        RestClientBuilder restClientBuilder = RestClient.builder(httpHosts);

        restClientBuilder.setFailureListener(new RestClient.FailureListener() {
            @Override
            public void onFailure(Node node) {
                super.onFailure(node);
                System.out.println("出错的节点: " + node);
            }
        });

        // 定义节点选择器 这个是跳过data=false，ingest为false的节点
        restClientBuilder.setNodeSelector(NodeSelector.SKIP_DEDICATED_MASTERS);
        // 定义默认请求配置回调
        restClientBuilder.setRequestConfigCallback(new RestClientBuilder.RequestConfigCallback() {
            @Override
            public RequestConfig.Builder customizeRequestConfig(RequestConfig.Builder requestConfigBuilder) {
                return requestConfigBuilder.setConnectTimeout(90000) // 连接超时（默认为1秒）
                        .setSocketTimeout(30000); // 套接字超时（默认为30秒）
            }
        });
        
       	//设置线程数
        //一般线程数与本地检测到的处理器数量相同，线程数主要取决于Runtime.getRuntime（）.availableProcessors（）返回的结果
        int number = 3;
        restClientBuilder.setHttpClientConfigCallback(new RestClientBuilder.HttpClientConfigCallback() {
            @Override
            public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpAsyncClientBuilder) {
                return httpAsyncClientBuilder.setDefaultIOReactorConfig(IOReactorConfig.custom().setIoThreadCount(number).build());
            }
        });

        return new RestHighLevelClient(restClientBuilder);
    }

    ///项目主要使用RestHighLevelClient，对于低级的客户端暂时不用
    // 注意执行完要把这个对象.close(); 
    @Bean
    public RestClient restClient() {
        // 解析hostlist 信息
        String[] split = hostlist.split(",");
        // 创建HttpHost数组  封装es的主机和端口
        HttpHost[] httpHosts = new HttpHost[split.length];
        for (int i = 0; i < split.length; i++) {
            String iterm = split[i];
            httpHosts[i] = new HttpHost(iterm.split(":")[0], Integer.parseInt(iterm.split(":")[1]), "http");
        }
        RestClientBuilder restClientBuilder = RestClient.builder(httpHosts);

        restClientBuilder.setFailureListener(new RestClient.FailureListener() {
            @Override
            public void onFailure(Node node) {
                super.onFailure(node);
                System.out.println("出错的节点: " + node);
            }
        });
        
        // 定义节点选择器 这个是跳过data=false，ingest为false的节点
        restClientBuilder.setNodeSelector(NodeSelector.SKIP_DEDICATED_MASTERS);
        // 定义默认请求配置回调
        restClientBuilder.setRequestConfigCallback(new RestClientBuilder.RequestConfigCallback() {
            @Override
            public RequestConfig.Builder customizeRequestConfig(RequestConfig.Builder requestConfigBuilder) {
                return requestConfigBuilder.setConnectTimeout(90000) // 连接超时（默认为1秒）
                        .setSocketTimeout(30000); // 套接字超时（默认为30秒）
            }
        });
        
        //设置线程数
        //一般线程数与本地检测到的处理器数量相同，线程数主要取决于Runtime.getRuntime（）.availableProcessors（）返回的结果
        int number = 3;
        restClientBuilder.setHttpClientConfigCallback(new RestClientBuilder.HttpClientConfigCallback() {
            @Override
            public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpAsyncClientBuilder) {
                return httpAsyncClientBuilder.setDefaultIOReactorConfig(IOReactorConfig.custom().setIoThreadCount(number).build());
            }
        });
        
        return restClientBuilder.build();
    }
}*/
