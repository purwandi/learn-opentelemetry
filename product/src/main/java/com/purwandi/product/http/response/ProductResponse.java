package com.purwandi.product.http.response;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.AccessLevel;

@Data
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class ProductResponse {
    String id;
    String name;
    String description;
}
