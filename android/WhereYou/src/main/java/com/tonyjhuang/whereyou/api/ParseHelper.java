package com.tonyjhuang.whereyou.api;

import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseInstallation;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by tony on 4/29/15.
 */
public class ParseHelper {

    public void checkName(String name, FunctionCallback<Boolean> callback) {
        Map<String, Object> params = new HashMap<>();
        params.put("name", name.toLowerCase());
        ParseCloud.callFunctionInBackground("checkName", params, callback);
    }

    public void updateName(String name) {
        ParseInstallation currentInstallation = ParseInstallation.getCurrentInstallation();
        currentInstallation.put("name", name);
        currentInstallation.put("nameLowercase", name.toLowerCase());
        currentInstallation.saveInBackground();
    }
}
