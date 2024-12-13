package backend.academy.loganalyzer.reader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.util.stream.Stream;

public class URLReader implements Reader {
    @Override
    public Stream<String> read(String urlString) throws IOException {
        URL url = URI.create(urlString).toURL();
        BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
        return reader.lines();
    }

}
