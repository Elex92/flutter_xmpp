package com.elex.flutter_xmpp;

import java.util.HashMap;
import java.util.Map;

public class FYResult {

   static Map connectSuccess() {
        HashMap map = new HashMap();
        map.put("code","200");
        map.put("message","连接成功");
        return map;
    }

    static Map paramsComplete(){
        HashMap map = new HashMap();
        map.put("code","200");
        map.put("message","参数齐全");
        return map;
    }
}
