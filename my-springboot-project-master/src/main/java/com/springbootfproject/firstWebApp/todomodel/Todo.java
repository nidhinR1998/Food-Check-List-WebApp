package com.springbootfproject.firstWebApp.todomodel;

import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

@Entity
public class Todo {
	public Todo() {

	}

	@Id
	@GeneratedValue
	private int id;
	private String username;
	private String description;
	private LocalDate targetDate;
	private boolean received;
	private String time;
	private int amount;

	public Todo(int id, String username, String description, LocalDate targetDate, boolean received, String time, int amount) {
		super();
		this.id = id;
		this.username = username;
		this.description = description;
		this.targetDate = targetDate;
		this.received = received;
		this.time = time;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public LocalDate getTargetDate() {
		return targetDate;
	}

	public void setTargetDate(LocalDate targetDate) {
		this.targetDate = targetDate;
	}

	public boolean isReceived() {
		return received;
	}

	/*
	 * public void setDone(boolean done) { this.done = done; }
	 */

	public String getTime() {
		return time;
	}

	/*
	 * public void setTime(String time) { this.time = time; }
	 */

	public int getAmount() {
		return amount;
	}

	public void setAmount(int amount) {
		this.amount = amount;
	}

	public void setTime(String time) {
        this.time = time;
        setAmountBasedOnTime();
    }

    public void setReceived(boolean received) {
        this.received = received;
       
    }

    private void setAmountBasedOnTime() {
        if (this.received) {
            switch (this.time.toLowerCase()) {
                case "morning":
                    this.amount = 45;
                    break;
                case "afternoon":
                    this.amount = 50;
                    break;
                case "night":
                    this.amount = 50;
                    break;
                default:
                    this.amount = 0;
            }
        } else {
            this.amount = 0;
        }
    }
	
	@Override
	public String toString() {
		return "todos [id=" + id + ", username=" + username + ", description=" + description + ", targetDate="
				+ targetDate + ", received=" + received + ", time=" + time + ", amount=" + amount + "]";
	}

}
