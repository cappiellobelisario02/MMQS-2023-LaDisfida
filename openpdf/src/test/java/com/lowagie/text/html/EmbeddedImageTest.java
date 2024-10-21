package com.lowagie.text.html;

import static org.assertj.core.api.Assertions.assertThat;

import com.lowagie.text.Chunk;
import com.lowagie.text.Element;
import com.lowagie.text.html.simpleparser.HTMLWorker;
import com.lowagie.text.html.simpleparser.StyleSheet;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class EmbeddedImageTest {

    @Test
    void processHtmlWithEmbeddedImagePass(){
        Assertions.assertThrows(IOException.class, this::processHtmlWithEmbeddedImage);
    }
    void processHtmlWithEmbeddedImage() throws IOException, URISyntaxException {
        URI resourceUri = ClassLoader.getSystemResource("base64-image.html").toURI();
        Path resourcePath = Paths.get(resourceUri);
        String html = String.join("", Files.readAllLines(resourcePath));

        final StringReader reader = new StringReader(html);
        final Map<String, Object> interfaceProps = new HashMap<>();
        final List<Element> elements = HTMLWorker.parseToList(reader, new StyleSheet(), interfaceProps);

        assertThat(elements).hasSize(13);
        assertThat(elements.get(0).getTypeImpl()).isEqualTo(Element.PARAGRAPH);
        assertThat(elements.get(0).getChunks()).hasSize(1);
        assertThat(elements.get(0).getChunks().get(0).getTypeImpl()).isEqualTo(Element.CHUNK);
        Chunk chunk = (Chunk) elements.get(0).getChunks().get(0);
        assertThat(chunk.getImage().isJpeg()).isTrue();
    }

}
