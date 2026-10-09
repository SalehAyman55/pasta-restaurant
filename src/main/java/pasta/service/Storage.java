package pasta.service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import pasta.util.Csv;

public final class Storage {

    private static final Path DIR = Paths.get(System.getProperty("pasta.data", "data"));

    private Storage() {
    }

    public static synchronized boolean exists(String name) {
        return Files.exists(DIR.resolve(name));
    }

    public static synchronized List<List<String>> read(String name) {
        Path path = DIR.resolve(name);
        List<List<String>> rows = new ArrayList<>();
        if (!Files.exists(path)) {
            return rows;
        }
        try {
            List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
            for (int i = 1; i < lines.size(); i++) {
                String line = lines.get(i);
                if (!line.isBlank()) {
                    rows.add(Csv.parse(line));
                }
            }
            return rows;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static synchronized void write(String name, String header, List<String> lines) {
        try {
            Files.createDirectories(DIR);
            Path target = DIR.resolve(name);
            Path temp = DIR.resolve(name + ".tmp");
            List<String> all = new ArrayList<>();
            all.add(header);
            all.addAll(lines);
            Files.write(temp, all, StandardCharsets.UTF_8);
            try {
                Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static synchronized void append(String name, String header, String line) {
        if (!exists(name)) {
            write(name, header, List.of(line));
            return;
        }
        try {
            Files.writeString(DIR.resolve(name), line + System.lineSeparator(), StandardCharsets.UTF_8, StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
