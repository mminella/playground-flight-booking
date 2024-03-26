package ai.spring.demo.ai.playground;

import java.io.IOException;

import ai.spring.demo.ai.playground.config.SpringApplicationContextInitializer;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.theme.Theme;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.ai.embedding.EmbeddingClient;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

@SpringBootApplication
@Theme(value = "customer-support-agent")
public class Application implements AppShellConfigurator {

	private static final Logger logger = LoggerFactory.getLogger(Application.class);

	public static void main(String[] args) {
		new SpringApplicationBuilder(Application.class)
				.initializers(new SpringApplicationContextInitializer())
				.application()
				.run(args);	}

	// In the real world, ingesting documents would often happen separately, on a CI server or similar
	@Bean
	CommandLineRunner docsToEmbeddings(
			EmbeddingClient embeddingClient,
			VectorStore vectorStore,
			ResourceLoader resourceLoader) throws IOException {

		return args -> {

			Resource resource = resourceLoader.getResource("classpath:rag/terms-of-service.txt");

			// Ingest the document into the vector store
			vectorStore
					.accept(new TokenTextSplitter(30, 20, 1, 10000, true)
							.apply(new TextReader(resource).get()));

			vectorStore.similaritySearch("Cancelling Bookings").forEach(doc -> {
				logger.info("Similar Document: {}", doc.getContent());
			});
		};
	}

	@Bean
	public VectorStore vectorStore(EmbeddingClient embeddingClient) {
		return new SimpleVectorStore(embeddingClient);
	}
}
