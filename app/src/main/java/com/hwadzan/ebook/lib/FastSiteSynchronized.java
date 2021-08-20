package com.hwadzan.ebook.lib;

import java.util.HashMap;

public class FastSiteSynchronized {
    public enum State{
        AllFail, NoAllFail, Sucess
    }

    HashMap<String, String> resultMap;
    public boolean sucess = false;
    public FastSiteSynchronized(String[] urls){
        resultMap = new HashMap<>();
        for(String url : urls){
            resultMap.put(url, "@$%null%$@");
        }
    }

    /**
     * 如果全部都失败后返回true
     * @param url
     * @return
     */
    public State putFail(String url) {
        synchronized (this) {
            if (sucess) {
                return State.Sucess; //已经有成功的
            } else {
                resultMap.put(url, "@$%Fail%$@");
                if (resultMap.values().contains("@$%null%$@")) {
                    return State.NoAllFail;
                } else {
                    return State.AllFail;
                }
            }
        }
    }

    /**
     * 如果是第一个成功的，就返回true
     * @param url
     * @param result
     * @return
     */
    public boolean putSucess(String url, String result){
        synchronized(this) {
            if (sucess) {
                return false;
            } else {
                sucess = true;
                return true;
            }
        }
    }
}
