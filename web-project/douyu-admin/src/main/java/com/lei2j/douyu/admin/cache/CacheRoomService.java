package com.lei2j.douyu.admin.cache;

import com.lei2j.douyu.admin.danmu.service.DouyuLogin;

import java.util.Map;

/**
 * @author lei2j
 * Created by lei2j on 2018/8/19.
 */
public interface CacheRoomService {

     /**
      * 获取房间
      * @param room room
      * @return DouyuLogin
      */
     DouyuLogin get(Integer room);

     /**
      * 缓存房间
      * @param room room
      * @param value value
      */
     void cache(Integer room, DouyuLogin value);

     /**
      * 移除房间
      * @param room room
      */
     void remove(Integer room);

     /**
      * 判断是否存在
      * @param room room
      * @return boolean
      */
     boolean containsKey(Integer room);

     /**
      * 获取所有房间
      * @return Map
      */
     Map<Integer, DouyuLogin> getAll();
}
