package com.yousells.common.ai;

/**
 * LLM 调用统一封装 —— Prompt 管理、结构化输出、Embedding。
 * 四个 P3 模块均通过此接口调用 AI 能力，不直接依赖具体模型厂商。
 */
public interface AiService {

    /**
     * 纯文本生成，返回模型原始输出。
     */
    String generate(String systemPrompt, String userPrompt);

    /**
     * 结构化 JSON 输出，要求模型按指定 schema 返回可反序列化的对象。
     * 实现层负责在 prompt 中注入 JSON Schema 约束，并解析/校验响应。
     */
    <T> T generateStructured(String systemPrompt, String userPrompt, Class<T> schema);

    /**
     * 文本转向量，用于语义搜索 / 相似度计算。
     */
    float[] embed(String text);
}
