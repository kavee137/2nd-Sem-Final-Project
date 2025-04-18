package lk.ijse.aadbackend.specification;

import lk.ijse.aadbackend.entity.Ad;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AdSpecification {

    public static Specification<Ad> searchAds(String status, UUID categoryId, 
                                               UUID districtId, UUID cityId, 
                                               String searchTerm) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            // Add status condition
            predicates.add(criteriaBuilder.equal(root.get("status"), status));
            
            // Add category condition if provided
            if (categoryId != null) {
                predicates.add(criteriaBuilder.equal(root.get("category").get("id"), categoryId));
            }
            
            // Add district condition if provided
            if (districtId != null) {
                predicates.add(criteriaBuilder.equal(root.get("location").get("parentLocation").get("id"), districtId));
            }
            
            // Add city condition if provided
            if (cityId != null) {
                predicates.add(criteriaBuilder.equal(root.get("location").get("id"), cityId));
            }
            
            // Add search term condition if provided
            if (searchTerm != null && !searchTerm.isEmpty()) {
                String searchLike = "%" + searchTerm.toLowerCase() + "%";
                
                Predicate titlePredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("title")), searchLike
                );
                
                Predicate descriptionPredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("description")), searchLike
                );
                
                predicates.add(criteriaBuilder.or(titlePredicate, descriptionPredicate));
            }
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}