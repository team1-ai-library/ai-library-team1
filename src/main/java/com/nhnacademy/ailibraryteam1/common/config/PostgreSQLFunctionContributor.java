package com.nhnacademy.ailibraryteam1.common.config;

import org.hibernate.boot.model.FunctionContributions;
import org.hibernate.boot.model.FunctionContributor;
import org.hibernate.query.sqm.function.SqmFunctionRegistry;
import org.hibernate.type.StandardBasicTypes;

/**
 * Hibernate 커스텀 함수 등록 (PostgreSQL 전문 검색 함수를 Hibernate에서 사용할 수 있도록)
 */
public class PostgreSQLFunctionContributor implements FunctionContributor {

    @Override
    public void contributeFunctions(FunctionContributions functionContributions) {

        /**
         * to_tsvector('korean', 컬럼) @@ plainto_tsquery('korean', 검색어)
         * 형태의 네이티브 SQL을 QueryDSL에서 Expressions.booleanTemplate으로 호출할 수 있도록 해줌
         * <p>
         * SPI 등록 필요:
         * META-INF/services/org.hibernate.boot.model.FunctionContributor 파일에 이 클래스의 FQCN을 등록해야 Hibernate가 인식함
         */
        functionContributions.getFunctionRegistry()
                .registerPattern(
                        "ts_match_korean", // Java/JPQL에서 부를 이름
                        "to_tsvector('korean', ?1) @@ plainto_tsquery('korean', ?2)",  // DB에 날아갈 SQL
                        functionContributions.getTypeConfiguration()
                                .getBasicTypeRegistry()
                                .resolve(StandardBasicTypes.BOOLEAN) // 이 함수는 true/false를 반환함
                );

        /**
         * 코사인 유사도 함수 등록
         * 1 - (embedding <=> vector): 코사인 거리를 유사도로 변환
         */
        functionContributions.getFunctionRegistry()
                .registerPattern(
                        "vector_cosine_similarity", // Java/JPQL에서 부를 이름
                        "(1.0 - (?1 <=> cast(?2 as vector)))", // ?1: embedding 컬럼, ?2: 쿼리 벡터
                        functionContributions.getTypeConfiguration()
                                .getBasicTypeRegistry()
                                .resolve(StandardBasicTypes.DOUBLE) // 이 함수의 반환 타입: double
                );
    }
}