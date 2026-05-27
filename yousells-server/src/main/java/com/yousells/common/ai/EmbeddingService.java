package com.yousells.common.ai;

import java.util.List;
import java.util.Map;

/**
 * 向量嵌入与语义检索服务。
 * 封装 Embedding 生成 + Qdrant 向量数据库 CRUD。
 */
public interface EmbeddingService {

    /**
     * 初始化话术向量集合，在应用启动时调用一次。
     */
    void initScriptCollection();

    /**
     * 写入或更新一条向量记录。
     */
    void upsertScript(String id, String text, Map<String, Object> payload);

    /**
     * 删除一条向量记录。
     */
    void deleteScript(String id);

    /**
     * 语义搜索，返回相似度最高的 N 条记录 ID。
     */
    List<SearchResult> searchSimilar(String queryText, int limit);

    record SearchResult(String id, double score, Map<String, Object> payload) {}
}
