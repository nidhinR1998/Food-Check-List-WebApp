package com.springbootfproject.firstWebApp.controller;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

import javax.print.attribute.standard.MediaTray;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.springbootfproject.firstWebApp.dto.TodoFilterReq;
import com.springbootfproject.firstWebApp.dto.TodoFilterRes;
import com.springbootfproject.firstWebApp.repository.TodoRepository;
import com.springbootfproject.firstWebApp.service.TodoService;
import com.springbootfproject.firstWebApp.todomodel.Todo;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/todo")
@SessionAttributes("username")
public class TodoControllerJpa {
	private Logger logger = LoggerFactory.getLogger(getClass());
	private TodoRepository todoRepository;
	private TodoService todoService;
	public TodoControllerJpa(TodoService todoService, TodoRepository todoRepository) {
		super();
		this.todoService = todoService;
		this.todoRepository = todoRepository;
	}

	@RequestMapping(value = "/list-todos", method = RequestMethod.GET)
	private String listAllTodos(ModelMap model, Principal principal) {
		String username = principal.getName();
		logger.debug("Username: {}", username);
		List<Todo> todos = todoRepository.findByUsername(username);
		int totalAmount = todos.stream().mapToInt(Todo::getAmount).sum();
		model.addAttribute("todos", todos);
		model.addAttribute("totalAmount", totalAmount);
		model.addAttribute("username", username); // Add username explicitly to the model
		logger.debug("List All Todos Hit");
		return "listTodos";
	}
	
	@RequestMapping(value = "/add-todo", method = RequestMethod.GET)
	private String showNewTodoPage(ModelMap model, Principal principal) {
		String username = getLoggedInUsername(model);
		Todo todo = new Todo(0, username, "", LocalDate.now().plusYears(1), true, "", 0);
		model.put("todo", todo);
		logger.debug("Show New Todo Page Hit");
		return "todo";
	}

	@RequestMapping(value = "/add-todo", method = RequestMethod.POST)
	private String addNewTodo(ModelMap model, @Valid Todo todo, BindingResult result) {
		if (result.hasErrors()) {

			return "todo";
		}
		String username = getLoggedInUsername(model);
		todo.setUsername(username);
		todoRepository.save(todo);		
		logger.debug("Add New Todo Hit");
		return "redirect:list-todos";
	}

	@RequestMapping(value = "/delete-todo", method = RequestMethod.GET)
	private String deleteTodo(@RequestParam int id) {
		logger.debug("Delete Todo ID: {}", id);
		todoRepository.deleteById(id);
		logger.debug("Delete Todo Hit");
		return "redirect:list-todos";
	}

	@RequestMapping(value = "/update-todo", method = RequestMethod.GET)
	private String showUpdateTodo(@RequestParam int id, ModelMap model) {
		Todo todo = todoRepository.findById(id).get();
		model.addAttribute("todo", todo);
		logger.debug("Show Update Todo Hit");
		return "todo";
	}

	@RequestMapping(value = "/update-todo", method = RequestMethod.POST)
	private String updateTodo(ModelMap model, @Valid Todo todo, BindingResult result) {
		if (result.hasErrors()) {

			return "todo";
		}
		String username = getLoggedInUsername(model);
		todo.setUsername(username);
		todoRepository.save(todo);		
		logger.debug("Update Todo Hit");
		return "redirect:list-todos";
	}

	private String getLoggedInUsername(ModelMap model) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		return authentication.getName();
	}
	
	@RequestMapping(value = "/filter-todos", method = RequestMethod.POST)
	@ResponseBody
	public TodoFilterRes filterTodos(@RequestBody TodoFilterReq req) {
        TodoFilterRes todoFilter = todoService.getfilterTodos(req);
        // Calculate total amount
        int totalAmount = todoService.calculateTotalAmount(todoFilter.getTodos());
        todoFilter.setTotalAmount(totalAmount);
        logger.debug("Total Amount: {}",totalAmount);
        return todoFilter;
    }

}