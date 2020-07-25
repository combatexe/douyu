/*
* Copyright (c) [2020] [jinjun lei]
* [douyu danmu] is licensed under Mulan PSL v2.
* You can use this software according to the terms and conditions of the Mulan PSL v2.
* You may obtain a copy of Mulan PSL v2 at:
*          http://license.coscl.org.cn/MulanPSL2
* THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
* EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
* MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
* See the Mulan PSL v2 for more details.
*/

package com.lei2j.douyu.web.controller;

import com.lei2j.douyu.core.constant.DateFormatConstants;
import com.lei2j.douyu.core.controller.BaseController;
import com.lei2j.douyu.qo.DanmuQuery;
import com.lei2j.douyu.qo.SearchPage;
import com.lei2j.douyu.service.es.ChatSearchService;
import com.lei2j.douyu.util.DateUtil;
import com.lei2j.douyu.util.DouyuUtil;
import com.lei2j.douyu.vo.DanmuSearchView;
import com.lei2j.douyu.vo.RoomDetailVo;
import com.lei2j.douyu.web.response.Pagination;
import com.lei2j.douyu.web.response.Response;
import com.lei2j.douyu.web.view.DanmuRankingView;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * author: lei2j
 * date: 2019/3/31
 */
@RestController
@RequestMapping("/danmu")
public class DanmuController extends BaseController {

    @Autowired
    private ChatSearchService chatSearchService;

    @GetMapping("")
    public Response find(@RequestParam(value = "limit",required = false,defaultValue = "15") Integer limit ,
                         @RequestParam(value = "pageNum",required = false,defaultValue = "1") Integer pageNum,
                         DanmuQuery danmuQuery){
        String ownerName = danmuQuery.getOwnerName();
        Integer roomId = danmuQuery.getRoomId();
        if (!StringUtils.isEmpty(ownerName)) {
            Optional<List<DouyuUtil.SearchRoomInfo>> roomInfoOptional = DouyuUtil.search(ownerName);
            if (!roomInfoOptional.isPresent()) {
                return Response.ok().entity(new Pagination<>(10, 1));
            }
            DouyuUtil.SearchRoomInfo searchRoomInfo = roomInfoOptional.get().get(0);
            roomId = searchRoomInfo.getRId();
        }else if(!StringUtils.isEmpty(roomId)){
            RoomDetailVo roomDetail = DouyuUtil.getRoomDetail(roomId);
            if(roomDetail==null){
                return Response.NOT_FOUND;
            }
            ownerName = roomDetail.getOwnerName();
        }

        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        if(!StringUtils.isEmpty(roomId)){
            boolQueryBuilder.must(QueryBuilders.termQuery("rid", roomId));
        }
        if(!StringUtils.isEmpty(danmuQuery.getNn())){
            boolQueryBuilder.must(QueryBuilders.termsQuery("nn", danmuQuery.getNn()));
        }
        if(danmuQuery.getStartDate()!=null){
            boolQueryBuilder.must(
                    QueryBuilders.rangeQuery("createAt")
                            .from(DateUtil.localDateTimeFormat(LocalDateTime.of(danmuQuery.getStartDate(), LocalTime.MIN)),true));
        }
        if(danmuQuery.getEndDate()!=null){
            boolQueryBuilder.must(
                    QueryBuilders.rangeQuery("createAt")
                            .format(DateFormatConstants.DATETIME_FORMAT)
                            .to(DateUtil.localDateTimeFormat(LocalDateTime.of(danmuQuery.getEndDate(), LocalTime.MAX)), true));
        }
        QueryBuilder queryBuilder = boolQueryBuilder;
        SearchPage searchPage = new SearchPage(queryBuilder,null);
        searchPage.setSort("createAt desc");
        Pagination<DanmuSearchView,SearchPage> pagination = new Pagination<>(limit,pageNum,searchPage);
        pagination = chatSearchService.queryDanmuByCondition(pagination);
        int total = pagination.getTotal();
        int maxShowTotal = 10000;
        pagination.setTotal(total>maxShowTotal?maxShowTotal:total);
        List<DanmuSearchView> items = pagination.getItems();
        if(!CollectionUtils.isEmpty(items)){
            for (DanmuSearchView item :
                    items) {
                item.setOwnerName(ownerName);
            }
        }
        return Response.ok().entity(pagination);
    }

    @GetMapping("/view/today/rankingList")
    public Response viewTodayDanmuRankingList(@RequestParam(value = "topSize",defaultValue = "10")Integer topSize){
        Map<String,Long> sumAggregation = chatSearchService.getTodayDanmuSumByRoomId(topSize);
        if(sumAggregation==null){
            return Response.INTERNAL_SERVER_ERROR;
        }
        List<DanmuRankingView> rankingViewList = sumAggregation.entrySet().parallelStream().map(entry -> {
            String roomId = entry.getKey();
            Long count = entry.getValue();
            RoomDetailVo roomDetailVo = DouyuUtil.getRoomDetail(Integer.valueOf(roomId));
            DanmuRankingView danmuRankingView = new DanmuRankingView();
            danmuRankingView.setRoomId(roomId);
            danmuRankingView.setCount(count);
            danmuRankingView.setNickName(roomDetailVo.getOwnerName());
            danmuRankingView.setRoomName(roomDetailVo.getRoomName());
            danmuRankingView.setRoomThumb(roomDetailVo.getRoomThumb());
            danmuRankingView.setRoomStatus(roomDetailVo.getRoomStatus());
            return danmuRankingView;
        }).collect(Collectors.toList());
        return Response.ok().entity(rankingViewList);
    }
}
