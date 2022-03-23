package com.movie.service;


import com.movie.dao.MovieDao;

import java.util.List;


public interface MovieService {

    //查询服务
    public List<MovieDao> searchMovie(String searchKeyWord);

    //修改服务，返回修改成功的列表
    public List<MovieDao> updateMovie(List<MovieDao> movieDaos);

    //添加服务，返回添加成功的个数
    public Integer insertMovies(List<MovieDao> movieDaos);

    //删除服务，返回删除成功个数
    public Integer deleteMovies(List<String> moviesIds);

    public List<MovieDao> searchWildCard(String searchKeyWord);
}
