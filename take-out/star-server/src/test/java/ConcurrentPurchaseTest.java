import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.star.constant.StatusConstant;
import com.star.context.BaseContext;
import com.star.dto.PurchaseDTO;
import com.star.entity.Order;
import com.star.entity.Schedule;
import com.star.mapper.AdminOrderMapper;
import com.star.mapper.AdminOrderMapper;
import com.star.mapper.ScheduleMapper;
import com.star.service.PurchaseService;
import com.star.service.TicketService;
import com.starserver.StarServerApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(classes = StarServerApplication.class)
public class ConcurrentPurchaseTest {

    @Autowired
    private PurchaseService purchaseService;

    @Autowired
    private ScheduleMapper scheduleMapper;

    @Autowired
    private TicketService ticketService;

    @Autowired
    private AdminOrderMapper orderMapper;

    @Test
    public void testConcurrentPurchase() throws InterruptedException {
        Long scheduleId = 2L;           // 测试车次ID（请确保数据库存在且状态为"可售票"）
        int totalThreads = 100;          // 模拟100个并发用户
        int ticketCountPerUser = 1;      // 每人购票数量

        // ===== 1. 初始化库存（可选，如果 Redis 已存在可跳过） =====
        Schedule schedule = scheduleMapper.selectById(scheduleId);
        if (schedule == null) {
            throw new RuntimeException("测试车次不存在，请先插入数据");
        }
        ticketService.initStock(scheduleId, schedule.getRemainingSeats());
        System.out.println("测试前库存（DB）: " + schedule.getRemainingSeats());

        // ===== 2. 准备并发执行 =====
        ExecutorService executor = Executors.newFixedThreadPool(totalThreads);
        CountDownLatch latch = new CountDownLatch(totalThreads);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        long startTime = System.currentTimeMillis();

        for (int i = 1; i <= totalThreads; i++) {
            final Long userId = (long) i;   // 模拟用户ID从 1 到 100
            executor.submit(() -> {
                try {
                    // 设置当前线程的用户ID（模拟登录上下文）
                    BaseContext.setCurrentId(userId);

                    PurchaseDTO dto = new PurchaseDTO();
                    dto.setScheduleId(scheduleId);
                    dto.setTicketCount((long) ticketCountPerUser);

                    purchaseService.save(dto);   // 执行购票逻辑
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                    System.err.println("用户 " + userId + " 购票失败: " + e.getMessage());
                } finally {
                    BaseContext.removeCurrentId(); // 清理 ThreadLocal，防止内存泄漏
                    latch.countDown();
                }
            });
        }

        latch.await();  // 等待所有线程执行完毕
        executor.shutdown();
        long endTime = System.currentTimeMillis();

        // ===== 3. 输出测试结果 =====
        System.out.println("\n========== 并发测试结果 ==========");
        System.out.println("总请求数: " + totalThreads);
        System.out.println("成功: " + successCount.get());
        System.out.println("失败: " + failCount.get());
        System.out.println("总耗时: " + (endTime - startTime) + " ms");

        // ===== 4. 数据一致性验证 =====
        Schedule afterSchedule = scheduleMapper.selectById(scheduleId);
        Long expectedRemaining = schedule.getRemainingSeats() - successCount.get();
        System.out.println("\n========== 数据验证 ==========");
        System.out.println("预期剩余库存: " + expectedRemaining);
        System.out.println("实际剩余库存(DB): " + afterSchedule.getRemainingSeats());

        // 统计实际订单数
        Long orderCount = orderMapper.selectCount(
                new LambdaQueryWrapper<Order>()
                        .eq(Order::getScheduleId, scheduleId)
                        .in(Order::getStatus, StatusConstant.ORDER_PAID, StatusConstant.ORDER_COMPLETED)
        );
        System.out.println("实际生成订单数: " + orderCount);

        // 检查是否有用户重复下单（理论上应为0）
        List<Map<String, Object>> duplicateUsers = orderMapper.selectMaps(
                new LambdaQueryWrapper<Order>()
                        .select(Order::getUserId, Order::getScheduleId)
                        .eq(Order::getScheduleId, scheduleId)
                        .in(Order::getStatus, StatusConstant.ORDER_PAID, StatusConstant.ORDER_COMPLETED)
                        .groupBy(Order::getUserId)
                        .having("COUNT(*) > 1")
        );
        System.out.println("重复下单用户数: " + duplicateUsers.size());

        // 断言验证（可选）
        assertEquals(expectedRemaining, afterSchedule.getRemainingSeats(), "库存扣减不一致");
        assertEquals(successCount.get(), orderCount, "成功订单数与实际订单数不匹配");
        assertEquals(0, duplicateUsers.size(), "存在用户重复下单");
    }
}