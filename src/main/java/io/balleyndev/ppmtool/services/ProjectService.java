package io.balleyndev.ppmtool.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.balleyndev.ppmtool.Exceptions.ProjectIdException;
import io.balleyndev.ppmtool.Exceptions.ProjectNotFoundException;
import io.balleyndev.ppmtool.domain.Backlog;
import io.balleyndev.ppmtool.domain.Project;
import io.balleyndev.ppmtool.domain.User;
import io.balleyndev.ppmtool.repositories.BacklogRepository;
import io.balleyndev.ppmtool.repositories.ProjectRepository;
import io.balleyndev.ppmtool.repositories.UserRepository;

@Service
public class ProjectService {

	@Autowired
	private ProjectRepository projectRepository;

	@Autowired
	private BacklogRepository backlogRepository;

	@Autowired
	private UserRepository userRepository;

	public Project saveOrUpdateProject(Project project, String username) {

		// project.getId != null
		// find project wit db id. Id can still be null cause doesnt need to be valid

		if (project.getId() != null) {
			Project existingProject = projectRepository.findByProjectIdentifier(project.getProjectIdentifier());

			if (existingProject != null && (!existingProject.getProjectLeader().equals(username))) {
				throw new ProjectNotFoundException("Project not found in your account");
			} else if (existingProject == null) {
				throw new ProjectNotFoundException("Project with ID: '" + project.getProjectIdentifier()
						+ "' canot be updated because it doesn't exist");
			}
		}

		try {
			User user = userRepository.findByUsername(username);
			project.setUser(user);
			project.setProjectLeader(user.getUsername());
			project.setProjectIdentifier(project.getProjectIdentifier().toUpperCase());

			String projectIDUP = project.getProjectIdentifier().toUpperCase();
			project.setProjectIdentifier(projectIDUP);

			if (project.getId() == null) {
				Backlog backlog = new Backlog();
				project.setBacklog(backlog);
				backlog.setProject(project);
				backlog.setProjectIdentifier(projectIDUP);
			}

			if (project.getId() != null) {
				project.setBacklog(backlogRepository.findByProjectIdentifier(projectIDUP));
			}

			return projectRepository.save(project);

		} catch (Exception e) {
			throw new ProjectIdException(
					"Project ID '" + project.getProjectIdentifier().toUpperCase() + "' already exists");
		}

	}

	public Project findProjectByIdentifier(String projectId, String username) {

		Project project = projectRepository.findByProjectIdentifier(projectId.toUpperCase());
		if (project == null) {
			throw new ProjectIdException("Project ID '" + projectId + "' does not exist");
		}

		if (!project.getProjectLeader().equals(username)) {
			throw new ProjectNotFoundException("Project not found in your account");
		}

		return project;
	}

	public Iterable<Project> findAllProjects(String username) {
		return projectRepository.findAllByProjectLeader(username);
	}

	public void deleteProjectByIdentifier(String projectId, String username) {

		projectRepository.delete(findProjectByIdentifier(projectId, username));
	}

}
