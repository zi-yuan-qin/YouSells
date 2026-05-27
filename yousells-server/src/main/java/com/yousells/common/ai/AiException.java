package com.yousells.common.ai;

/**
 * AI 调用异常，统一包装 LLM / Embedding / Qdrant 各类错误。
 */
public class AiException extends RuntimeException {

    public AiException(String message) {
        super(message);
    }

    public AiException(String message, Throwable cause) {
        super(message, cause);
    }
}
