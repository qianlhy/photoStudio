package com.utils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 订单状态机：定义合法状态、阶段顺序与流转规则。
 *
 * 正向流程：待排队 -> 已排期 -> 待拍摄 -> 拍摄进行中 -> 制作中 -> 成品已上线
 * 终态：成品已上线 / 已取消（不可再变更）
 * 兼容历史别名：待到店拍摄=待拍摄，拍摄完成=拍摄进行中，待出成品=制作中
 */
public final class OrderStatus {

    public static final String PENDING_QUEUE = "待排队";
    public static final String SCHEDULED = "已排期";
    public static final String PENDING_SHOOT = "待拍摄";
    public static final String SHOOTING = "拍摄进行中";
    public static final String PRODUCING = "制作中";
    public static final String DELIVERED = "成品已上线";
    public static final String CANCELLED = "已取消";

    /** 不计入排队（已离场）的状态 */
    public static final List<String> INACTIVE = Arrays.asList(CANCELLED, DELIVERED);

    private static final Map<String, Integer> STAGE = new HashMap<>();

    static {
        STAGE.put(PENDING_QUEUE, 0);
        STAGE.put(SCHEDULED, 1);
        STAGE.put(PENDING_SHOOT, 2);
        STAGE.put("待到店拍摄", 2);
        STAGE.put(SHOOTING, 3);
        STAGE.put("拍摄完成", 3);
        STAGE.put(PRODUCING, 4);
        STAGE.put("待出成品", 4);
        STAGE.put(DELIVERED, 5);
    }

    private OrderStatus() {
    }

    public static boolean isKnown(String s) {
        return CANCELLED.equals(s) || STAGE.containsKey(s);
    }

    public static boolean isTerminal(String s) {
        return CANCELLED.equals(s) || DELIVERED.equals(s);
    }

    /** 是否可被用户取消（未完成、未取消） */
    public static boolean canCancel(String s) {
        return isKnown(s) && !isTerminal(s);
    }

    /**
     * 校验状态流转是否合法。
     * - to 为空：表示不修改状态，放行
     * - to 未知：拒绝
     * - from 与 to 相同：放行（幂等）
     * - 取消：仅当 from 可取消
     * - 其余：终态不可前进；非终态只允许前进或停留，不允许回退
     */
    public static boolean canTransfer(String from, String to) {
        if (to == null || to.trim().isEmpty()) {
            return true;
        }
        if (!isKnown(to)) {
            return false;
        }
        if (to.equals(from)) {
            return true;
        }
        if (CANCELLED.equals(to)) {
            return canCancel(from);
        }
        if (isTerminal(from)) {
            return false;
        }
        Integer f = STAGE.get(from);
        Integer t = STAGE.get(to);
        if (f == null || t == null) {
            return true;
        }
        return t >= f;
    }
}
