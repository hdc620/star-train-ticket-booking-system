package com.star.constant;

/**
 * 状态常量，启用或者禁用
 */
public class StatusConstant {
    //1.站点
    //启用
    public static final String STATION_ENABLE = "启用";
    //禁用
    public static final String STATION_DISABLE = "禁用";

    //2.列车
    public static final String TRAIN_IN_OPERATION="运营中";
    public static final String TRAIN_UNDER_MAINTENANCE="维护中";
    public static final String TRAIN_OUT_OF_SERVICE="停运";

    //3.车次
    public static final String SCHEDULE_AVAILABLE="可售票";
    public static final String SCHEDULE_UNAVAILABLE="已停售";
    public static final String SCHEDULE_DEPARTED="已发车";

    //4.订单
    public static final String ORDER_PAID="已支付";
    public static final String ORDER_REFUNDED="已退票";
    public static final String ORDER_COMPLETED="已完成";
}
