package com.ensam.projet.mapper;

import com.ensam.projet.dto.response.CategoryResponse;
import com.ensam.projet.entity.Category;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    CategoryResponse toResponse(Category category);
}
