
package teccr.justdoitcloud.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import teccr.justdoitcloud.data.Task;
import teccr.justdoitcloud.data.User;
import teccr.justdoitcloud.service.TaskService;

import java.time.LocalDateTime;

@Slf4j
@Controller
@RequestMapping("/user/tasks")
@SessionAttributes("user")
public class UserTasksController {

    private final TaskService taskService;

    public UserTasksController(TaskService taskService) {
        this.taskService = taskService;
    }

    // Mostrar el listado de tareas
    @GetMapping
    public String showUserTasks(Model model) {

        model.addAttribute(
                "newTask",
                new Task(
                        null,
                        "",
                        LocalDateTime.now(),
                        null,
                        Task.Status.PENDING
                )
        );

        // Recuperar el usuario de la sesión
        User user = (User) model.getAttribute("user");

        if (user != null) {
            user.setTasks(
                    taskService.getTasksForUser(user)
            );
        }

        return "usertasks";
    }

    // Registrar una nueva tarea
    @PostMapping
    public String addTask(
            @Valid @ModelAttribute("newTask") Task newTask,
            Errors errors,
            @ModelAttribute("user") User user) {

        log.info("Agregando tarea: {}", newTask);

        if (errors.hasErrors()) {
            user.setTasks(
                    taskService.getTasksForUser(user)
            );
            return "usertasks";
        }

        taskService.addTaskToUser(user, newTask);

        return "redirect:/user/tasks";
    }

    // Avanzar una tarea mediante una única ruta POST
    @PostMapping("/advance")
    public String avanzarTarea(
            @RequestParam("id") Long id,
            @ModelAttribute("user") User user) {

        log.info(
                "Avanzando estado de tarea con ID: {}",
                id
        );

        // La lógica se ejecuta en el servicio
        taskService.avanzarTarea(user, id);

        // Recargar el listado desde la base de datos
        return "redirect:/user/tasks";
    }
}
