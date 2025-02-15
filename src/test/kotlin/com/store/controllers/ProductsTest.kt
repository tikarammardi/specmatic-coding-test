package com.store.controllers

import com.fasterxml.jackson.databind.ObjectMapper
import com.store.models.ProductDetails
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@SpringBootTest
@AutoConfigureMockMvc
class ProductsTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Test
    fun `POST valid product returns 201 Created`() {
        val productDetails = ProductDetails(
            name = "iPhone",
            type = "gadget",
            inventory = 100,
            cost = 699.99
        )
        mockMvc.perform(
            post("/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productDetails))
        )
            .andExpect(status().isCreated)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").exists())
    }

    @Test
    fun `POST product with blank name returns 400 Bad Request`() {
        val productDetails = ProductDetails(
            name = "  ",
            type = "gadget",
            inventory = 100,
            cost = 699.99
        )
        mockMvc.perform(
            post("/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productDetails))
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `POST product with name containing digits returns 400 Bad Request`() {
        val productDetails = ProductDetails(
            name = "iPhone 12",
            type = "gadget",
            inventory = 100,
            cost = 699.99
        )
        mockMvc.perform(
            post("/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productDetails))
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `POST product with invalid type returns 400 Bad Request`() {
        val productDetails = ProductDetails(
            name = "iPhone",
            type = "invalid",
            inventory = 100,
            cost = 699.99
        )
        mockMvc.perform(
            post("/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productDetails))
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `POST product with inventory below range returns 400 Bad Request`() {
        val productDetails = ProductDetails(
            name = "iPhone",
            type = "gadget",
            inventory = 0,
            cost = 699.99
        )
        mockMvc.perform(
            post("/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productDetails))
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `POST product with inventory above range returns 400 Bad Request`() {
        val productDetails = ProductDetails(
            name = "iPhone",
            type = "gadget",
            inventory = 10000,
            cost = 699.99
        )
        mockMvc.perform(
            post("/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productDetails))
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `POST product with null cost returns 400 Bad Request`() {
        // Because our ProductDetails declares cost as nullable,
        // we need to simulate a JSON with cost as null.
        val json = """
            {
                "name": "iPhone",
                "type": "gadget",
                "inventory": 100,
                "cost": null
            }
        """.trimIndent()
        mockMvc.perform(
            post("/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `POST product with negative cost returns 400 Bad Request`() {
        val productDetails = ProductDetails(
            name = "iPhone",
            type = "gadget",
            inventory = 100,
            cost = -10.0
        )
        mockMvc.perform(
            post("/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productDetails))
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `GET products returns list of products`() {
        // First, create a valid product so that GET returns data
        val productDetails = ProductDetails(
            name = "iPhone",
            type = "gadget",
            inventory = 100,
            cost = 699.99
        )
        mockMvc.perform(
            post("/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productDetails))
        )
            .andExpect(status().isCreated)

        mockMvc.perform(get("/products"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$").isArray)
            .andExpect(jsonPath("$[0].id").exists())
    }

    @Test
    fun `GET products with invalid type returns 400`() {
        mockMvc.perform(get("/products").param("type", "invalid"))
            .andExpect(status().isBadRequest)
    }
}
