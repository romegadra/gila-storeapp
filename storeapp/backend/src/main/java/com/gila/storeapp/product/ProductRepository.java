package com.gila.storeapp.product;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findBySkuIgnoreCase(String sku);

    long countBySkuIgnoreCase(String sku);

    boolean existsBySkuIgnoreCaseAndIdNot(String sku, Long id);

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

    @Query("select distinct p.category from Product p order by p.category")
    List<String> findCategories();
}
