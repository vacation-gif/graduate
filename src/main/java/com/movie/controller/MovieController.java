package com.movie.controller;


import com.movie.dao.MovieDao;
import com.movie.service.MovieService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
public class MovieController {

    @Autowired
    private MovieService movieService;

    @ApiOperation("电影搜索接口，传入参数为MovieDao")
    @GetMapping("/movieSearch")
    public List<MovieDao> searchMovie(@RequestParam String searchKeyWord){
        return movieService.searchMovie(searchKeyWord);
    }

    @ApiOperation("批量更新电影信息接口，传入参数为列表")
    @PutMapping("/movieUpdate")
    public List<MovieDao> updateMovie(@RequestBody List<MovieDao> movieDaos){
        return movieService.updateMovie(movieDaos);
    }

    @ApiOperation("批量添加电影接口")
    @PostMapping("/insertMovie")
    public Integer insertMovies(@RequestBody List<MovieDao> movieDaos){
        return movieService.insertMovies(movieDaos);
    }

    @ApiOperation("批量删除接口")
    @DeleteMapping("/deleteMovies")
    public Integer deleteMovies(@RequestBody List<String> movieIds){
        return movieService.deleteMovies(movieIds);
    }

    @ApiOperation("es模糊查询")
    @GetMapping("/movieWildSearch")
    public List<MovieDao> searchWildCard(@RequestBody String searchKeyWord){
        return movieService.searchWildCard(searchKeyWord);
    }

}
