package com.meiseguo.api.pojo;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Document(collection ="head")
public class Head {
    @Id
    String name;
    String source;
    List<ApiHead> values;
}
