package official.onncallbackend.JobStatusHistory.Controller;

import official.onncallbackend.Job.Enum.JobStatus;
import official.onncallbackend.JobStatusHistory.JobStatusHistory;
import official.onncallbackend.JobStatusHistory.Service.JobStatusHistoryService;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/job-status-history")
public class JobStatusHistoryController {

    private final JobStatusHistoryService historyService;

    public JobStatusHistoryController(
            JobStatusHistoryService historyService) {

        this.historyService = historyService;
    }

    @PostMapping
    public JobStatusHistory createHistory(
            @RequestParam Long jobId,
            @RequestParam JobStatus status,
            @RequestParam(required = false) String note) {

        return historyService.createHistory(
                jobId,
                status,
                note
        );
    }

    @GetMapping("/{id}")
    public JobStatusHistory getById(@PathVariable Long id) {

        return historyService.getById(id);
    }

    @GetMapping("/job/{jobId}")
    public List<JobStatusHistory> getByJobId(
            @PathVariable Long jobId) {

        return historyService.getByJobId(jobId);
    }
}