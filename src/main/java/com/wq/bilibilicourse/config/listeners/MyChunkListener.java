package com.wq.bilibilicourse.config.listeners;

import org.springframework.batch.core.annotation.AfterChunk;
import org.springframework.batch.core.annotation.BeforeChunk;
import org.springframework.batch.core.scope.context.ChunkContext;

/**
 * ChunkContext的监听器
 * 注解方式实现
 * @beforeChunk 每次读取数据之前都会调用
 * @AfterChunck 每次输出数据之前都会调用
 * 读取多少数据由 .chunck(n) 决定
 */
public class MyChunkListener {

    @BeforeChunk
    public void beforeChunk(ChunkContext chunkContext){
        System.out.println(chunkContext.getStepContext().getStepName() + "before...");
    }
    @AfterChunk
    public void afterChunk(ChunkContext chunkContext){
        System.out.println(chunkContext.getStepContext().getStepName() + "after...");
    }
}
