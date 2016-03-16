package org.boilerpipe.web;

import com.feedpresso.HTMLHighlighter;
import de.l3s.boilerpipe.document.Image;
import de.l3s.boilerpipe.document.TextDocument;
import de.l3s.boilerpipe.extractors.ArticleExtractor;
import de.l3s.boilerpipe.sax.BoilerpipeSAXInput;
import de.l3s.boilerpipe.sax.ImageExtractor;
import org.jboss.resteasy.logging.Logger;
import org.xml.sax.InputSource;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.io.StringReader;
import java.util.List;
import java.util.stream.Collectors;


@Path("/")
public class BoilerpipeService {
    Logger logger = Logger.getLogger(BoilerpipeService.class);

    ArticleExtractor articleExtractor = ArticleExtractor.getInstance();
    ImageExtractor imageExtractor = ImageExtractor.getInstance();

    @POST
    @Path("/extract")
    @Consumes("application/json")
    @Produces("application/json")
    public Object getContent(ExtractionRequest request) {
        try {
            String html = request.html;
            String url = request.url;

            if (request.extractHtml) {
                HTMLHighlighter hh = HTMLHighlighter.newExtractingInstance();

                InputSource is1 = new InputSource(new StringReader(html));
                TextDocument doc = new BoilerpipeSAXInput(is1).getTextDocument();

                articleExtractor.process(doc);

                InputSource is = new InputSource(new StringReader(html));
                return hh.process(doc, is);
            }

            if (request.extractImages) {
                TextDocument doc = new BoilerpipeSAXInput(new InputSource(new StringReader(html))).getTextDocument();
                articleExtractor.process(doc);

                InputSource is = new InputSource(new StringReader(html));

                List<Image> process = imageExtractor.process(doc, is);

                return process.stream()
                        .map(Image::getSrc)
                        .collect(Collectors.toList());

            }

            throw new RuntimeException("Not Supported operation");

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
