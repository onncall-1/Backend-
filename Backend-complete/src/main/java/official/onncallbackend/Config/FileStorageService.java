package official.onncallbackend.Config;

import official.onncallbackend.common.ApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/**
 * Stores uploaded photos and documents on local disk under app.upload-dir.
 * Files get random UUID names, so they cannot be guessed. For production with
 * several servers, replace this with an object store (S3, GCS, etc.) behind the same methods.
 */
@Service
public class FileStorageService {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp", "pdf");

    private final Path root;

    public FileStorageService(@Value("${app.upload-dir}") String uploadDir) {
        this.root = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.root);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot create upload directory " + root, e);
        }
    }

    public String store(MultipartFile file) {
        String original = file.getOriginalFilename();
        String ext = (original == null || !original.contains("."))
                ? ""
                : original.substring(original.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);

        if (!ALLOWED_EXTENSIONS.contains(ext)) {
            throw ApiException.badRequest(
                    "Unsupported file type. Allowed: " + String.join(", ", ALLOWED_EXTENSIONS));
        }

        String storedName = UUID.randomUUID() + "." + ext;
        Path target = root.resolve(storedName);

        try (InputStream in = file.getInputStream()) {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not store file", e);
        }
        return storedName;
    }

    /** Resolves a stored file name to a path, refusing anything outside the upload directory. */
    public Path resolve(String fileName) {
        Path path = root.resolve(fileName).normalize();
        if (!path.startsWith(root) || fileName.contains("/") || fileName.contains("\\")) {
            throw ApiException.badRequest("Invalid file name");
        }
        if (!Files.exists(path)) {
            throw ApiException.notFound("File not found");
        }
        return path;
    }

    public void delete(String fileName) {
        try {
            Files.deleteIfExists(resolve(fileName));
        } catch (IOException e) {
            throw new UncheckedIOException("Could not delete file", e);
        }
    }
}
