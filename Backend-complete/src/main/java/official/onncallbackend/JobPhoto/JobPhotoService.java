package official.onncallbackend.JobPhoto;

import official.onncallbackend.Auth.Security.AccessGuard;
import official.onncallbackend.Config.FileStorageService;
import official.onncallbackend.Job.Job;
import official.onncallbackend.Job.Repository.JobRepository;
import official.onncallbackend.common.ApiException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class JobPhotoService {

    private static final long MAX_BYTES = 5L * 1024 * 1024;

    private final JobPhotoRepository photoRepository;
    private final JobRepository jobRepository;
    private final FileStorageService fileStorageService;
    private final AccessGuard guard;

    public JobPhotoService(
            JobPhotoRepository photoRepository,
            JobRepository jobRepository,
            FileStorageService fileStorageService,
            AccessGuard guard) {
        this.photoRepository = photoRepository;
        this.jobRepository = jobRepository;
        this.fileStorageService = fileStorageService;
        this.guard = guard;
    }

    /** Client or provider on the job can add photos. */
    public JobPhoto upload(Long jobId, String description, MultipartFile file) {
        Job job = findJob(jobId);
        guard.requireParticipant(job.getClient().getId(), job.getProvider().getId());

        if (file == null || file.isEmpty()) {
            throw ApiException.badRequest("Photo is required");
        }
        if (file.getSize() > MAX_BYTES) {
            throw ApiException.badRequest("Photo must not exceed 5 MB");
        }

        String storedName = fileStorageService.store(file);

        JobPhoto photo = new JobPhoto();
        photo.setJobId(jobId);
        photo.setFileName(storedName);
        photo.setDescription(description);
        return photoRepository.save(photo);
    }

    public List<JobPhoto> getPhotosByJob(Long jobId) {
        Job job = findJob(jobId);
        guard.requireParticipant(job.getClient().getId(), job.getProvider().getId());
        return photoRepository.findByJobId(jobId);
    }

    public void deletePhoto(Long photoId) {
        JobPhoto photo = photoRepository.findById(photoId)
                .orElseThrow(() -> ApiException.notFound("Photo not found"));
        Job job = findJob(photo.getJobId());
        guard.requireParticipant(job.getClient().getId(), job.getProvider().getId());

        fileStorageService.delete(photo.getFileName());
        photoRepository.delete(photo);
    }

    private Job findJob(Long jobId) {
        return jobRepository.findById(jobId)
                .orElseThrow(() -> ApiException.notFound("Job not found with id: " + jobId));
    }
}
