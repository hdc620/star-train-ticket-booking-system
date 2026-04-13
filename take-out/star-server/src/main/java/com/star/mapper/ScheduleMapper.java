package com.star.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.star.entity.Schedule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface ScheduleMapper extends BaseMapper<Schedule> {
    /**
     * 原子扣减剩余座位（保证数据库层面的并发安全）
     * @param scheduleId 车次ID
     * @param count      扣减数量
     * @return 影响行数，0 表示余票不足或更新失败
     */
    @Update("UPDATE t_schedule SET remaining_seats = remaining_seats - #{count} " +
            "WHERE schedule_id = #{scheduleId} AND remaining_seats >= #{count}")
    int decrementRemainingSeats(@Param("scheduleId") Long scheduleId, @Param("count") Long count);
}
