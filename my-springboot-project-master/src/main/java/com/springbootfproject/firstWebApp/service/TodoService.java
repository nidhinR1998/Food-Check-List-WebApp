package com.springbootfproject.firstWebApp.service;

import java.net.http.HttpRequest;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.SessionAttributes;

import com.springbootfproject.firstWebApp.dto.TodoFilterReq;
import com.springbootfproject.firstWebApp.dto.TodoFilterRes;
import com.springbootfproject.firstWebApp.repository.AdvanceRepository;
import com.springbootfproject.firstWebApp.repository.TodoRepository;
import com.springbootfproject.firstWebApp.todomodel.Todo;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Service
@SessionAttributes("username")
public class TodoService {
	@Autowired
	private TodoRepository todoRepository;
	
	@Autowired
	private AdvanceRepository advanceRepository;

	private Logger logger = LoggerFactory.getLogger(getClass());

	private static List<Todo> todos = new ArrayList();
	private static int todosCount = 0;

	public List<Todo> findByUsername(String username) {
		Predicate<? super Todo> predicate = todo -> todo.getUsername().equalsIgnoreCase(username);
		return todos.stream().filter(predicate).toList();
	}

	public void addTodo(String username, String description, LocalDate targetDate, boolean done, String time,
			int amount) {
		Todo todo = new Todo(++todosCount, username, description, targetDate, done, time, amount);
		logger.debug("Adding a Food Details by the user: {}", username);
		todos.add(todo);
	}

	public void deleteById(int id) {
		Predicate<? super Todo> predicate = todo -> todo.getId() == id;
		todos.removeIf(predicate);

	}

	public Todo findById(int id) {
		Predicate<? super Todo> predicate = todo -> todo.getId() == id;
		Todo todo = todos.stream().filter(predicate).findFirst().get();
		return todo;
	}

	public void updateTodo(@Valid Todo todo) {
		deleteById(todo.getId());
		todos.add(todo);

	}
	
	public TodoFilterRes getfilterTodos(TodoFilterReq req) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String name = authentication.getName();
		List<Todo> allTodos = todoRepository.findByUsername(name);

		logger.debug("All Todos from database: {}", allTodos);

		List<Todo> filteredTodos = allTodos.stream()
				.filter(todo -> (req.getTime() == null || req.getTime().isEmpty()
						|| todo.getTime().equalsIgnoreCase(req.getTime())))
				.filter(todo -> (req.getReceived() == null || todo.isReceived() == req.getReceived()))
				.filter(todo -> (req.getMonth() == null || todo.getTargetDate().getMonthValue() == req.getMonth()))
				.collect(Collectors.toList());

		
		logger.debug("Filtered Todos: {}", filteredTodos);

		TodoFilterRes response = new TodoFilterRes();
		response.setTodos(filteredTodos);

		return response;
	}

	public int calculateTotalAmount(List<Todo> todos) {
		int totalAmount = todos.stream().mapToInt(Todo::getAmount).sum();
		return totalAmount;
	}

	public String getFullName(String username) {
		
		return null;
	}

	public int getPayAmount(int totalAdvanceAmount, int consumedAmount, String username) {
	    int difference = totalAdvanceAmount - consumedAmount;

	    if (difference > 0) {
	        // STAGE 1: Difference is positive
	        // Update totalAdvanceAmount to the difference
	        totalAdvanceAmount = difference;

	        // Update the Todo table to set all amounts to zero
	        List<Todo> todos = todoRepository.findByUsername(username);
	        for (Todo todo : todos) {
	            todo.setAmount(0);
	            todoRepository.save(todo);
	        }
	    } else if (difference < 0) {
	        // STAGE 2: Difference is negative
	        // Set totalAdvanceAmount to zero
	        totalAdvanceAmount = 0;

	        // Update the Advance table to set totalAdvanceAmount to zero
	        advanceRepository.updateTotalAdvanceAmount(totalAdvanceAmount, username);

	        // Update consumedAmount to the absolute value of the difference
	        consumedAmount = Math.abs(difference);
	    } else {
	        // STAGE 3: Difference is zero
	        // No changes needed for both totalAdvanceAmount and consumedAmount
	        totalAdvanceAmount = 0;

	        // Update the Todo table to set all amounts to zero
	        List<Todo> todos = todoRepository.findByUsername(username);
	        for (Todo todo : todos) {
	            todo.setAmount(0);
	            todoRepository.save(todo);
	        }
	    }

	    // Update the totalAdvanceAmount in the database
	    advanceRepository.updateTotalAdvanceAmount(totalAdvanceAmount, username);

	    // Return the payable amount (which should be 0 if difference is positive)
	    return Math.max(0, consumedAmount - totalAdvanceAmount);
	}


}
