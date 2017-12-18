package com.youzan.et.groovy.rulex.doc;

import java.util.Map;

/**
 * @author chuxiaofeng
 * 不需要实现, 仅作为接口签名示例
 */
@FunctionalInterface
public interface Action {
    void execute(Map<String, Object> factors) throws Exception;
}
