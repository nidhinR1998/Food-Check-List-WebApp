package com.springbootfproject.firstWebApp.dto;

import java.util.List;

import com.springbootfproject.firstWebApp.todomodel.Todo;

public class TodoFilterRes {
	private List<Todo> todos;
	private int totalAmount;

	
	public int getTotalAmount() {
		return totalAmount;
	}

	public void setTotalAmount(int totalAmount) {
		this.totalAmount = totalAmount;
	}

	public List<Todo> getTodos() {
		return todos;
	}

	public void setTodos(List<Todo> todos) {
		this.todos = todos;
	}
	
	
	
}
