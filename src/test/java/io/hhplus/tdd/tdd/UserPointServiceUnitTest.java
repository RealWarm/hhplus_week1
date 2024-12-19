package io.hhplus.tdd.tdd;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.UserPoint;
import io.hhplus.tdd.point.UserPointService;
import io.hhplus.tdd.point.error.InvalidAmountException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;


/*
* 요구사항에서 충전/사용 금액에 대한
* Edge 테스트를 작성해보았습니다.
*
* */

public class UserPointServiceUnitTest {

    @Mock
    private UserPointTable userPointRepository;

    @Mock
    private PointHistoryTable pointHistoryRepository;

    @InjectMocks
    private UserPointService userPointService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }


    @Test
    @DisplayName("충전 금액이 0이하면 InvalidAmountException을 발생시킨다.")
    void chargeUserPoint_NegativeOrZeroAmount_ThrowsException() {
        //given
        long userId = 1L;

        // 충전 금액이 0이면 예외 확인
        assertThatThrownBy(() -> userPointService.chargeUserPoint(userId, 0))
                .isInstanceOf(InvalidAmountException.class)
                .hasMessage("충전금액은 0이하 일 수 없습니다.");

        // 충전 금액이 음수면 예외 확인
        assertThatThrownBy(() -> userPointService.chargeUserPoint(userId, -100))
                .isInstanceOf(InvalidAmountException.class)
                .hasMessage("충전금액은 0이하 일 수 없습니다.");
    }


    @Test
    @DisplayName("충전 후 금액이 (1억)을 넘으면 에러를 발생시킨다.")
    void testChargeUserPoint_ExceedsLimit_ThrowsException() {
        // given
        long userId = 2L;
        long chargeAmount = 80000000L; // 충전할 금액
        UserPoint userPoint = new UserPoint(userId, 30000000L, System.currentTimeMillis()); // 현재 잔액
        when(userPointRepository.selectById(userId)).thenReturn(userPoint);

        // when & then
        // 잔액과 충전 금액의 합이 1억을 초과하는 경우 예외 확인
        assertThatThrownBy(() -> userPointService.chargeUserPoint(userId, chargeAmount))
                .isInstanceOf(InvalidAmountException.class)
                .hasMessage("충전가능한 한도를 초과했습니다.");
    }


    @Test
    @DisplayName("차감금액이 보유금액보다 크면 에러를 발생시킨다.")
    void deductPoint_mustBelownNowPoint_ElseThrowsException() {
        // given
        long userId = 3L;
        long deductAmount = 6000L; // 차감할 금액
        UserPoint userPoint = new UserPoint(userId, 5000L, System.currentTimeMillis()); // 현재 잔액
        when(userPointRepository.selectById(userId)).thenReturn(userPoint);

        // when & then
        assertThatThrownBy(() -> userPointService.deductPoint(userId, deductAmount))
                .isInstanceOf(InvalidAmountException.class)
                .hasMessage("잔고가 부족합니다.");
    }


}//end
