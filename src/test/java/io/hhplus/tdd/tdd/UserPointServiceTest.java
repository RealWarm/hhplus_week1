package io.hhplus.tdd.tdd;


import io.hhplus.tdd.point.PointHistory;
import io.hhplus.tdd.point.TransactionType;
import io.hhplus.tdd.point.UserPoint;
import io.hhplus.tdd.point.UserPointService;
import io.hhplus.tdd.point.error.InvalidAmountException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static io.hhplus.tdd.point.TransactionType.CHARGE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/*
* UserPointService의
* 1) 해피케이스
* 2) 예외케이스
* 3) 종단관심사인 구매내역 정상작동 확인
*
* 이렇게 3가지를 위해서 테스트 케이스를 작성했습니다.
* */

@SpringBootTest
public class UserPointServiceTest {
    @Autowired
    private UserPointService userPointService;


    @Test
    @DisplayName("유저의 포인트 충전 기능이 정상 작동한다.")
    void chargeUserPoint() {
        long userId = 1L;
        long chargeAmount = 5000L;

        // 충전 전 잔액 확인
        UserPoint initialUserPoint = userPointService.getUserPoint(userId);
        assertThat(initialUserPoint.point()).isEqualTo(0L);

        // 포인트 충전
        UserPoint updatedUserPoint = userPointService.chargeUserPoint(userId, chargeAmount);

        // 충전 후 잔액 확인
        assertThat(updatedUserPoint.point()).isEqualTo(chargeAmount);

        // 포인트 내역 확인
        List<PointHistory> histories = userPointService.getAllUserPointHistories(userId);
        assertThat(histories).hasSize(1);
        assertThat(histories.get(0).type()).isEqualTo(CHARGE);
    }


    @Test
    @DisplayName("유저의 포인트 차감 기능이 정상 작동한다.")
    void testDeductPoint() {
        //given
        long userId = 2L;
        long initialAmount = 10000L;
        long deductAmount = 3000L;

        // when
        // 첫 포인트 충전
        userPointService.chargeUserPoint(userId, initialAmount);

        // 포인트 차감
        UserPoint updatedUserPoint = userPointService.deductPoint(userId, deductAmount);

        // then
        // 차감 후 잔액 확인
        assertThat(updatedUserPoint.point()).isEqualTo(initialAmount - deductAmount);

        // 포인트 내역 확인
        List<PointHistory> histories = userPointService.getAllUserPointHistories(userId);
        assertThat(histories).hasSize(2);
        assertThat(histories.get(1).type()).isEqualTo(TransactionType.USE);
    }


    @Test
    @DisplayName("포인트 차감 시 잔액이 부족하면 예외가 발생한다.")
    void testDeductPoint_InsufficientBalance_ThrowsException() {
        // given
        long userId = 3L;
        long initialAmount = 2000L;
        long deductAmount = 3000L;
        userPointService.chargeUserPoint(userId, initialAmount); // 초기 포인트 설정

        // when & then
        // 잔액 부족 예외 확인
        assertThatThrownBy(() -> userPointService.deductPoint(userId, deductAmount))
                .isInstanceOf(InvalidAmountException.class)
                .hasMessage("잔고가 부족합니다.");
    }


    @Test
    @DisplayName("충전 금액이 0이하일 경우 예외가 발생한다.")
    void testChargeUserPoint_NegativeOrZeroAmount_ThrowsException() {
        long userId = 4L;

        // 0원 충전 시 예외 확인
        assertThatThrownBy(() -> userPointService.chargeUserPoint(userId, 0))
                .isInstanceOf(InvalidAmountException.class)
                .hasMessage("충전금액은 0이하 일 수 없습니다.");

        // 음수 금액 충전 시 예외 확인
        assertThatThrownBy(() -> userPointService.chargeUserPoint(userId, -100))
                .isInstanceOf(InvalidAmountException.class)
                .hasMessage("충전금액은 0이하 일 수 없습니다.");
    }


    @Test
    @DisplayName("충전 후 금액이 한도를 초과할 경우 예외가 발생한다.")
    void testChargeUserPoint_ExceedsLimit_ThrowsException() {
        // given
        long userId = 5L;
        long initialAmount = 95000000L;
        long chargeAmount = 10000000L;
        userPointService.chargeUserPoint(userId, initialAmount); // 초기 포인트 설정

        // when & then
        // 한도 초과 예외 확인
        assertThatThrownBy(() -> userPointService.chargeUserPoint(userId, chargeAmount))
                .isInstanceOf(InvalidAmountException.class)
                .hasMessage("충전가능한 한도를 초과했습니다.");
    }


    @Test
    @DisplayName("유저의 결제 내역을 조회한다.")
    void testGetAllPaymentHistories() {
        // given
        long userId = 6L;
        long chargeAmount = 10000L;
        long deductAmount = 3000L;

        // when
        // 포인트 충전
        userPointService.chargeUserPoint(userId, chargeAmount);

        // 포인트 차감
        userPointService.deductPoint(userId, deductAmount);

        // then
        // 결제 내역 확인
        List<PointHistory> pointHistories = userPointService.getAllUserPointHistories(userId);
        assertThat(pointHistories).hasSize(2);
        assertThat(pointHistories.get(0).amount()).isEqualTo(chargeAmount);
        assertThat(pointHistories.get(1).amount()).isEqualTo(chargeAmount-deductAmount);
    }


}//end
