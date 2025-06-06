package lk.ijse.aadbackend.repo;

import lk.ijse.aadbackend.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {
    List<Category> findByParentCategoryId(UUID parentCategoryId);


    // New method to count subcategories
    int countByParentCategoryId(UUID parentCategoryId);

}
