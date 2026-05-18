package com.leroy.store.controllers;

import com.leroy.store.dtos.ProductDto;
import com.leroy.store.entities.Product;
import com.leroy.store.mappers.ProductMapper;
import com.leroy.store.repositories.CategoryRepository;
import com.leroy.store.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final CategoryRepository categoryRepository;

    @GetMapping
    public ResponseEntity<List<ProductDto>> getAllProducts (
            @RequestParam(required = false, name = "categoryId", defaultValue = "") UUID categoryId) {
        List<Product> products;
        if (categoryId != null) {
            products = productRepository.findAllByCategory_Id(categoryId);
        } else {
            products = productRepository.findAll();
        }

        return ResponseEntity.ok(products.stream().map(productMapper::toDto).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDto> getProductById (@PathVariable UUID id) {
        var product = productRepository.findById(id);
        return product.map(prod -> ResponseEntity.ok(productMapper.toDto(prod))).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    @Transactional
    public  ResponseEntity<?> createProduct(@RequestBody ProductDto productDto, UriComponentsBuilder uriComponentsBuilder) {
        var product = productMapper.toEntity(productDto);
        var category = categoryRepository.findById(productDto.getCategoryId());
        if (category.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Category not found"));

        }

        product.setCategory(category.get());
        var savedProduct = productRepository.save(product);
        var uri = uriComponentsBuilder.path("/products/{id}").buildAndExpand(savedProduct.getId()).toUri();
        return ResponseEntity.created(uri).body(productMapper.toDto(savedProduct));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateProduct(@PathVariable UUID id, @RequestBody ProductDto productDto) {
        var product = productRepository.findById(id);

        var category = categoryRepository.findById(productDto.getCategoryId());
        if (category.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Category not found"));
        }

        return product
                .map(prod ->  {
                    productMapper.updateProduct(productDto, prod);
                    prod.setCategory(category.get());
                    return ResponseEntity.ok(productMapper.toDto(productRepository.save(prod)));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable UUID id) {
        var product = productRepository.findById(id);
        return product
                .map(prod -> {
                    productRepository.delete(prod);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElseGet(() -> ResponseEntity.notFound().build());

    }
}
