package com.app.base.net.parser;

import io.reactivex.annotations.NonNull;
import okhttp3.Response;

import java.io.IOException;

public interface Parser<T> {

    /**
     * 数据解析,Http请求成功后回调
     *
     * @param response Http执行结果
     * @return 解析后的对象类型
     * @throws IOException 网络异常、解析异常
     */
    T parse(@NonNull Response response) throws IOException;

}
