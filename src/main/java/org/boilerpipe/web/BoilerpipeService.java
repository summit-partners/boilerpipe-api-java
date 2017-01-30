package org.boilerpipe.web;

import com.feedpresso.HTMLHighlighter;
import de.l3s.boilerpipe.document.Image;
import de.l3s.boilerpipe.document.TextDocument;
import de.l3s.boilerpipe.extractors.ArticleExtractor;
import de.l3s.boilerpipe.sax.BoilerpipeSAXInput;
import de.l3s.boilerpipe.sax.ImageExtractor;
import org.jboss.resteasy.logging.Logger;
import org.xml.sax.InputSource;

import javax.ws.rs.*;
import java.io.StringReader;
import java.util.List;
import java.util.stream.Collectors;


@Path("/")
public class BoilerpipeService {
    Logger logger = Logger.getLogger(BoilerpipeService.class);

    ArticleExtractor articleExtractor = ArticleExtractor.getInstance();
    ImageExtractor imageExtractor = ImageExtractor.getInstance();

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
            HTMLHighlighter hh = HTMLHighlighter.newExtractingInstance();

            InputSource is1 = new InputSource(new StringReader(html));
            TextDocument doc = new BoilerpipeSAXInput(is1).getTextDocument();

            articleExtractor.process(doc);

            InputSource is = new InputSource(new StringReader(html));
            return hh.process(doc, is);
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

                        if (getInteger(width) < 100) {
                            return false;
                        }

                        if (getInteger(height) < 100) {
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

    private int getInteger(String width) {
        try {
            return Integer.parseInt(width);
        } catch (NumberFormatException e) {
            logger.warn("Could not Parse integer %s", width);
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
