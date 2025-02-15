package com.store.models

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime

/**
 * Matches `ProductDetails` from the OpenAPI schema:
 *   required: [name, type, inventory, cost]
 *   inventory range: [1..9999]
 *   cost must be provided and non-negative.
 *
 * The cost property is declared as nullable (Double?) so that missing or null cost values can be validated in the controller.
 */
data class ProductDetails @JsonCreator constructor(
    @JsonProperty("name") val name: String,
    @JsonProperty("type") val type: String,
    @JsonProperty("inventory") val inventory: Int
)

/**
 * Matches `ProductId` from the OpenAPI schema:
 *   required: [id]
 */
data class ProductId(
    val id: Int
)

/**
 * Matches `Product` from the OpenAPI schema:
 *   allOf: [ProductId, ProductDetails]
 *
 * In a valid product, cost is non-null.
 */
data class Product(
    val id: Int,
    val name: String,
    val type: String,
    val inventory: Int
)

/**
 * Matches `ErrorResponseBody` from the OpenAPI schema.
 */
data class ErrorResponseBody(
    val timestamp: LocalDateTime,
    val status: Int,
    val error: String,
    val path: String
)
