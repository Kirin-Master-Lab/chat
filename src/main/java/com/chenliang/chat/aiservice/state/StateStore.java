package com.chenliang.chat.aiservice.state;

/**
 * 分布式缓存状态存储接口。
 * 用于取代 HttpSession，解决负载均衡架构下的状态共享问题。
 */
public interface StateStore {

    /**
     * 持久化一个业务状态值。
     *
     * @param userId 唯一用户ID
     * @param key    状态Key (如 SESSION_MODE_KEY)
     * @param value  具体的状态值 (如 DICT_EXPERT)
     */
    void set(String userId, String key, Object value);

    /**
     * 获取指定状态。
     *
     * @param userId 用户ID
     * @param key    状态Key
     * @return 状态值
     */
    Object get(String userId, String key);

    /**
     * 清除对应状态。
     *
     * @param userId 用户ID
     * @param key    状态Key
     */
    void remove(String userId, String key);
}
