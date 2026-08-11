package cn.yanque.commons.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Collections;
import java.util.Collection;
import java.util.Set;

@Component
public class RedisUtils{

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    public void set(String key, String value) {
        stringRedisTemplate.opsForValue().set(key, value);
    }

    public void set(String key, String value, Duration timeout) {
        stringRedisTemplate.opsForValue().set(key, value, timeout);
    }

    public String get(String key) {
        return stringRedisTemplate.opsForValue().get(key);
    }

    // setnx()
    public Boolean setIfAbsent(String key, String value, Duration timeout) {
        return stringRedisTemplate.opsForValue().setIfAbsent(key, value, timeout);
    }

    public Boolean delete(String key) {
        return stringRedisTemplate.delete(key);
    }

    /**
     * 只有 value 仍然是自己写进去的值时才删除。
     *
     * 用在 Redis 锁释放，避免锁过期后被别人重新拿到，
     * 当前线程又把别人的锁删掉。
     */
    public Boolean deleteIfValue(String key, String value) {
        String script = """
                if redis.call('get', KEYS[1]) == ARGV[1] then
                    return redis.call('del', KEYS[1])
                else
                    return 0
                end
                """;
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>(script, Long.class);
        Long result = stringRedisTemplate.execute(redisScript, Collections.singletonList(key), value);
        return result != null && result > 0;
    }

    public Boolean hasKey(String key) {
        return stringRedisTemplate.hasKey(key);
    }

    public Long addToSet(String key, String... values) {
        return stringRedisTemplate.opsForSet().add(key, values);
    }

    public Boolean isSetMember(String key, String value) {
        return stringRedisTemplate.opsForSet().isMember(key, value);
    }

    public Set<String> setMembers(String key) {
        return stringRedisTemplate.opsForSet().members(key);
    }

    public Boolean expire(String key, Duration timeout) {
        return stringRedisTemplate.expire(key, timeout);
    }

    public Long delete(Collection<String> keys) {
        return stringRedisTemplate.delete(keys);
    }
}
