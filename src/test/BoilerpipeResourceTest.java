import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.GetRequest;
import io.github.binout.jaxrsunit.JaxrsResource;
import io.github.binout.jaxrsunit.JaxrsResponse;
import io.github.binout.jaxrsunit.JaxrsServer;
import io.github.binout.jaxrsunit.JaxrsUnit;
import org.boilerpipe.web.BoilerpipeResource;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class BoilerpipeResourceTest {
    private JaxrsServer server;

    @Before
    public void init() {
        server = JaxrsUnit.newServer(BoilerpipeResource.class);
    }

    @Test
    public void should_return_hello() {
        JaxrsResource resource = server.resource("/");
        JaxrsResponse response = resource.get();

        assertThat(response.ok(), equalTo(true));
    }


    @Test
    public void should_extract_html() {
        String content = getContent();

        JaxrsResource resource = server.resource("/extractHtml");
        JaxrsResponse response = resource.post(content);

        assertThat(response.ok(), equalTo(true));
        assertThat(response.content().toLowerCase(), containsString("tamsūs duomenys"));
        assertThat(response.content().toLowerCase(), containsString("kuria vis mažiau ir  mažiau neutrinų, turinčių vis didesnę"));
        assertThat(response.content(), containsString("http://www.technologijos.lt/upload/image/n/mokslas/astronomija_ir_kosmonautika/S-48726/nuotrauka-88682/1-00.jpg"));
    }


    private String getContent() {
        GetRequest getRequest = Unirest.get("http://www.technologijos.lt/n/mokslas/astronomija_ir_kosmonautika/S-48726/straipsnis/WIMPzilos-monstriskosios-daleles-is-laiko-pradzios");

        try {
            return getRequest.asString().getBody();
        } catch (UnirestException e) {
            throw new RuntimeException(e);
        }
    }

}