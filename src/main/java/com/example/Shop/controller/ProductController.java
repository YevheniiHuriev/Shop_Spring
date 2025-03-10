package com.example.Shop.controller;

import com.example.Shop.Service.ProductService;
import com.example.Shop.model.Product;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
@RequestMapping("/products")
public class ProductController {
    private final ProductService productService;
    public ProductController(ProductService productService) {
        this.productService = productService;
    }
    @GetMapping
    public String read(Model model) {
        model.addAttribute("products", productService.getAllProducts());
        return "crud/read";
    }
    @GetMapping("/save")
    public String save(Model model) {
        model.addAttribute("product", new Product());
        return "crud/save";
    }
    @PostMapping("/save")
    public String save(@ModelAttribute Product product) {

        productService.addProduct(product);
        return "redirect:/products";
    }

    @GetMapping("/update/{id}")
    public String showEditProductPage(@PathVariable Long id, Model model) {
        Optional<Product> product = productService.getProductById(id);
        if (product.isPresent()) {
            model.addAttribute("product", product);
            return "crud/update";
        }
        return "redirect:/products";
    }

    @PostMapping("/update")
    public String updateProduct(@ModelAttribute Long id, Product product) {
        productService.updateProduct(id, product);
        return "redirect:/products";
    }

    @GetMapping("/delete/{id}")
    public String showDeleteProductPage(@PathVariable Long id, Model model) {
        Optional<Product> product = productService.getProductById(id);
        if (product.isPresent()) {
            model.addAttribute("product", product);
            return "crud/delete";
        }
        return "redirect:/products";
    }

    @PostMapping("/delete")
    public String deleteProduct(@RequestParam("id") Long id) {
        productService.deleteProduct(id);
        return "redirect:/products";
    }
}
