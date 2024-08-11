package com.springbootfproject.firstWebApp.controller;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;

import com.springbootfproject.firstWebApp.dto.TodoFilterReq;
import com.springbootfproject.firstWebApp.dto.TodoFilterRes;
import com.springbootfproject.firstWebApp.repository.AdvanceRepository;
import com.springbootfproject.firstWebApp.repository.TodoRepository;
import com.springbootfproject.firstWebApp.service.TodoService;
import com.springbootfproject.firstWebApp.todomodel.Advance;
import com.springbootfproject.firstWebApp.todomodel.Todo;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/todo")
@SessionAttributes("username")
public class TodoControllerJpa {
	private Logger logger = LoggerFactory.getLogger(getClass());
	private AdvanceRepository advMoney;
	
	private TodoRepository todoRepository;
	private TodoService todoService;
	public TodoControllerJpa(TodoService todoService, TodoRepository todoRepository, AdvanceRepository advMoney) {
		super();
		this.todoService = todoService;
		this.todoRepository = todoRepository;
		this.advMoney = advMoney;
	}

	@RequestMapping(value = "/list-todos", method = RequestMethod.GET)
	private String listAllTodos(ModelMap model, Principal principal) {
	    String username = principal.getName();
	    logger.debug("Username: {}", username);
	    
	    // Fetch todos and calculate the consumed amount
	    List<Todo> todos = todoRepository.findByUsername(username);
	    int consumedAmount = todos.stream().mapToInt(Todo::getAmount).sum();
	    
	    // Fetch advances and calculate the total advance amount
	    List<Advance> advances = advMoney.findByUsername(username);
	    int totalAdvanceAmount = advances.stream().mapToInt(Advance::getAmount).sum();
	    
	    // Calculate the payable amount using the service method	   
	   // int payableAmount = todoService.getPayAmount(totalAdvanceAmount, consumedAmount, username);
	    // For testing
	  //  int payableAmount = consumedAmount;
	    
	    // Add attributes to the model
	    model.addAttribute("todos", todos);
	    model.addAttribute("consumedAmount", consumedAmount);
	    model.addAttribute("availableAdvance", totalAdvanceAmount);
	   // model.addAttribute("payableAmount", payableAmount);
	    model.addAttribute("username", username);
	    
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
	
	@RequestMapping(value = "/add-advance", method = RequestMethod.GET)
	private String showAddAdvancePage(ModelMap model, Principal principal) {
		String username = getLoggedInUsername(model);
		//Todo todo = new Todo(0, username, "", LocalDate.now().plusYears(1), true, "", 0);
		Advance advance = new Advance(username, 0,LocalDate.now().plusYears(1));
		model.put("advance", advance);
		logger.debug("Show Advance Page Hit");
		return "advancePage";
	}
	
	@RequestMapping(value = "/add-advance", method = RequestMethod.POST)
	private String addAdvance(ModelMap model, @Valid Advance advance, BindingResult result) {
		if (result.hasErrors()) {

			return "advancePage";
		}
		String username = getLoggedInUsername(model);
		advance.setUsername(username);
		advMoney.save(advance);		
		logger.debug("Add New Todo Hit");
		return "redirect:list-todos";
	}

}