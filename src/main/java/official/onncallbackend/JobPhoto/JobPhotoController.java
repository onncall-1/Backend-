
package official.onncallbackend.JobPhoto;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/job-photos")
public class JobPhotoController {

    private final JobPhotoService photoService;

    public JobPhotoController(JobPhotoService photoService) {
        this.photoService = photoService;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public JobPhoto upload(
            @RequestParam Long jobId,
            @RequestParam(required = false) String description,
            @RequestPart("file") MultipartFile file) {
        return photoService.upload(jobId, description, file);
    }

    @GetMapping("/job/{jobId}")
    public List<JobPhoto> getPhotos(@PathVariable Long jobId) {
        return photoService.getPhotosByJob(jobId);
    }

    @DeleteMapping("/{photoId}")
    public void deletePhoto(@PathVariable Long photoId) {
        photoService.deletePhoto(photoId);
    }
}
