package com.challenge.desafioanotaai.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.challenge.desafioanotaai.domain.category.Category;
import com.challenge.desafioanotaai.domain.category.CategoryDTO;
import com.challenge.desafioanotaai.domain.category.exceptions.CategoryNotFoundException;
import com.challenge.desafioanotaai.repositories.CategoryRepository;
import com.challenge.desafioanotaai.services.aws.AwsSnsService;
import com.challenge.desafioanotaai.services.aws.MessageDTO;

import java.util.Optional;

import static com.challenge.desafioanotaai.utils.MockCategories.CATEGORY_ID;
import static com.challenge.desafioanotaai.utils.MockCategories.DESCRIPTION;
import static com.challenge.desafioanotaai.utils.MockCategories.OWNER_ID;
import static com.challenge.desafioanotaai.utils.MockCategories.TITLE;
import static com.challenge.desafioanotaai.utils.MockCategories.mockCategoryEntity;
import static com.challenge.desafioanotaai.utils.MockCategories.mockCategoryList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository repository;
    @Mock
    private AwsSnsService snsService;

    @InjectMocks
    private CategoryService service;


    @Test
    void insertValidData() {
        final var input = new CategoryDTO("title", "description", "ownerId");
        final var category = new Category(input);
        given(repository.save(category)).willReturn(category);

        final var actual = service.insert(input);

        assertEquals(category, actual);
        then(snsService).should().publish(new MessageDTO(category.toString()));
    }

    @Test
    void updateNonexistentCategory() {
       assertThrows(CategoryNotFoundException.class,
           () -> service.update("id", null));
    }

    @Test
    @DisplayName("should delete Category when exists")
    void deleteSuccess() {
        final var category = mockCategoryEntity();
        given(repository.findById(CATEGORY_ID)).willReturn(Optional.of(category));

        this.service.delete(CATEGORY_ID);

        then(repository).should().delete(category);
    }

    @Test
    @DisplayName("should throw exception when Category not exists")
    void deleteError() {
        given(repository.findById(CATEGORY_ID)).willReturn(Optional.empty());

        assertThrows(CategoryNotFoundException.class, () -> {
            this.service.delete(CATEGORY_ID);
        });
    }

    @Test
    @DisplayName("should return a Category List on getAll")
    void getAllSuccess() {
        given(repository.findAll()).willReturn(mockCategoryList());

        final var result = this.service.getAll();

        assertNotNull(result);
        assertEquals(result.get(0).getOwnerId(), OWNER_ID);
        assertEquals(result.get(0).getTitle(), TITLE);
        assertEquals(result.get(0).getDescription(), DESCRIPTION);
    }

    @Test
    void getById() {
    }
}