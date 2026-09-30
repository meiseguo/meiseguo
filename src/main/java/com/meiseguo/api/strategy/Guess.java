package com.meiseguo.api.strategy;

import com.meiseguo.api.pojo.Case;
import com.meiseguo.api.pojo.Operator;
import lombok.Data;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Data
public class Guess implements Predicate<Case> {
    List<Case> list;
    String traceId;
    String operator;
    public Guess(Operator operator) {
        this.operator = operator.operator;
        this.list = new ArrayList<>();
        this.traceId = new ObjectId().toHexString();
    }

    @Override
    public boolean test(Case when) {
        when.setTraceid(traceId);
        when.setOperator(operator);
        list.add(when);
        return when.isResult();
    }

    @Override
    public String toString() {
        return list.stream().map(when -> when.getDesc() + (when.isResult()?"✅":"×")).collect(Collectors.joining("/"));
    }

    public void clear() {
        list.clear();
    }
}


