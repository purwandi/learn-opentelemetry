package com.purwandi.product.rest

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import com.purwandi.product.http.response.MessageResponse;
import com.purwandi.product.http.response.ProductResponse;
import com.purwandi.product.models.Message;

@RestController
@RequestMapping("/messages")
class MessageController {

    @GetMapping("/")
    fun index(): String {
        return "Hello World";
    }

    @GetMapping("/{id}")
    fun get(): Message {
        return Message("1", "Purwandi")
    }

    @GetMapping("/{id}/messages")
    fun getMessage(): MessageResponse<List<Message>> {
        return MessageResponse(
            listOf(
                Message("1", "Purwandi"),
                Message("2", "Hazel")
            ),
            "error"
        )
    }

    @GetMapping("/{id}/product")
    fun getProduct(): ProductResponse<List<Message>, String> {
        return ProductResponse(
            listOf(
                Message("1", "Purwandi"),
                Message("2", "Hazel")
            ),
            "error"
        )
    }
}
