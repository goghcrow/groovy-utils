package com.youzan.et.groovy.shell;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class EvalResult {
    private String out;
    private Object ret;

    EvalResult(String out, Object ret) {
        this.out = out;
        this.ret = ret;
    }
}
