
package teccr.justdoitcloud.service;

import org.springframework.stereotype.Service;
import teccr.justdoitcloud.data.Task;
import teccr.justdoitcloud.data.User;
import teccr.justdoitcloud.repository.TaskRepository;

import java.util.List;

@Service
public class TaskService {

    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    // Obtener todas las tareas de un usuario
    public List<Task> getTasksForUser(User user) {
        return taskRepository.findByUserId(user.getId());
    }

    // Guardar una nueva tarea en la base de datos
    public void addTaskToUser(User user, Task task) {
        task.setUserId(user.getId());
        taskRepository.save(task);
    }

    // Avanzar el estado de una tarea
    public void avanzarTarea(User user, Long id) {

        // Buscar la tarea en la base de datos
        Task tarea = taskRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Tarea no encontrada con ID: " + id
                        )
                );

        // Verificar que pertenece al usuario actual
        if (!user.getId().equals(tarea.getUserId())) {
            throw new IllegalArgumentException(
                    "La tarea no pertenece al usuario actual"
            );
        }

        // Determinar el siguiente estado
        Task.Status nuevoEstado;

        switch (tarea.getStatus()) {

            case PENDING ->
                    nuevoEstado = Task.Status.INPROGRESS;

            case INPROGRESS ->
                    nuevoEstado = Task.Status.DONE;

            case DONE -> {
                // Una tarea completada no puede avanzar
                return;
            }

            default -> throw new IllegalStateException(
                    "Estado de tarea no válido"
            );
        }

        // Crear una versión actualizada de la tarea
        Task tareaActualizada = new Task(
                tarea.getId(),
                tarea.getDescription(),
                tarea.getCreatedAt(),
                tarea.getDeadline(),
                nuevoEstado
        );

        // Conservar el propietario de la tarea
        tareaActualizada.setUserId(tarea.getUserId());

        // Persistir el nuevo estado en MySQL
        taskRepository.save(tareaActualizada);
    }
}
