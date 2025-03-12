package com.example.Shop.controller;

import com.example.Shop.model.OrderDemo;
import com.example.Shop.model.OrderItem;
import com.example.Shop.model.Product;
import com.example.Shop.model.User;
import com.example.Shop.repository.OrderDemoRepository;
import com.example.Shop.repository.OrderItemRepository;
import com.example.Shop.repository.ProductRepository;
import com.example.Shop.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/orders")
public class OrderDemoController {
    private final OrderDemoRepository orderDemoRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Autowired
    public OrderDemoController(OrderDemoRepository orderDemoRepository, OrderItemRepository orderItemRepository, ProductRepository productRepository, UserRepository userRepository) {
        this.orderDemoRepository = orderDemoRepository;
        this.orderItemRepository = orderItemRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    public String getAllOrders(Model model) {
        List<OrderDemo> orders = orderDemoRepository.findAll();
        model.addAttribute("orders", orders);
        return "order/read";
    }

    @GetMapping("/save")
    public String createOrderForm(Model model) {
        List<Product> products = productRepository.findAll();
        List<User> users = userRepository.findAll();

        model.addAttribute("order", new OrderDemo());
        model.addAttribute("products", products);
        model.addAttribute("users", users);

        return "order/save";
    }

    @PostMapping("/save")
    public String createOrder(@RequestParam("userId") long id,
                              @RequestParam("productIds") List<Long> productIds,
                              @RequestParam("quantities") List<Integer> quantities) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isEmpty()) return "redirect:/orders?error=usernotfound";

        OrderDemo order = new OrderDemo();
        order.setUser(userOpt.get());

        BigDecimal totalPrice = BigDecimal.ZERO;

        if (order.getOrderItems() == null) {
            order.setOrderItems(new ArrayList<>());
        }

        for (int i = 0; i < productIds.size(); i++) {
            Optional<Product> productOpt = productRepository.findById(productIds.get(i));
            if (productOpt.isEmpty()) return "redirect:/orders?error=productnotfound";

            Product product = productOpt.get();
            int quantity = quantities.get(i);
            BigDecimal itemPrice = product.getPrice().multiply(BigDecimal.valueOf(quantity));

            OrderItem item = new OrderItem();
            item.setProduct(product);
            item.setQuantity(quantity);
            item.setPrice(itemPrice);
            item.setOrder(order);

            totalPrice = totalPrice.add(itemPrice);
            order.getOrderItems().add(item);
        }

        order.setTotal(totalPrice);
        orderDemoRepository.save(order);

        return "redirect:/orders";
    }

    @GetMapping("/update/{id}")
    public String editOrderForm(@PathVariable long id, Model model) {
        Optional<OrderDemo> orderOpt = orderDemoRepository.findById(id);
        if (orderOpt.isEmpty()) {
            return "redirect:/orders?error=notfound";
        }

        List<Product> products = productRepository.findAll();
        List<User> users = userRepository.findAll();

        model.addAttribute("order", orderOpt.get());
        model.addAttribute("products", products);
        model.addAttribute("users", users);

        return "order/update";
    }

    @PostMapping("/update/{id}")
    public String updateOrder(@PathVariable long id,
                              @RequestParam("userId") long userId,
                              @RequestParam(value = "productIds", required = false) List<Long> productIds,
                              @RequestParam(value = "quantities", required = false) List<Integer> quantities) {
        Optional<OrderDemo> existingOrderOpt = orderDemoRepository.findById(id);
        Optional<User> userOpt = userRepository.findById(userId);

        if (existingOrderOpt.isEmpty() || userOpt.isEmpty()) {
            return "redirect:/orders?error=notfound";
        }

        OrderDemo existingOrder = existingOrderOpt.get();
        existingOrder.setUser(userOpt.get());

        orderItemRepository.deleteAll(existingOrder.getOrderItems());
        existingOrder.getOrderItems().clear();
        existingOrder.setTotal(BigDecimal.ZERO);

        if (productIds != null && quantities != null) {
            for (int i = 0; i < productIds.size(); i++) {
                Optional<Product> productOpt = productRepository.findById(productIds.get(i));
                if (productOpt.isEmpty()) {
                    return "redirect:/orders?error=productnotfound";
                }

                Product product = productOpt.get();
                int quantity = quantities.get(i);
                BigDecimal itemPrice = product.getPrice().multiply(BigDecimal.valueOf(quantity));

                OrderItem item = new OrderItem();
                item.setProduct(product);
                item.setQuantity(quantity);
                item.setPrice(itemPrice);
                item.setOrder(existingOrder);

                existingOrder.getOrderItems().add(item);
                existingOrder.setTotal(existingOrder.getTotal().add(itemPrice));
            }
        }

        orderDemoRepository.save(existingOrder);
        return "redirect:/orders";
    }

    @GetMapping("/delete/{id}")
    public String deleteOrderForm(@PathVariable long id, Model model) {
        Optional<OrderDemo> orderOpt = orderDemoRepository.findById(id);
        if (orderOpt.isEmpty()) {
            return "redirect:/orders?error=notfound";
        }
        model.addAttribute("order", orderOpt.get());
        return "order/delete";
    }

    @PostMapping("/delete/{id}")
    public String deleteOrder(@PathVariable long id) {
        Optional<OrderDemo> orderOpt = orderDemoRepository.findById(id);
        if (orderOpt.isPresent()) {
            orderItemRepository.deleteAll(orderOpt.get().getOrderItems());
            orderDemoRepository.deleteById(id);
        }
        return "redirect:/orders";
    }


}
