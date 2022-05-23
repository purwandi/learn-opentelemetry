package com.purwandi.product.http.response

data class ProductResponse<T, S> (
    val data: T,
    val error: S
)
