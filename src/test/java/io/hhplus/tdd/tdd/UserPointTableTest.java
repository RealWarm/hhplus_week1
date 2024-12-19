package io.hhplus.tdd.tdd;

import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.UserPoint;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class UserPointTableTest {

    @Autowired
    private UserPointTable userPointTable;

    /*
    *
    * InfraLayer 계층의 동작 방식 파악 및
    * CRU를 테스트 하기 위해서 통합 테스트를 작성해봤습니다.
    *
    * */

    @Test
    @DisplayName("신규 유저라면 0원을 반환한다.")
    void selectById_UserDoesNotExist_ReturnsEmptyUserPoint() {
        // given
        long userId = 2L;

        // when
        UserPoint result = userPointTable.selectById(userId);

        //then
        assertThat(result.id()).isEqualTo(userId);
        assertThat(result.point()).isZero();
    }


    @Test
    @DisplayName("유저가 첫 충전을 시도한다.")
    void insertOrUpdate_SavesUserPoint() {
        // given
        long userId = 3L;
        long amount = 10000L;

        // when
        UserPoint result = userPointTable.insertOrUpdate(userId, amount);

        //then
        assertThat(result.id()).isEqualTo(userId);
        assertThat(result.point()).isEqualTo(amount);
    }


    @Test
    @DisplayName("충전은 증감연산이 아닌 상태 변화다.")
    void testInsertOrUpdate_UpdatesUserPoint() {
        // given
        long userId = 4L;
        userPointTable.insertOrUpdate(userId, 20000L);
        userPointTable.insertOrUpdate(userId, 30000L);

        // when
        UserPoint result = userPointTable.selectById(userId);

        //then
        assertThat(result.point()).isEqualTo(30000L); // 마지막 업데이트된 포인트 확인
    }


    @Test
    @DisplayName("기존 유저라면 보유한 포인트를 반환한다.")
    void selectById_UserExists_ReturnsUserPoint() {
        // given
        long userId = 1L;
        UserPoint userPoint = new UserPoint(userId, 5000L, System.currentTimeMillis());
        userPointTable.insertOrUpdate(userId, 5000L);

        //when
        UserPoint result = userPointTable.selectById(userId);

        //then
        assertThat(result.id()).isEqualTo(userPoint.id());
        assertThat(result.point()).isEqualTo(userPoint.point());
    }


}//end
