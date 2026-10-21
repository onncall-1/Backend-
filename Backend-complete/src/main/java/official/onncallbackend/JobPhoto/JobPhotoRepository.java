
package official.onncallbackend.JobPhoto;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface JobPhotoRepository extends JpaRepository<JobPhoto, Long> {

    List<JobPhoto> findByJobId(Long jobId);
}
