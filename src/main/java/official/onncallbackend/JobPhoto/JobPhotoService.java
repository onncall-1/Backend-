
package official.onncallbackend.JobPhoto;

import official.onncallbackend.Config.FileStorageService;
import official.onncallbackend.Job.Repository.JobRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class JobPhotoService {

    private final JobPhotoRepository photoRepository;
    private final JobRepository jobRepository;
    private final FileStorageService fileStorageService;

    public JobPhotoService(
            JobPhotoRepository photoRepository,
            JobRepository jobRepository,
            FileStorageService fileStorageService) {
        this.photoRepository = photoRepository;
        this.jobRepository = jobRepository;
        this.fileStorageService = fileStorageService;
    }

    public JobPhoto upload(Long jobId, String description,
                           MultipartFile file) {

        if (!jobRepository.existsById(jobId)) {
            throw new IllegalArgumentException("Job not found");
        }

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Photo is required");
        }

        if (file.getSize() > 5L * 1024 * 1024) {
            throw new IllegalArgumentException(
                    "Photo must not exceed 5 MB");
        }

        String storedName = fileStorageService.store(file);

        JobPhoto photo = new JobPhoto();
        photo.setJobId(jobId);
        photo.setFileName(storedName);
        photo.setDescription(description);

        return photoRepository.save(photo);
    }

    public List<JobPhoto> getPhotosByJob(Long jobId) {

        if (!jobRepository.existsById(jobId)) {
            throw new IllegalArgumentException("Job not found");
        }

        return photoRepository.findByJobId(jobId);
    }

    public void deletePhoto(Long photoId) {

        JobPhoto photo = photoRepository.findById(photoId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Photo not found"));

        photoRepository.delete(photo);
    }
}
