package io.balleyndev.ppmtool.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.balleyndev.ppmtool.Exceptions.ProjectNotFoundException;
import io.balleyndev.ppmtool.domain.Backlog;
import io.balleyndev.ppmtool.domain.ProjectTask;
import io.balleyndev.ppmtool.repositories.BacklogRepository;
import io.balleyndev.ppmtool.repositories.ProjectRepository;
import io.balleyndev.ppmtool.repositories.ProjectTaskRepository;

@Service
public class ProjectTaskService {

	@Autowired
	private BacklogRepository backlogRepository;

	@Autowired
	private ProjectRepository projectRepository;

	@Autowired
	private ProjectTaskRepository projectTaskRepository;

	@Autowired
	private ProjectService projectService;

	public ProjectTask addProjectTask(String projectIdentifier, ProjectTask projectTask, String username) {

		// Exception id not found
		/*
		 * "" ProjectNotFound: "Project not found";
		 */
		// PTs to be added to a specific project, project != null, BL exists
		// set the BL to PT
		String projectIdentifierUP = projectIdentifier.toUpperCase();
		Backlog backlog = projectService.findProjectByIdentifier(projectIdentifier, username).getBacklog();// backlogRepository.findByProjectIdentifier(projectIdentifierUP);
		projectTask.setBacklog(backlog);

		// we want our project sequence to be like this
		// ProjectIdentifier - 1
		// ProjectIdentifier - 2
		// Update the BL Sequence
		Integer BacklogSequence = backlog.getPTSequence();
		BacklogSequence++;

		backlog.setPTSequence(BacklogSequence);

		// Add Sequence to Task
		projectTask.setProjectSequence(projectIdentifierUP + "-" + BacklogSequence);
		projectTask.setProjectIdentifier(projectIdentifierUP);

		// Initial priority when priority null
		if (projectTask.getPriority() == null || projectTask.getPriority() == 0) {
			projectTask.setPriority(3);
		}

		// Initial status when status null
		if (projectTask.getStatus() == null || projectTask.getStatus() == "") {
			projectTask.setStatus("TO_DO");
		}

		return projectTaskRepository.save(projectTask);

	}

	public Iterable<ProjectTask> findBacklogById(String id, String username) {
		projectService.findProjectByIdentifier(id, username);

		return projectTaskRepository.findByProjectIdentifierOrderByPriority(id);
	}

	public ProjectTask findPTByProjectSequence(String backlog_id, String pt_id, String username) {
		// make sure we are searching on an existing backlog
		projectService.findProjectByIdentifier(backlog_id, username);

		// make sure our task exists
		ProjectTask projectTask = projectTaskRepository.findByProjectSequence(pt_id);
		if (projectTask == null) {
			throw new ProjectNotFoundException("Project Task with ID: '" + pt_id + "' not found");
		}

		// make sure that the backlog/project id in the path corresponds to the right
		// project
		if (!projectTask.getProjectIdentifier().equals(backlog_id)) {
			throw new ProjectNotFoundException(
					"Project Task '" + pt_id + "' does not exist in project: '" + backlog_id);
		}

		// make sure we are searching on the right backlog
		return projectTask;
	}

	public ProjectTask updateByProjectSequence(ProjectTask updatedTask, String backlog_id, String pt_id,
			String username) {
		// ProjectTask projectTask =
		// projectTaskRepository.findByProjectSequence(updatedTask.getProjectSequence());
		// ProjectTask projectTask = projectTaskRepository.findByProjectSequence(pt_id);
		ProjectTask projectTask = findPTByProjectSequence(backlog_id, pt_id, username);

		projectTask = updatedTask;
		return projectTaskRepository.save(projectTask);
	}

	public void deletePTByProjectSequence(String backlog_id, String pt_id, String username) {
		ProjectTask projectTask = findPTByProjectSequence(backlog_id, pt_id, username);

		/*
		 * Backlog backlog = projectTask.getBacklog(); List<ProjectTask> pts =
		 * backlog.getProjectTasks(); pts.remove(projectTask);
		 * backlogRepository.save(backlog);
		 */

		projectTaskRepository.delete(projectTask);
	}

}
