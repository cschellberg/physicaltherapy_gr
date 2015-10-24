package com.agileapps.pt.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by dschellb on 10/10/2015.
 */
public class MapOfMaps {
    private Map<String,Map> rootMap=new HashMap<String,Map>();

    public void add(String  keys[]){
             add(0,keys,rootMap);
     }

    private void add(int index, String keys[], Map<String,Map> outerMap){
          if ( index >= keys.length ){
              return;
          }
          Map innerMap=outerMap.get(keys[index]);
          if ( innerMap == null ){
              innerMap=new HashMap<String,Map>();
              outerMap.put(keys[index],innerMap);
          }
          add(++index,keys,innerMap);
    }

    public Map<String,Map> get(String  keys[]){
        return get(0,keys,rootMap);
    }

    private Map<String,Map> get(int index,String keys[],Map<String,Map> outerMap){
        Map<String,Map> innerMap=outerMap.get(keys[index]);
        if ( index >= (keys.length-1) || innerMap == null ){
            return innerMap;
        }else {
            return get(++index,keys,innerMap);
        }
    }

    public Map<String,Map> get(){
        return rootMap;
    }


    public static void main(String ars[]){
       MapOfMaps mapOfMaps=new MapOfMaps();
        mapOfMaps.add(new String[]{"red","white","blue","green"});
        Map<String,Map> result=mapOfMaps.get(new String[]{"red","white","blue"});
        System.out.println(result);
    }

}
