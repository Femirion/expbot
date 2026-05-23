package com.femirion.expbot.expbot.`in`.controller

import com.femirion.expbot.expbot.domain.entity.Category
import com.femirion.expbot.expbot.domain.entity.User
import com.femirion.expbot.expbot.service.CategoryService
import com.femirion.expbot.expbot.service.UserService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/categories")
class CategoryController(
    val categoryService: CategoryService,
) {

    @GetMapping("/")
    fun getCategories(): ResponseEntity<List<Category>> {
        return ResponseEntity.ok(categoryService.getAll())
    }

    @GetMapping("/{id}")
    fun getCategory(@PathVariable("id") id: Long): ResponseEntity<Category> {
        return ResponseEntity.ok(categoryService.getCategory(id))
    }

    @PostMapping("/create")
    fun createCategory(@RequestBody category: Category): ResponseEntity<Category> {
        return ResponseEntity.ok(categoryService.createCategory(category))
    }

    @DeleteMapping("/{id}")
    fun deleteById(@PathVariable("id") id: Long): ResponseEntity<Unit> {
        return ResponseEntity.ok(categoryService.deleteById(id))
    }
}