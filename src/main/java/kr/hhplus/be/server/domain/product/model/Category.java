package kr.hhplus.be.server.layered.product.model;

import jakarta.persistence.*;
import lombok.Getter;

import java.util.List;

@Entity
@Table(name = "category")
@Getter
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long categoryId;

    private String categoryName;

    protected Category() {};

    public Category create(String categoryName) {
        Category category = new Category();
        category.categoryName = categoryName;
        return category;
    }

}
