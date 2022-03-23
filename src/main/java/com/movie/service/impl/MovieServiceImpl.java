package com.movie.service.impl;

import com.alibaba.fastjson.JSON;
import com.movie.dao.MovieDao;
import com.movie.service.MovieService;
import com.movie.utils.CommonUtils;
import org.apache.http.HttpHost;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.*;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MovieServiceImpl implements MovieService {

    @Value("${es.index:movie_test}")
    private String index;

    private static RestClientBuilder restClientBuilder;
    private static RestHighLevelClient client = null;
    private static HttpHost[] esHosts = new HttpHost[]{
            new HttpHost("localhost", 9200)
    };
    static {
        restClientBuilder = RestClient.builder(esHosts);
        client = new RestHighLevelClient(restClientBuilder);
    }

    /**
     * 查询电影
     * @param searchKeyWord
     * @return
     */
    @Override
    public List<MovieDao> searchMovie(String searchKeyWord) {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices(index);
        MovieDao movieDao = new MovieDao();
        movieDao.setMovieName(searchKeyWord);
        movieDao.setMovieFilmer(searchKeyWord);
        searchSourceBuilder.from(0);
        searchSourceBuilder.size(100);
        searchSourceBuilder.query(buildQuery(movieDao));
        searchRequest.source(searchSourceBuilder);
        SearchResponse response = null;
        try {
            response = client.search(searchRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return hitsToList(response);
    }

    /**
     * 修改电影信息
     * @param movieDaos
     * @return
     */
    @Override
    public List<MovieDao> updateMovie(List<MovieDao> movieDaos) {
        //更新列表中所有对象
        for(MovieDao movieDao:movieDaos){
            Map<String,Object> movieMap = new HashMap<>();
            try {
                movieMap = CommonUtils.objectToMap(movieDao);
            } catch (IllegalAccessException e) {

            }
            UpdateRequest updateRequest = new UpdateRequest(index,"_doc",movieDao.getId()).doc(movieMap);
        }
        return null;
    }

    /**
     * 添加电影
     */
    @Override
    public Integer insertMovies(List<MovieDao> movieDaos) {
        BulkRequest request = new BulkRequest();
        for(MovieDao movieDao:movieDaos){
            Map<String, Object> map = new HashMap<>();
            try {
                map = CommonUtils.objectToMap(movieDao);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            request.add(new IndexRequest(index, "_doc").source(map, XContentType.JSON));
        }
        BulkResponse bulk = null;
        try {
            bulk = client.bulk(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(bulk==null)return 0;
        return bulk.getItems().length;
    }

    /**
     * 批量删除电影接口
     * @param moviesIds
     * @return
     */
    @Override
    public Integer deleteMovies(List<String> moviesIds) {
        DeleteByQueryRequest request = new DeleteByQueryRequest(index);
        BulkRequest bulkRequest = new BulkRequest();
        for (String movieId:moviesIds){
            DeleteRequest deleteRequest = new DeleteRequest(index,"_doc",movieId);
            bulkRequest.add(deleteRequest);
        }
        BulkResponse bulkResponse = null;
        try {
            bulkResponse = client.bulk(bulkRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            System.out.println("不想导log4j的包，先这样吧");
        }
        if(bulkResponse==null) return 0;
        return bulkResponse.getItems().length;
    }

    /**
     * 根据电影名模糊搜索
     * @param searchKeyWord
     * @return
     */
    @Override
    public List<MovieDao> searchWildCard(String searchKeyWord) {
        QueryBuilder builder = QueryBuilders.wildcardQuery("movie_name.keyword", "*"+searchKeyWord+"*");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices(index);
        searchSourceBuilder.query(builder);
        SearchResponse response = null;
        try {
            response = client.search(searchRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        List<MovieDao> movieDaos = hitsToList(response);
        return movieDaos;
    }

    /**
     * 根据有的值，向QueryBuilder对象中添加查询条件
     * @param movieDao
     * @return
     */
    private QueryBuilder buildQuery(MovieDao movieDao){
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        if(movieDao.getMovieFilmer()!=null&&!movieDao.getMovieFilmer().isEmpty()){
            MatchQueryBuilder matchQueryBuilder = QueryBuilders.matchQuery("movie_filmer",movieDao.getMovieFilmer());
            boolQueryBuilder.should(matchQueryBuilder);
        }
        if(movieDao.getMovieName()!=null&&!movieDao.getMovieName().isEmpty()){
            MatchQueryBuilder matchQueryBuilder = QueryBuilders.matchQuery("movie_name",movieDao.getMovieName());
            boolQueryBuilder.should(matchQueryBuilder);
        }
        return boolQueryBuilder;
    }


    private List<MovieDao> hitsToList(SearchResponse response){
        List<MovieDao> movieDaos = new ArrayList<>();
        if(response==null)return null;
        if(response.getHits()!=null){
            for(SearchHit hitSource:response.getHits().getHits()){
                MovieDao resMovieDao = JSON.parseObject(hitSource.getSourceAsString(),MovieDao.class);
                resMovieDao.setId(hitSource.getId());
                movieDaos.add(resMovieDao);
            }
        }
        return movieDaos;
    }
}
