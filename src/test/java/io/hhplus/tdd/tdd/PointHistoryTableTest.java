package io.hhplus.tdd.tdd;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.point.PointHistory;
import io.hhplus.tdd.point.TransactionType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
public class PointHistoryTableTest {

    @Autowired
    private PointHistoryTable pointHistoryTable;


    /*
     *
     * InfraLayer 계층의 CRU를 테스트 하기 위해서
     * 통합 테스트를 작성해봤습니다.
     *
     * */

    @Test
    @DisplayName("충전/사용 내역을 PointHistory에 저장한다")
    void insert_AddsPointHistory() {
        // given
        long userId = 1L;
        long amount = 5000L;
        TransactionType type = TransactionType.CHARGE;
        long updateMillis = System.currentTimeMillis();

        // when
        PointHistory result = pointHistoryTable.insert(userId, amount, type, updateMillis);

        // then
        assertThat(result.userId()).isEqualTo(userId);
        assertThat(result.amount()).isEqualTo(amount);
        assertThat(result.type()).isEqualTo(type);
        assertThat(result.updateMillis()).isEqualTo(updateMillis);
    }


    @Test
    @DisplayName("충전/사용 내역에서 특정유저의 내역만 가져온다")
    void selectAllByUserId_ReturnsCorrectHistories() {
        // given
        long userId1 = 1L;
        long userId2 = 2L;

        // when
        pointHistoryTable.insert(userId1, 5000L, TransactionType.CHARGE, System.currentTimeMillis());
        pointHistoryTable.insert(userId1, 3000L, TransactionType.USE, System.currentTimeMillis());
        pointHistoryTable.insert(userId2, 10000L, TransactionType.CHARGE, System.currentTimeMillis());
        List<PointHistory> result = pointHistoryTable.selectAllByUserId(userId1);

        // then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(PointHistory::userId).containsOnly(userId1);
    }



}//end
