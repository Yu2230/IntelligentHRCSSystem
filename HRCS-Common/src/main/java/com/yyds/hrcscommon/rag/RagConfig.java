package com.yyds.hrcscommon.rag;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.splitter.DocumentByParagraphSplitter;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingStore;

import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import java.util.Collections;
import java.util.List;

@Slf4j
@Configuration
public class RagConfig {

    private final EmbeddingModel embeddingModel;
    private final EmbeddingStore<TextSegment> embeddingStore;

    /**
     * 构造器注入，优先使用 auto-config 的 QwenEmbeddingModel
     */
    public RagConfig(EmbeddingModel embeddingModel, EmbeddingStore<TextSegment> embeddingStore) {
        this.embeddingModel = embeddingModel;
        this.embeddingStore = embeddingStore;
    }

    /**
     * 安全可启动的 ContentRetriever
     * - @Lazy 确保 Bean 在首次使用时才初始化
     * - 捕获 Qwen API 异常，避免启动失败
     */
    @Bean
    @Lazy
    public ContentRetriever contentRetriever() {
        log.info("开始初始化 ContentRetriever...");

        // 1. 加载文档
        List<Document> documents;
        try {
//            documents = FileSystemDocumentLoader.loadDocuments(
//                    "D:\\GraduationProject\\IntelligentHRCSSystem\\HRCS-Server\\src\\main\\resources\\docs"
//            );
            documents = FileSystemDocumentLoader.loadDocuments(
                    "/Users/huhuha/Desktop/interesting/intelligent-hr-system/IntelligentHRCSSystem/HRCS-Server/src/main/resources/docs"
            );
            log.info("文档加载完成，共 {} 个文档", documents.size());
        } catch (Exception e) {
            log.warn("文档加载失败，使用空文档列表继续启动", e);
            documents = Collections.emptyList();
        }

        // 2. 文档切割器
        DocumentByParagraphSplitter paragraphSplitter = new DocumentByParagraphSplitter(1000, 200);

        // 3. EmbeddingStoreIngestor
        EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                .documentSplitter(paragraphSplitter)
                .textSegmentTransformer(ts -> TextSegment.from(
                        ts.metadata().getString("file_name") + "\n" + ts.text(),
                        ts.metadata()
                ))
                .embeddingModel(embeddingModel)
                .embeddingStore(embeddingStore)
                .build();

        // 4. 安全加载文档向量
        try {
            if (!documents.isEmpty()) {
                ingestor.ingest(documents);
                log.info("文档向量化完成");
            }
        } catch (Exception e) {
            log.warn("Qwen API 调用失败，内容向量化未执行，本地 Mock 启动继续", e);
        }

        // 5. 创建 ContentRetriever
        ContentRetriever retriever;
        try {
            retriever = EmbeddingStoreContentRetriever.builder()
                    .embeddingStore(embeddingStore)
                    .embeddingModel(embeddingModel)
                    .maxResults(5)
                    .minScore(0.75)
                    .build();
            log.info("ContentRetriever 初始化完成");
        } catch (Exception e) {
            log.warn("ContentRetriever 创建失败，返回空实现继续启动", e);
            retriever = query -> Collections.emptyList(); // Mock 返回空列表
        }

        return retriever;
    }

}
