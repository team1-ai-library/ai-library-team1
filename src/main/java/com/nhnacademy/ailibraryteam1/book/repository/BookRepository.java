package com.nhnacademy.ailibraryteam1.book.repository;

import com.nhnacademy.ailibraryteam1.book.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface BookRepository extends JpaRepository<Book, Long>, BookQuerydslRepository{

    /**
     * JPA의 @Query는 기본적으로 select를 가정함
     * 데이터 변경 쿼리를 날리려면 @Modifying 붙여야 함
     * 안 붙이면 Hibernate가 이거 조회 쿼리인데 결과 어떻게 매핑해야되나? 하며 에러 던짐
     */
    @Modifying
    @Query(value = "TRUNCATE TABLE books", nativeQuery = true)
    void truncateTable();
}