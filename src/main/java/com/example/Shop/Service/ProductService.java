package com.example.Shop.Service;
import com.example.Shop.model.Product;
import com.example.Shop.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService {
    @Autowired
    private ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }
    public Optional<Product> getProductById(long id) {
        return productRepository.findById(id);
    }
    public List<Product> searchProductsByName(String name) {
        return productRepository.findByNameContaining(name);
    }
    public Product addProduct(Product product) {
        return productRepository.save(product);
    }
    public Product updateProduct(Long id, Product product) {
        product.setId(id);
        return productRepository.save(product);
    }
    public void deleteProduct(long id) {
        productRepository.deleteById(id);
    }
}
