package com.store.controllers

import com.store.models.*
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

@RestController
@RequestMapping("/products")
class Products {

    // Allowed product types per your OpenAPI spec
    private val allowedTypes = setOf("book", "food", "gadget", "other")

    // In-memory store for products
    private val productsMap = ConcurrentHashMap<Int, Product>()
    private val productIdGenerator = AtomicInteger(1)

    /**
     * POST /products
     * Returns:
     * - 201 with ProductId on success.
     * - 400 with an ErrorResponseBody on invalid input.
     */
    @PostMapping
    fun createProduct(
        @RequestBody productDetails: ProductDetails,
        request: HttpServletRequest
    ): ResponseEntity<Any> {
        // Validate product name: Check if blank, contains digits,
        // or equals "true"/"false" (case-insensitive)
        val name = productDetails.name
        if (name.isBlank() ||
            name.any { it.isDigit() } ||
            name.equals("true", ignoreCase = true) ||
            name.equals("false", ignoreCase = true)
        ) {
            val error = ErrorResponseBody(
                timestamp = LocalDateTime.now(),
                status = HttpStatus.BAD_REQUEST.value(),
                error = "Product name cannot be blank or contain numbers",
                path = request.requestURI
            )
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error)
        }
        // Validate product type
        if (productDetails.type !in allowedTypes) {
            val error = ErrorResponseBody(
                timestamp = LocalDateTime.now(),
                status = HttpStatus.BAD_REQUEST.value(),
                error = "Invalid product type: ${productDetails.type}",
                path = request.requestURI
            )
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error)
        }
        // Validate inventory range
        if (productDetails.inventory < 1 || productDetails.inventory > 9999) {
            val error = ErrorResponseBody(
                timestamp = LocalDateTime.now(),
                status = HttpStatus.BAD_REQUEST.value(),
                error = "Inventory must be between 1 and 9999",
                path = request.requestURI
            )
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error)
        }
        // Create new product
        val id = productIdGenerator.getAndIncrement()
        val product = Product(
            id = id,
            name = name,
            type = productDetails.type,
            inventory = productDetails.inventory
        )
        productsMap[id] = product

        return ResponseEntity.status(HttpStatus.CREATED).body(ProductId(id))
    }


}
