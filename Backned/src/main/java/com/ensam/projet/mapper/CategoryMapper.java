package com.ensam.projet.mapper;

import com.ensam.projet.dto.response.CategoryResponse;
import com.ensam.projet.entity.Category;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    @org.mapstruct.Mapping(target = "bookCount", expression = "java(category.getBooks() != null ? category.getBooks().size() : 0)")
    CategoryResponse toResponse(Category category);
}
