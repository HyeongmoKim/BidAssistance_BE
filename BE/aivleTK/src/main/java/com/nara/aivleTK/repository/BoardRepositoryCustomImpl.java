package com.nara.aivleTK.repository;

import com.nara.aivleTK.domain.board.Board;
import com.nara.aivleTK.domain.board.QBoard;
import com.nara.aivleTK.dto.board.BoardListRequest;
import com.nara.aivleTK.dto.board.BoardResponse;
import com.nara.aivleTK.dto.board.CategoryCountsResponse;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class BoardRepositoryCustomImpl implements BoardRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<BoardResponse> search(BoardListRequest condition, Pageable pageable) {
        // Q클래스 가져오기 (QueryDSL 설정 되어 있어야 함)
        QBoard board = QBoard.board;

        List<OrderSpecifier<?>> orders = getOrderSpecifiers(pageable);

        // 1. 컨텐츠 조회
        List<Board> content = queryFactory
                .selectFrom(board)
                .where(
                        categoryEq(condition.getCategory()), // 카테고리 조건
                        titleOrContentContains(condition.getQ()) // 검색어 조건
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(
                        orders.toArray(OrderSpecifier[]::new))
                .fetch();

        // 2. 전체 개수 조회 (페이징 위해 필요)
        long total = queryFactory
                .selectFrom(board)
                .where(
                        categoryEq(condition.getCategory()),
                        titleOrContentContains(condition.getQ()))
                .fetch()
                .stream()
                .count();

        List<BoardResponse> responses = content.stream()
                .map(BoardResponse::new)
                .collect(Collectors.toList());

        return new PageImpl<>(responses, pageable, total);
    }

    @Override
    public CategoryCountsResponse getCategoryCounts() {
        QBoard board = QBoard.board;

        List<Board> allBoards = queryFactory
                .selectFrom(board)
                .fetch();

        long all = allBoards.size();
        long question = allBoards.stream().filter(b -> "1".equals(b.getCategory())).count();
        long info = allBoards.stream().filter(b -> "2".equals(b.getCategory())).count();
        long review = allBoards.stream().filter(b -> "3".equals(b.getCategory())).count();
        long discussion = allBoards.stream().filter(b -> "4".equals(b.getCategory())).count();

        return CategoryCountsResponse.builder()
                .all(all)
                .question(question)
                .info(info)
                .review(review)
                .discussion(discussion)
                .build();
    }

    private BooleanExpression categoryEq(String category) {
        return StringUtils.hasText(category) ? QBoard.board.category.eq(category) : null;
    }

    private BooleanExpression titleOrContentContains(String q) {
        return StringUtils.hasText(q)
                ? QBoard.board.title.contains(q).or(QBoard.board.content.contains(q))
                : null;
    }

    private List<OrderSpecifier<?>> getOrderSpecifiers(Pageable pageable) {
        List<OrderSpecifier<?>> orders = new ArrayList<>();
        if (!pageable.getSort().isEmpty()) {
            for (Sort.Order order : pageable.getSort()) {
                Order direction = order.getDirection().isAscending() ? Order.ASC : Order.DESC;
                switch (order.getProperty()) {
                    case "viewCount":
                        orders.add(new OrderSpecifier<>(direction, QBoard.board.viewCount));
                        break;
                    case "likeCount":
                        orders.add(new OrderSpecifier<>(direction, QBoard.board.likeCount));
                        break;
                    case "createdAt":
                    default:
                        orders.add(new OrderSpecifier<>(direction, QBoard.board.createdAt));
                        break;
                }
            }
        }
        return orders;
    }
}
