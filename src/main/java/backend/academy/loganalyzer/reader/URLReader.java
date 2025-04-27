package backend.academy.loganalyzer.reader;

import java.io.IOException;
import java.net.URL;
import java.util.Scanner;
import java.util.stream.Stream;

public class URLReader implements Reader {
    private final String url;

    public URLReader(String url) {
        this.url = url;
    }

    @Override
    public Stream<String> read() throws IOException {
        Scanner sc = new Scanner(new URL(url).openStream());
        return sc.tokens();
    }
}
