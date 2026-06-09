package com.ensam.projet.mapper;

import com.ensam.projet.dto.response.CategoryResponse;
import com.ensam.projet.entity.Category;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-10T00:20:41+0100",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.11 (Red Hat, Inc.)"
)
@Component
public class CategoryMapperImpl implements CategoryMapper {

    @Override
    public CategoryResponse toResponse(Category category) {
        if ( category == null ) {
            return null;
        }

        CategoryResponse categoryResponse = new CategoryResponse();

        categoryResponse.setId( category.getId() );
        categoryResponse.setName( category.getName() );
        categoryResponse.setDescription( category.getDescription() );
        categoryResponse.setColor( category.getColor() );

        categoryResponse.setBookCount( category.getBooks() != null ? category.getBooks().size() : 0 );

        return categoryResponse;
    }
}
