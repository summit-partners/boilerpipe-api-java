package org.boilerpipe.web;

import com.feedpresso.HTMLHighlighter;
import de.l3s.boilerpipe.document.Image;
import de.l3s.boilerpipe.document.TextDocument;
import de.l3s.boilerpipe.extractors.*;
import de.l3s.boilerpipe.sax.BoilerpipeSAXInput;
import de.l3s.boilerpipe.sax.ImageExtractor;
import org.jboss.resteasy.logging.Logger;
import org.xml.sax.InputSource;

import javax.ws.rs.*;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Path("/")
public class BoilerpipeResource {
    Logger logger = Logger.getLogger(BoilerpipeResource.class);

    ArticleExtractor articleExtractor = ArticleExtractor.getInstance();
    ImageExtractor imageExtractor = ImageExtractor.getInstance();

    Map<String, ExtractorBase> extractors = Stream.of(new Object[][]{
        { "article", ArticleExtractor.INSTANCE },
        { "articleSentecnes", ArticleSentencesExtractor.INSTANCE },
        { "canola", CanolaExtractor.INSTANCE },
        { "default", DefaultExtractor.INSTANCE },
        { "keepEverything", KeepEverythingExtractor.INSTANCE },
        // { "keepEveryingMinK", KeepEverythingWithMinKWordsExtractor.INSTANCE },
        { "largestContent", LargestContentExtractor.INSTANCE },
        { "numWordsRules", NumWordsRulesExtractor.INSTANCE }
    }).collect(Collectors.toMap(data -> (String)data[0], data -> (ExtractorBase)data[1]));

    @GET
    @Path("/")
    @Produces("application/json")
    public Object hello() {
        return new Hello();
    }

    @POST
    @Path("/extract")
    @Consumes("application/json")
    @Produces("application/json")
    public Object getContent(ExtractionRequest request) {
        try {
            String html = request.html;

            if (request.extractHtml) {
                return extractHtml(html);
            }

            if (request.extractImages) {
                return extractImages(html);
            }

            throw new RuntimeException("Not Supported operation");

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @POST
    @Path("/extractHtml")
    @Consumes("text/html")
    @Produces("application/json")
    public String extractHtml(String html) {
        try {
            HTMLHighlighter htmlHighlighter = HTMLHighlighter.newExtractingInstance();

            InputSource is1 = new InputSource(new StringReader(html));
            TextDocument doc = new BoilerpipeSAXInput(is1).getTextDocument();
//            ArticleExtractor.INSTANCE.getText(myHtml);
            articleExtractor.process(doc);

            InputSource is = new InputSource(new StringReader(html));
            String process = htmlHighlighter.process(doc, is);
            return process;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @POST
    @Path("/extractImagesMeta")
    @Consumes("text/html")
    @Produces("application/json")
    public List<Image> extractImagesMeta(String html) {
        try {
            TextDocument doc = new BoilerpipeSAXInput(new InputSource(new StringReader(html))).getTextDocument();
            articleExtractor.process(doc);

            InputSource is = new InputSource(new StringReader(html));

            List<Image> process = imageExtractor.process(doc, is);

            return new ArrayList<>(process);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @POST
    @Path("/extractImages")
    @Consumes("text/html")
    @Produces("application/json")
    public List<String> extractImages(String html) {
        try {
            TextDocument doc = new BoilerpipeSAXInput(new InputSource(new StringReader(html))).getTextDocument();
            articleExtractor.process(doc);

            InputSource is = new InputSource(new StringReader(html));

            List<Image> process = imageExtractor.process(doc, is);

            return process.stream()
                    .filter(image -> {
                        String width = toEmpty(image.getWidth());
                        String height = toEmpty(image.getHeight());

                        if (width.isEmpty() && height.isEmpty()) {
                            return true;
                        }

                        if (width.contains("%")) {
                            return true;
                        }

                        if (!width.isEmpty() && getInteger(width) < 100) {
                            return false;
                        }

                        if (!height.isEmpty() && getInteger(height) < 100) {
                            return false;
                        }

                        return true;
                    })
                    .map(Image::getSrc)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @POST
    @Path("/extractText")
    @Consumes("text/html")
    @Produces("text/plain")
    public String extractText(
        String html, 
        @DefaultValue("ArticleExtractor") @QueryParam("extractor") String extractorClassName
    ) {
        try {
            ExtractorBase extractor = this.extractors.get(extractorClassName);
            if (extractor == null) {
                extractor = ArticleExtractor.INSTANCE;
            }

            logger.debug("Using " + extractor + " for plain-text extraction");

            InputSource is1 = new InputSource(new StringReader(html));
            TextDocument doc = new BoilerpipeSAXInput(is1).getTextDocument();
            extractor.process(doc);
            return doc.getContent();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private int getInteger(String integer) {
        try {
            String cleanedInt = integer.replaceAll("[^\\d.]", "");
            return Integer.parseInt(cleanedInt);
        } catch (NumberFormatException e) {
            logger.warn("Could not Parse integer " + integer);
            return 0;
        }
    }

    private String toEmpty(String string) {
        if (string == null) {
            return "";
        }

        return string;
    }
}
