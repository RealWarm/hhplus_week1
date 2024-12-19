package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.error.InvalidAmountException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.locks.ReentrantLock;


/*
* 포인트 충전/사용 기능과
* 포인트 충전/사용 기록을 분리하려했으나
* 규모가 작은 케이스라 기능을 붙여놨습니다.
*
* ReentrantLock를 이용하여 동시성에 대한 처리를 구현해봤습니다.
*
* */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserPointService {
    private final long POINT_LIMIT = 100_000_000;
    private final UserPointTable userPointRepository;
    private final PointHistoryTable pointHistoryRepository;
    private final ReentrantLock lock = new ReentrantLock(); // ReentrantLock 인스턴스


    // 유저의 포인프를 조회
    public UserPoint getUserPoint(long id) {
        return userPointRepository.selectById(id);
    }//getUserPoint


    // 유저의 포인트를 충전
    public UserPoint chargeUserPoint(long id, long chargeAmount) {
        lock.lock();
        try {
            if (chargeAmount <= 0) {
                throw new InvalidAmountException("충전금액은 0이하 일 수 없습니다.");
            }

            long nowAmount = userPointRepository.selectById(id).point();
            long totalAmount = chargeAmount + nowAmount;
            if (totalAmount > POINT_LIMIT) {
                throw new InvalidAmountException("충전가능한 한도를 초과했습니다.");
            }

            UserPoint insertedValue = userPointRepository.insertOrUpdate(id, totalAmount);
            if(insertedValue.point()==totalAmount){
                pointHistoryRepository.insert(id, totalAmount, TransactionType.CHARGE, System.currentTimeMillis());
            }else{
                throw new RuntimeException("금액 충전에 실패 했습니다.");
            }

            return insertedValue;
        }finally {
            lock.unlock();
        }//try-1

    }//chargeUserPoint


    // 유저의 포인트를 사용하는 기능
    public UserPoint deductPoint(long id, long deductAmount) {
        lock.lock();
        try {
            long nowAmount = userPointRepository.selectById(id).point();
            if (deductAmount > nowAmount) {
                throw new InvalidAmountException("잔고가 부족합니다.");
            }//if

            long totalAmount = nowAmount - deductAmount;
            UserPoint insertedValue = userPointRepository.insertOrUpdate(id, nowAmount - deductAmount);
            if(insertedValue.point()==totalAmount){
                pointHistoryRepository.insert(id, totalAmount, TransactionType.USE, System.currentTimeMillis());
            }else{
                throw new RuntimeException("금액 사용에 실패 하였습니다.");
            }

            return insertedValue;
        }finally {
            lock.unlock();
        }

    }//userPoint


    // 유저의 포인트 충전/이용 내역을 조회
    public List<PointHistory> getAllUserPointHistories(long id) {
        return pointHistoryRepository.selectAllByUserId(id);
    }//getAllUserPointHistories

}//end
