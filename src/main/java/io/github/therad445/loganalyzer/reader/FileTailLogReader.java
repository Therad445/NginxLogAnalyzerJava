package io.github.therad445.loganalyzer.reader;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class FileTailLogReader implements Reader {
    private final String file;

    public FileTailLogReader(String file) {
        this.file = file;
    }

    @Override
    public Stream<String> read() {
        throw new UnsupportedOperationException("FileTailLogReader не поддерживает batch");
    }

    @Override
    public void read(Consumer<String> consumer) throws IOException {
        try (var raf = new RandomAccessFile(file, "r")) {
            long pos = 0;
            while (true) {
                raf.seek(pos);
                String line;
                while ((line = raf.readLine()) != null) {
                    consumer.accept(line);
                }
                pos = raf.getFilePointer();
                Thread.sleep(500);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
