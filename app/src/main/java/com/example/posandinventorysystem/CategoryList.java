    // CategoryList.java
    package com.example.posandinventorysystem;

    import androidx.appcompat.app.AlertDialog;
    import androidx.appcompat.app.AppCompatActivity;

    import android.content.DialogInterface;
    import android.os.Bundle;
    import android.view.LayoutInflater;
    import android.view.View;
    import android.view.ViewGroup;
    import android.widget.ArrayAdapter;
    import android.widget.Button;
    import android.widget.EditText;
    import android.widget.ListView;
    import android.widget.TextView;
    import android.widget.Toast;

    import java.util.List;

    public class CategoryList extends AppCompatActivity {

        EditText editCateName;
        Button addButton;
        ListView categoryListView;
        CategoryAdapter adapter; // Custom adapter
        DBHelper DB;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_category_list);

            editCateName = findViewById(R.id.EditCateName);
            addButton = findViewById(R.id.myCircularButton);
            categoryListView = findViewById(R.id.categoryListView);
            DB = new DBHelper(this);

            addButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String categoryName = editCateName.getText().toString().trim();

                    // Check if the category name is not empty
                    if (!categoryName.isEmpty()) {
                        // Insert the category into the database
                        boolean isInserted = DB.insertCategory(categoryName);

                        // Check if insertion was successful
                        if (isInserted) {
                            Toast.makeText(CategoryList.this, "Category added successfully", Toast.LENGTH_SHORT).show();
                            // Clear the input field
                            editCateName.setText("");
                            // Reload categories into the ListView
                            loadCategories();
                        } else {
                            Toast.makeText(CategoryList.this, "Failed to add category", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // Show a toast indicating that the category name is empty
                        Toast.makeText(CategoryList.this, "Please provide a valid category name", Toast.LENGTH_SHORT).show();
                    }
                }
            });


            // Set adapter for ListView
            adapter = new CategoryAdapter(this, R.layout.category_item, DB.getAllCategories());
            categoryListView.setAdapter(adapter);

            // Call loadCategories() method to load categories into the ListView
            loadCategories();
        }

        // Method to load categories into the ListView
        private void loadCategories() {
            // Update adapter data
            adapter.clear();
            List<String> categories = DB.getAllCategories();
            // Remove "All Categories" from the list
            categories.remove("All Categories");
            adapter.addAll(categories);
            adapter.notifyDataSetChanged();
        }


        // Custom adapter for displaying categories with edit and delete buttons
        private class CategoryAdapter extends ArrayAdapter<String> {

            public CategoryAdapter(CategoryList context, int resource, List<String> categories) {
                super(context, resource, categories);
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                LayoutInflater inflater = LayoutInflater.from(getContext());
                View view = inflater.inflate(R.layout.category_item, parent, false);

                TextView categoryText = view.findViewById(R.id.categoryText);
                Button editButton = view.findViewById(R.id.editButton);
                Button deleteButton = view.findViewById(R.id.deleteButton);

                final String categoryName = getItem(position);

                categoryText.setText(categoryName);

                editButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // Handle edit button click
                        // Show a dialog for editing the category
                        AlertDialog.Builder builder = new AlertDialog.Builder(CategoryList.this);
                        builder.setTitle("Edit Category");

                        // Set up the input field
                        final EditText input = new EditText(CategoryList.this);
                        input.setText(categoryName);
                        builder.setView(input);

                        // Set up the buttons
                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String editedCategoryName = input.getText().toString().trim();
                                if (!editedCategoryName.isEmpty() && !editedCategoryName.equals(categoryName)) {
                                    // Update the category name in the database
                                    boolean isUpdated = DB.updateCategory(categoryName, editedCategoryName);
                                    if (isUpdated) {
                                        // Reload categories into the ListView
                                        loadCategories();
                                        Toast.makeText(CategoryList.this, "Category updated successfully", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(CategoryList.this, "Failed to update category", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    // Show a toast indicating that the category name is empty or unchanged
                                    Toast.makeText(CategoryList.this, "Please provide a valid category name", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });

                        builder.show();
                    }
                });

                deleteButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // Handle delete button click
                        // Show a confirmation dialog before deleting the category
                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder.setTitle("Delete Category");
                        builder.setMessage("Are you sure you want to delete this category?");

                        // Set up the buttons
                        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                boolean isDeleted = DB.deleteCategory(categoryName);
                                if (isDeleted) {
                                    // Notify the adapter that the data set has changed
                                    adapter.remove(categoryName);
                                    adapter.notifyDataSetChanged();
                                    // Show a toast message indicating successful deletion
                                    Toast.makeText(getContext(), "Category deleted successfully", Toast.LENGTH_SHORT).show();
                                } else {
                                    // Show a toast message indicating deletion failure
                                    Toast.makeText(getContext(), "Failed to delete category", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Do nothing, simply close the dialog
                                dialog.dismiss();
                            }
                        });

                        builder.show();
                    }
                });


                return view;
            }
        }
    }