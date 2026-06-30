package com.nhnacademy.ailibraryteam1.common.init.loader;

import com.nhnacademy.ailibraryteam1.book.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * BookCopyService 에 그냥 합치지 않고 별도 클래스로 뺀 이유:
 * Spring의 @Transactional은 AOP 프록시 방식으로 동작함
 * 어떤 메서드에 트랜잭셔널 어노테이션을 붙이면 스프링이 그 클래스의 프록시 객체를 만들어서, 외부에서 그 메서드를 호출할 때 프록시가 먼저 가로채서 트랜잭션을 시작하고, 실제 메서드를 실행하고, 끝나면 커밋롤백 처리함
 * 그런데, 같은 클래스 안에서 this.메서드() 이렇게 자기 자신 호출하면 프록시를 거치지 않고 원본 객체의 메서드가 직접 호출됨. 그러면 트랜잭셔널이 무시됨
 * 만약 truncate()를 BookCopyService 안에 그냥 두고 this.truncate()를 호출하면, 트랜잭션이 적용 안 돼서 에러가 발생할 것임 (No active transaction for update query)
 * 그래서 truncate()를 아예 다른 Bean으로 분리하고, BookCopyService가 이 Bean을 주입받아 호출하는 구조로 만듬
 * 이렇게 하면 다른 객체 간의 호출이 되어 프록시를 거치고 트랜잭셔널이 의도대로 작동함
 *
 * @Transactional 이 왜 필요한가:
 * truncateTable() 은 @Modifying + 네이티브쿼리로 만들어진 메서드임. 당연히 붙여야지..
 */
@Component
@RequiredArgsConstructor
public class BookTableTruncator {

    private final BookRepository bookRepository;

    @Transactional
    public void truncate() {
        this.bookRepository.truncateTable();
    }
}