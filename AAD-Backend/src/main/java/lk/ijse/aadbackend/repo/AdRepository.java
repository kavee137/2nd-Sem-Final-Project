package lk.ijse.aadbackend.repo;

import lk.ijse.aadbackend.entity.Ad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface AdRepository  extends JpaRepository<Ad, UUID> {
    List<Ad> findByStatus(String status);

    List<Ad> findByStatusOrderByCreatedAtDesc(String status);

    List<Ad> findByUserId(UUID userId);

    @Query("SELECT COUNT(a) FROM Ad a WHERE a.category.id IN (" +
            "SELECT c.id FROM Category c WHERE c.parentCategory.id = :parentId" +
            ") AND a.status = 'ACTIVE'")
    int countActiveAdsByParentCategory(@Param("parentId") UUID parentId);



    //Ad filter ----------------------------------------------------------------------------

    List<Ad> findByStatusAndCategoryIdAndLocationParentLocationIdAndLocationId(
            String status, UUID categoryId, UUID districtId, UUID cityId);

    List<Ad> findByStatusAndCategoryIdAndLocationParentLocationId(
            String status, UUID categoryId, UUID districtId);

    List<Ad> findByStatusAndCategoryIdAndLocationId(
            String status, UUID categoryId, UUID cityId);

    List<Ad> findByStatusAndCategoryId(String status, UUID categoryId);

    List<Ad> findByStatusAndLocationParentLocationIdAndLocationId(
            String status, UUID districtId, UUID cityId);

    List<Ad> findByStatusAndLocationParentLocationId(String status, UUID districtId);

    List<Ad> findByStatusAndLocationId(String status, UUID cityId);

    //End of Ad filter ----------------------------------------------------------------------------




    //Ad search ----------------------------------------------------------------------------

    @Query("SELECT a FROM Ad a " +
            "WHERE a.status = 'ACTIVE' " +
            "AND (:keyword IS NULL OR LOWER(a.title) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:categoryId IS NULL OR a.category.id = :categoryId) " +
            "AND (:districtId IS NULL OR a.location.parentLocation.id = :districtId) " +
            "AND (:cityId IS NULL OR a.location.id = :cityId)")
    List<Ad> searchAds(@Param("keyword") String keyword,
                       @Param("categoryId") UUID categoryId,
                       @Param("districtId") UUID districtId,
                       @Param("cityId") UUID cityId);



    //End of Ad search ----------------------------------------------------------------------------
}