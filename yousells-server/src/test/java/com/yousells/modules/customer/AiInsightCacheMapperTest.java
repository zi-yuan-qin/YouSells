package com.yousells.modules.customer;

import com.yousells.modules.customer.dto.AiInsightResponse;
import com.yousells.modules.customer.entity.AiInsightCache;
import com.yousells.modules.customer.mapper.AiInsightCacheMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@Transactional
class AiInsightCacheMapperTest {

    @Autowired
    private AiInsightCacheMapper mapper;

    @Test
    void shouldInsertAndFindValidCache() {
        AiInsightCache cache = new AiInsightCache();
        cache.setCustomerId(1L);
        cache.setInsightJson("{\"summary\":\"test\"}");
        cache.setGeneratedAt(LocalDateTime.now());
        cache.setExpiresAt(LocalDateTime.now().plusHours(2));
        mapper.insert(cache);

        AiInsightCache found = mapper.selectValidByCustomerId(1L);
        assertThat(found).isNotNull();
        assertThat(found.getCustomerId()).isEqualTo(1L);
        assertThat(found.getInsightJson()).contains("test");
    }

    @Test
    void shouldReturnNullWhenCacheExpired() {
        AiInsightCache cache = new AiInsightCache();
        cache.setCustomerId(2L);
        cache.setInsightJson("{\"summary\":\"expired\"}");
        cache.setGeneratedAt(LocalDateTime.now().minusHours(3));
        cache.setExpiresAt(LocalDateTime.now().minusHours(1));
        mapper.insert(cache);

        AiInsightCache found = mapper.selectValidByCustomerId(2L);
        assertThat(found).isNull();
    }

    @Test
    void shouldReturnNullWhenNoCache() {
        AiInsightCache found = mapper.selectValidByCustomerId(9999L);
        assertThat(found).isNull();
    }

    @Test
    void shouldOverwriteCacheOnDuplicateCustomer() {
        AiInsightCache first = new AiInsightCache();
        first.setCustomerId(3L);
        first.setInsightJson("{\"v\":1}");
        first.setGeneratedAt(LocalDateTime.now());
        first.setExpiresAt(LocalDateTime.now().plusHours(2));
        mapper.insert(first);

        AiInsightCache second = new AiInsightCache();
        second.setCustomerId(3L);
        second.setInsightJson("{\"v\":2}");
        second.setGeneratedAt(LocalDateTime.now());
        second.setExpiresAt(LocalDateTime.now().plusHours(2));

        // delete old then insert new (since UNIQUE on customer_id)
        mapper.deleteById(first.getId());
        mapper.insert(second);

        AiInsightCache found = mapper.selectValidByCustomerId(3L);
        assertThat(found).isNotNull();
        assertThat(found.getInsightJson()).isEqualTo("{\"v\":2}");
    }

    @Test
    void shouldCreateEmptyResponse() {
        AiInsightResponse empty = AiInsightResponse.empty(42L);
        assertThat(empty.customerId()).isEqualTo(42L);
        assertThat(empty.intentTrend()).isEqualTo("平稳");
        assertThat(empty.conversionProbability()).isEqualTo("低");
        assertThat(empty.conversionConfidence()).isZero();
        assertThat(empty.keyConcerns()).isEmpty();
    }
}
