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


    private fun errorResponse(status: HttpStatus, message: String, request: HttpServletRequest): ResponseEntity<Any> =
        ResponseEntity.status(status).body(
            ErrorResponseBody(
                timestamp = LocalDateTime.now(),
                status = status.value(),
                error = message,
                path = request.requestURI
            )
        )

    // Consolidated validation function
    private fun validateProductDetails(details: ProductDetails, request: HttpServletRequest): ResponseEntity<Any>? {
        // Validate product name: must not be blank, contain any digits,
        // or equal "true"/"false" (case-insensitive)
        if (details.name.isBlank() ||
            details.name.any { it.isDigit() } ||
            details.name.equals("true", ignoreCase = true) ||
            details.name.equals("false", ignoreCase = true)
        ) {
            return errorResponse(HttpStatus.BAD_REQUEST, "Product name cannot be blank or contain numbers", request)
        }
        // Validate product type
        if (details.type !in allowedTypes) {
            return errorResponse(HttpStatus.BAD_REQUEST, "Invalid product type: ${details.type}", request)
        }
        // Validate inventory range
        if (details.inventory !in 1..9999) {
            return errorResponse(HttpStatus.BAD_REQUEST, "Inventory must be between 1 and 9999", request)
        }
        // Validate cost: it must be provided and non-negative
        if (details.cost == null) {
            return errorResponse(HttpStatus.BAD_REQUEST, "Cost must be provided", request)
        }
        if (details.cost < 0.0) {
            return errorResponse(HttpStatus.BAD_REQUEST, "Cost must be non-negative", request)
        }
        return null
    }

    @PostMapping
    fun createProduct(
        @RequestBody productDetails: ProductDetails,
        request: HttpServletRequest
    ): ResponseEntity<Any> {
        // Use the validation helper to check the input
        validateProductDetails(productDetails, request)?.let { return it }

        val id = productIdGenerator.getAndIncrement()
        val product = Product(
            id = id,
            name = productDetails.name,
            type = productDetails.type,
            inventory = productDetails.inventory,
            cost = productDetails.cost!!
        )
        productsMap[id] = product
        return ResponseEntity.status(HttpStatus.CREATED).body(ProductId(id))
    }

    /**
     * GET /products?type={type}
     * Returns:
     * - 200 with a list of products.
     * - 400 if an invalid product type is provided.
     */
    @GetMapping
    fun getProducts(
        @RequestParam(required = false) type: String?,
        request: HttpServletRequest
    ): ResponseEntity<Any> {
        if (type != null && type !in allowedTypes) {
            return errorResponse(HttpStatus.BAD_REQUEST, "Invalid product type: $type", request)
        }
        val result = if (type != null) {
            productsMap.values.filter { it.type.equals(type, ignoreCase = true) }
        } else {
            productsMap.values.toList()
        }
        return ResponseEntity.ok(result)
    }
}
