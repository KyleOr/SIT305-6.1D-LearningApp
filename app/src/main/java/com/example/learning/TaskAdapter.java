package com.example.learning;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {
    private List<Task> tasks;

    public TaskAdapter(List<Task> tasks) {
        this.tasks = tasks;
    }

    @Override
    public TaskViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(TaskViewHolder holder, int position) {
        Task task = tasks.get(position);

        // Make sure all views are properly set
        holder.taskTitle.setText(task.getName());
        holder.taskDescription.setText(task.getDescription());
        holder.goButton.setVisibility(View.VISIBLE); // Ensure button is visible

        holder.goButton.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), QuestionActivity.class);
            intent.putExtra("taskName", task.getName());
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    public void updateTasks(List<Task> newTasks) {
        this.tasks = newTasks;
        notifyDataSetChanged();
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView taskTitle;
        TextView taskDescription;
        Button goButton;

        public TaskViewHolder(View itemView) {
            super(itemView);
            taskTitle = itemView.findViewById(R.id.taskTitle);
            taskDescription = itemView.findViewById(R.id.taskDescription);
            goButton = itemView.findViewById(R.id.goButton);

            // Make sure all views are found
            if (taskTitle == null || taskDescription == null || goButton == null) {
                throw new RuntimeException("Missing view in item_task.xml");
            }
        }
    }
}