package com.challenge.desafioanotaai.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.challenge.desafioanotaai.domain.category.Category;
import com.challenge.desafioanotaai.domain.category.CategoryDTO;
import com.challenge.desafioanotaai.domain.category.exceptions.CategoryNotFoundException;
import com.challenge.desafioanotaai.repositories.CategoryRepository;
import com.challenge.desafioanotaai.services.aws.AwsSnsService;
import com.challenge.desafioanotaai.services.aws.MessageDTO;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository repository;
    private final AwsSnsService snsService;

    public Category insert(CategoryDTO categoryData){
        Category newCategory = new Category(categoryData);

        this.repository.save(newCategory);

        this.snsService.publish(new MessageDTO(newCategory.toString()));

        return newCategory;
    }

    public Category update(String id, CategoryDTO categoryData){
        Category category = this.repository.findById(id)
                .orElseThrow(CategoryNotFoundException::new);

        if(!categoryData.title().isEmpty()) category.setTitle(categoryData.title());
        if(!categoryData.description().isEmpty()) category.setDescription(categoryData.description());

        this.snsService.publish(new MessageDTO(category.toString()));

        this.repository.save(category);

        return category;
    }

    public void delete(String id){
        Category category = this.repository.findById(id)
                .orElseThrow(CategoryNotFoundException::new);

        this.repository.delete(category);
        this.snsService.publish(new MessageDTO(category.deleteToString()));
    }

    public List<Category> getAll(){
        return this.repository.findAll();
    }

    public Optional<Category> getById(String id){
        return this.repository.findById(id);
    }
}
