package com.purwandi.product.http.response

data class MessageResponse<T> (
    val data: T,
    val error: String
)
