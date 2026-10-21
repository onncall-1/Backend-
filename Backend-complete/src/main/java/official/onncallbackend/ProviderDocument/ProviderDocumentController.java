
package official.onncallbackend.ProviderDocument;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/provider-documents")
public class ProviderDocumentController {

    private final ProviderDocumentService documentService;

    public ProviderDocumentController(
            ProviderDocumentService documentService) {
        this.documentService = documentService;
    }

    @PostMapping(
            value = "/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ProviderDocument upload(
            @RequestParam Long providerId,
            @RequestParam String documentType,
            @RequestPart MultipartFile file) {

        return documentService.upload(
                providerId, documentType, file);
    }

    @GetMapping("/provider/{providerId}")
    public List<ProviderDocument> getByProvider(
            @PathVariable Long providerId) {

        return documentService.getByProvider(providerId);
    }
}
