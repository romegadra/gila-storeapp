package com.gila.storeapp.product;

import java.util.List;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findBySkuIgnoreCase(String sku);

    long countBySkuIgnoreCase(String sku);

    boolean existsBySkuIgnoreCaseAndIdNot(String sku, Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Product p where p.id = :id")
    Optional<Product> findByIdForUpdate(@Param("id") Long id);

    @Query("""
        select p from Product p
        where (:query is null or :query = ''
            or lower(p.name) like lower(concat('%', :query, '%'))
            or lower(p.sku) like lower(concat('%', :query, '%'))
            or lower(p.description) like lower(concat('%', :query, '%')))
        and (:category is null or :category = '' or lower(p.category) = lower(:category))
        order by p.name asc
        """)
    List<Product> search(@Param("query") String query, @Param("category") String category);

    @Query("""
        select p from Product p
        where (:query is null or :query = ''
            or lower(p.name) like lower(concat('%', :query, '%'))
            or lower(p.sku) like lower(concat('%', :query, '%'))
            or lower(p.description) like lower(concat('%', :query, '%')))
        and (:category is null or :category = '' or lower(p.category) = lower(:category))
        """)
    Page<Product> searchPage(@Param("query") String query, @Param("category") String category, Pageable pageable);

    @Query("select distinct p.category from Product p order by p.category")
    List<String> findCategories();
}
